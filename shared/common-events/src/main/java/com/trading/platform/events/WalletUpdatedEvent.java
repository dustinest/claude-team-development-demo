package com.trading.platform.events;

import com.trading.platform.domain.Currency;
import java.math.BigDecimal;
import java.util.UUID;

public class WalletUpdatedEvent extends BaseEvent {
    private UUID userId;
    private Currency currency;
    private BigDecimal newBalance;

    public WalletUpdatedEvent() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
}
