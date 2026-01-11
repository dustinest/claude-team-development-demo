package com.trading.platform.events;

import com.trading.platform.domain.Currency;
import com.trading.platform.domain.TradeType;
import java.math.BigDecimal;
import java.util.UUID;

public class TradeCompletedEvent extends BaseEvent {
    private UUID tradeId;
    private UUID userId;
    private String symbol;
    private TradeType tradeType;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private Currency currency;
    private BigDecimal totalAmount;
    private BigDecimal fees;

    public TradeCompletedEvent() {}

    // Getters and setters
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID tradeId) { this.tradeId = tradeId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public TradeType getTradeType() { return tradeType; }
    public void setTradeType(TradeType tradeType) { this.tradeType = tradeType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
}
