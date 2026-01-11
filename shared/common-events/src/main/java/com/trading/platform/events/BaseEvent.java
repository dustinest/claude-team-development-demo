package com.trading.platform.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "UserCreated"),
    @JsonSubTypes.Type(value = WalletUpdatedEvent.class, name = "WalletUpdated"),
    @JsonSubTypes.Type(value = DepositCompletedEvent.class, name = "DepositCompleted"),
    @JsonSubTypes.Type(value = WithdrawalCompletedEvent.class, name = "WithdrawalCompleted"),
    @JsonSubTypes.Type(value = CurrencyExchangedEvent.class, name = "CurrencyExchanged"),
    @JsonSubTypes.Type(value = TradeCompletedEvent.class, name = "TradeCompleted"),
    @JsonSubTypes.Type(value = TradeFailedEvent.class, name = "TradeFailed")
})
public abstract class BaseEvent {
    private String eventId = UUID.randomUUID().toString();
    private Instant timestamp = Instant.now();

    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
