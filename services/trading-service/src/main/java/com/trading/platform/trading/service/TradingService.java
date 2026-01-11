package com.trading.platform.trading.service;

import com.trading.platform.domain.*;
import com.trading.platform.events.TradeCompletedEvent;
import com.trading.platform.trading.entity.Trade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.smallrye.reactive.messaging.MutinyEmitter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class TradingService {
    private static final Logger LOG = LoggerFactory.getLogger(TradingService.class);

    @Inject @RestClient PricingClient pricingClient;
    @Inject @RestClient FeeClient feeClient;
    @Inject @RestClient WalletClient walletClient;

    @org.eclipse.microprofile.reactive.messaging.Channel("trading-events-out")
    MutinyEmitter<TradeCompletedEvent> tradingEventsEmitter;

    @Transactional
    public Trade executeBuy(UUID userId, String symbol, Currency currency, OrderType orderType,
                            BigDecimal amountOrQuantity) {
        // Get current price
        BigDecimal currentPrice = pricingClient.getPrice(symbol).price;

        // Calculate quantity and total based on order type
        BigDecimal quantity, totalBeforeFees;
        if (orderType == OrderType.BY_AMOUNT) {
            // User specifies money amount, calculate quantity
            BigDecimal amountAfterFees = calculateAmountAfterFees(symbol, amountOrQuantity);
            quantity = MoneyCalculator.roundQuantityForBuy(amountAfterFees.divide(currentPrice, 10, RoundingMode.HALF_UP));
            totalBeforeFees = quantity.multiply(currentPrice);
        } else {
            // User specifies quantity
            quantity = amountOrQuantity;
            totalBeforeFees = quantity.multiply(currentPrice);
        }

        // Calculate fees
        BigDecimal fees = feeClient.getTradingFee(symbol, totalBeforeFees).fee;
        BigDecimal totalWithFees = totalBeforeFees.add(fees);

        // Validate and deduct funds from wallet
        try {
            walletClient.validateFunds(userId, currency, totalWithFees);
        } catch (Exception e) {
            LOG.error("Insufficient funds for trade: userId={}, required={}", userId, totalWithFees);
            return createFailedTrade(userId, symbol, TradeType.BUY, orderType, quantity,
                currentPrice, currency, totalWithFees, fees, "Insufficient funds");
        }

        // Create and persist trade
        Trade trade = new Trade();
        trade.userId = userId;
        trade.symbol = symbol;
        trade.tradeType = TradeType.BUY;
        trade.orderType = orderType;
        trade.quantity = quantity;
        trade.pricePerUnit = currentPrice;
        trade.currency = currency;
        trade.totalAmount = totalWithFees;
        trade.fees = fees;
        trade.status = TradeStatus.COMPLETED;
        trade.createdAt = Instant.now();
        trade.completedAt = Instant.now();
        trade.persist();

        LOG.info("Buy trade executed: tradeId={}, userId={}, symbol={}, quantity={}, price={}, total={}",
            trade.id, userId, symbol, quantity, currentPrice, totalWithFees);

        // Publish event
        publishTradeCompletedEvent(trade);

        return trade;
    }

    @Transactional
    public Trade executeSell(UUID userId, String symbol, Currency currency, OrderType orderType,
                             BigDecimal amountOrQuantity) {
        // Get current price
        BigDecimal currentPrice = pricingClient.getPrice(symbol).price;

        // Calculate quantity and total
        BigDecimal quantity, totalBeforeFees;
        if (orderType == OrderType.BY_AMOUNT) {
            // User specifies money amount they want to receive
            quantity = amountOrQuantity.divide(currentPrice, 2, RoundingMode.DOWN);
            totalBeforeFees = quantity.multiply(currentPrice);
        } else {
            // User specifies quantity to sell
            quantity = amountOrQuantity;
            totalBeforeFees = quantity.multiply(currentPrice);
        }

        // Calculate fees
        BigDecimal fees = feeClient.getTradingFee(symbol, totalBeforeFees).fee;
        BigDecimal totalAfterFees = MoneyCalculator.roundAmountForSell(totalBeforeFees.subtract(fees));

        // Create and persist trade
        Trade trade = new Trade();
        trade.userId = userId;
        trade.symbol = symbol;
        trade.tradeType = TradeType.SELL;
        trade.orderType = orderType;
        trade.quantity = quantity;
        trade.pricePerUnit = currentPrice;
        trade.currency = currency;
        trade.totalAmount = totalAfterFees;
        trade.fees = fees;
        trade.status = TradeStatus.COMPLETED;
        trade.createdAt = Instant.now();
        trade.completedAt = Instant.now();
        trade.persist();

        LOG.info("Sell trade executed: tradeId={}, userId={}, symbol={}, quantity={}, price={}, total={}",
            trade.id, userId, symbol, quantity, currentPrice, totalAfterFees);

        // Publish event
        publishTradeCompletedEvent(trade);

        return trade;
    }

    private BigDecimal calculateAmountAfterFees(String symbol, BigDecimal amount) {
        BigDecimal estimatedFee = feeClient.getTradingFee(symbol, amount).fee;
        return amount.subtract(estimatedFee);
    }

    private Trade createFailedTrade(UUID userId, String symbol, TradeType tradeType, OrderType orderType,
                                    BigDecimal quantity, BigDecimal price, Currency currency,
                                    BigDecimal total, BigDecimal fees, String reason) {
        Trade trade = new Trade();
        trade.userId = userId;
        trade.symbol = symbol;
        trade.tradeType = tradeType;
        trade.orderType = orderType;
        trade.quantity = quantity;
        trade.pricePerUnit = price;
        trade.currency = currency;
        trade.totalAmount = total;
        trade.fees = fees;
        trade.status = TradeStatus.FAILED;
        trade.createdAt = Instant.now();
        trade.persist();
        return trade;
    }

    private void publishTradeCompletedEvent(Trade trade) {
        TradeCompletedEvent event = new TradeCompletedEvent();
        event.setTradeId(trade.id);
        event.setUserId(trade.userId);
        event.setSymbol(trade.symbol);
        event.setTradeType(trade.tradeType);
        event.setQuantity(trade.quantity);
        event.setPricePerUnit(trade.pricePerUnit);
        event.setCurrency(trade.currency);
        event.setTotalAmount(trade.totalAmount);
        event.setFees(trade.fees);
        tradingEventsEmitter.sendAndAwait(event);
    }
}
