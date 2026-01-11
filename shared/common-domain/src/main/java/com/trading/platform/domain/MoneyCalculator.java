package com.trading.platform.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for monetary calculations with customer-favorable rounding.
 * All rounding is done in favor of the customer as per business requirements.
 */
public class MoneyCalculator {
    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 6;

    private MoneyCalculator() {
        // Utility class
    }

    /**
     * Round money amounts to 2 decimal places using standard rounding.
     */
    public static BigDecimal roundMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Round quantity for buy orders in customer's favor (round up).
     * Customer gets more shares when buying with a fixed amount.
     */
    public static BigDecimal roundQuantityForBuy(BigDecimal quantity) {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        return quantity.setScale(MONEY_SCALE, RoundingMode.UP);
    }

    /**
     * Round amount for sell orders in customer's favor (round up).
     * Customer gets more money when selling shares.
     */
    public static BigDecimal roundAmountForSell(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UP);
    }

    /**
     * Round exchange rates to 6 decimal places.
     */
    public static BigDecimal roundRate(BigDecimal rate) {
        if (rate == null) {
            return BigDecimal.ZERO;
        }
        return rate.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Convert currency amount using exchange rate.
     */
    public static BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        if (amount == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return roundMoney(amount.multiply(rate));
    }

    /**
     * Calculate percentage of an amount.
     */
    public static BigDecimal percentage(BigDecimal amount, BigDecimal percentageRate) {
        if (amount == null || percentageRate == null) {
            return BigDecimal.ZERO;
        }
        return roundMoney(amount.multiply(percentageRate));
    }
}
