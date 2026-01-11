package com.trading.platform.pricing.entity;

import com.trading.platform.domain.SecurityType;
import java.math.BigDecimal;
import java.time.Instant;

public class Security {
    private String symbol;
    private String name;
    private SecurityType type;
    private BigDecimal currentPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Instant lastUpdated;

    public Security() {}

    public Security(String symbol, String name, SecurityType type, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
        this.currentPrice = currentPrice;
        this.openPrice = currentPrice;
        this.highPrice = currentPrice;
        this.lowPrice = currentPrice;
        this.lastUpdated = Instant.now();
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SecurityType getType() { return type; }
    public void setType(SecurityType type) { this.type = type; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }

    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }

    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
