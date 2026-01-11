package com.trading.platform.fee.service;

import com.trading.platform.domain.Currency;
import com.trading.platform.domain.MoneyCalculator;
import com.trading.platform.fee.entity.FeeRule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class FeeCalculationService {
    private static final Logger LOG = LoggerFactory.getLogger(FeeCalculationService.class);

    // Default fees if no rules are found
    private static final BigDecimal DEFAULT_TRADING_FIXED_FEE = new BigDecimal("0.50");
    private static final BigDecimal DEFAULT_TRADING_PERCENTAGE = new BigDecimal("0.01"); // 1%
    private static final BigDecimal DEFAULT_EXCHANGE_FIXED_FEE = new BigDecimal("0.25");
    private static final BigDecimal DEFAULT_EXCHANGE_PERCENTAGE = new BigDecimal("0.005"); // 0.5%

    public BigDecimal calculateTradingFee(String symbol, BigDecimal amount) {
        FeeRule rule = FeeRule.findTradingFee(symbol).orElse(null);

        BigDecimal fixedFee = rule != null ? rule.fixedFee : DEFAULT_TRADING_FIXED_FEE;
        BigDecimal percentageFee = rule != null ? rule.percentageFee : DEFAULT_TRADING_PERCENTAGE;

        BigDecimal fee = fixedFee.add(MoneyCalculator.percentage(amount, percentageFee));
        LOG.debug("Calculated trading fee for {}: {} (amount: {}, fixed: {}, percentage: {})",
            symbol, fee, amount, fixedFee, percentageFee);

        return fee;
    }

    public BigDecimal calculateExchangeFee(Currency from, Currency to, BigDecimal amount) {
        FeeRule rule = FeeRule.findExchangeFee(from, to).orElse(null);

        BigDecimal fixedFee = rule != null ? rule.fixedFee : DEFAULT_EXCHANGE_FIXED_FEE;
        BigDecimal percentageFee = rule != null ? rule.percentageFee : DEFAULT_EXCHANGE_PERCENTAGE;

        BigDecimal fee = fixedFee.add(MoneyCalculator.percentage(amount, percentageFee));
        LOG.debug("Calculated exchange fee {} to {}: {} (amount: {})",
            from, to, fee, amount);

        return fee;
    }

    @Transactional
    public FeeRule createTradingFee(String symbol, BigDecimal fixedFee, BigDecimal percentageFee) {
        FeeRule rule = new FeeRule();
        rule.ruleType = "TRADING";
        rule.symbol = symbol;
        rule.fixedFee = fixedFee;
        rule.percentageFee = percentageFee;
        rule.persist();
        LOG.info("Created trading fee rule for symbol: {}", symbol);
        return rule;
    }

    @Transactional
    public FeeRule createExchangeFee(Currency from, Currency to, BigDecimal fixedFee, BigDecimal percentageFee) {
        FeeRule rule = new FeeRule();
        rule.ruleType = "EXCHANGE";
        rule.fromCurrency = from;
        rule.toCurrency = to;
        rule.fixedFee = fixedFee;
        rule.percentageFee = percentageFee;
        rule.persist();
        LOG.info("Created exchange fee rule: {} to {}", from, to);
        return rule;
    }
}
