package com.trading.platform.events;

import java.util.UUID;

public class TradeFailedEvent extends BaseEvent {
    private UUID tradeId;
    private UUID userId;
    private String reason;

    public TradeFailedEvent() {}

    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID tradeId) { this.tradeId = tradeId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
