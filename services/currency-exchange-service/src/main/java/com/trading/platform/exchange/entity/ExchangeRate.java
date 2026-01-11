package com.trading.platform.exchange.entity;

import com.trading.platform.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;

public class ExchangeRate {
    private Currency fromCurrency;
    private Currency toCurrency;
    private BigDecimal rate;
    private Instant lastUpdated;

    public ExchangeRate() {}

    public ExchangeRate(Currency from, Currency to, BigDecimal rate) {
        this.fromCurrency = from;
        this.toCurrency = to;
        this.rate = rate;
        this.lastUpdated = Instant.now();
    }

    public Currency getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(Currency fromCurrency) { this.fromCurrency = fromCurrency; }
    public Currency getToCurrency() { return toCurrency; }
    public void setToCurrency(Currency toCurrency) { this.toCurrency = toCurrency; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
