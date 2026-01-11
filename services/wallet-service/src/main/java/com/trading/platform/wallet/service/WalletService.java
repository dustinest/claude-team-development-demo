package com.trading.platform.wallet.service;

import com.trading.platform.domain.Currency;
import com.trading.platform.domain.MoneyCalculator;
import com.trading.platform.events.*;
import com.trading.platform.wallet.entity.WalletBalance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.smallrye.reactive.messaging.MutinyEmitter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletService {
    private static final Logger LOG = LoggerFactory.getLogger(WalletService.class);

    @Inject
    @RestClient
    ExchangeRateClient exchangeRateClient;

    @Inject
    @RestClient
    FeeClient feeClient;

    @org.eclipse.microprofile.reactive.messaging.Channel("wallet-events-out")
    MutinyEmitter<BaseEvent> walletEventsEmitter;

    @Transactional
    public WalletBalance deposit(UUID userId, Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        WalletBalance balance = getOrCreateBalance(userId, currency);
        balance.balance = balance.balance.add(amount);
        balance.updatedAt = Instant.now();
        balance.persist();

        LOG.info("Deposit: userId={}, currency={}, amount={}, newBalance={}",
            userId, currency, amount, balance.balance);

        publishDepositEvent(userId, currency, amount);
        publishWalletUpdatedEvent(userId, currency, balance.balance);

        return balance;
    }

    @Transactional
    public WalletBalance withdraw(UUID userId, Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        WalletBalance balance = WalletBalance.findByUserAndCurrency(userId, currency)
            .orElseThrow(() -> new IllegalStateException("Insufficient funds"));

        if (balance.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        balance.balance = balance.balance.subtract(amount);
        balance.updatedAt = Instant.now();
        balance.persist();

        LOG.info("Withdrawal: userId={}, currency={}, amount={}, newBalance={}",
            userId, currency, amount, balance.balance);

        publishWithdrawalEvent(userId, currency, amount);
        publishWalletUpdatedEvent(userId, currency, balance.balance);

        return balance;
    }

    @Transactional
    public ExchangeResult exchange(UUID userId, Currency fromCurrency, Currency toCurrency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Get exchange rate
        BigDecimal rate = exchangeRateClient.getRate(fromCurrency, toCurrency).rate;

        // Calculate fee
        BigDecimal fee = feeClient.getExchangeFee(fromCurrency, toCurrency, amount).fee;

        // Calculate amounts
        BigDecimal amountAfterFee = amount.subtract(fee);
        BigDecimal convertedAmount = MoneyCalculator.convert(amountAfterFee, rate);

        // Deduct from source currency
        WalletBalance fromBalance = WalletBalance.findByUserAndCurrency(userId, fromCurrency)
            .orElseThrow(() -> new IllegalStateException("Insufficient funds"));

        if (fromBalance.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        fromBalance.balance = fromBalance.balance.subtract(amount);
        fromBalance.updatedAt = Instant.now();
        fromBalance.persist();

        // Add to target currency
        WalletBalance toBalance = getOrCreateBalance(userId, toCurrency);
        toBalance.balance = toBalance.balance.add(convertedAmount);
        toBalance.updatedAt = Instant.now();
        toBalance.persist();

        LOG.info("Exchange: userId={}, {} {} -> {} {} (rate={}, fee={})",
            userId, fromCurrency, amount, toCurrency, convertedAmount, rate, fee);

        publishCurrencyExchangedEvent(userId, fromCurrency, toCurrency, amount, convertedAmount, rate, fee);
        publishWalletUpdatedEvent(userId, fromCurrency, fromBalance.balance);
        publishWalletUpdatedEvent(userId, toCurrency, toBalance.balance);

        return new ExchangeResult(fromBalance, toBalance, convertedAmount, rate, fee);
    }

    public List<WalletBalance> getBalances(UUID userId) {
        return WalletBalance.findByUser(userId);
    }

    public BigDecimal getBalance(UUID userId, Currency currency) {
        return WalletBalance.findByUserAndCurrency(userId, currency)
            .map(b -> b.balance)
            .orElse(BigDecimal.ZERO);
    }

    @Transactional
    WalletBalance getOrCreateBalance(UUID userId, Currency currency) {
        return WalletBalance.findByUserAndCurrency(userId, currency)
            .orElseGet(() -> {
                WalletBalance balance = new WalletBalance();
                balance.userId = userId;
                balance.currency = currency;
                balance.balance = BigDecimal.ZERO;
                balance.updatedAt = Instant.now();
                balance.persist();
                return balance;
            });
    }

    private void publishDepositEvent(UUID userId, Currency currency, BigDecimal amount) {
        DepositCompletedEvent event = new DepositCompletedEvent();
        event.setUserId(userId);
        event.setCurrency(currency);
        event.setAmount(amount);
        walletEventsEmitter.sendAndAwait(event);
    }

    private void publishWithdrawalEvent(UUID userId, Currency currency, BigDecimal amount) {
        WithdrawalCompletedEvent event = new WithdrawalCompletedEvent();
        event.setUserId(userId);
        event.setCurrency(currency);
        event.setAmount(amount);
        walletEventsEmitter.sendAndAwait(event);
    }

    private void publishCurrencyExchangedEvent(UUID userId, Currency from, Currency to,
                                                BigDecimal fromAmount, BigDecimal toAmount,
                                                BigDecimal rate, BigDecimal fee) {
        CurrencyExchangedEvent event = new CurrencyExchangedEvent();
        event.setUserId(userId);
        event.setFromCurrency(from);
        event.setToCurrency(to);
        event.setFromAmount(fromAmount);
        event.setToAmount(toAmount);
        event.setExchangeRate(rate);
        event.setFees(fee);
        walletEventsEmitter.sendAndAwait(event);
    }

    private void publishWalletUpdatedEvent(UUID userId, Currency currency, BigDecimal newBalance) {
        WalletUpdatedEvent event = new WalletUpdatedEvent();
        event.setUserId(userId);
        event.setCurrency(currency);
        event.setNewBalance(newBalance);
        walletEventsEmitter.sendAndAwait(event);
    }

    public static class ExchangeResult {
        public WalletBalance fromBalance;
        public WalletBalance toBalance;
        public BigDecimal convertedAmount;
        public BigDecimal exchangeRate;
        public BigDecimal fee;

        public ExchangeResult(WalletBalance fromBalance, WalletBalance toBalance,
                              BigDecimal convertedAmount, BigDecimal exchangeRate, BigDecimal fee) {
            this.fromBalance = fromBalance;
            this.toBalance = toBalance;
            this.convertedAmount = convertedAmount;
            this.exchangeRate = exchangeRate;
            this.fee = fee;
        }
    }
}
