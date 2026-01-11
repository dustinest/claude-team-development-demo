package com.trading.platform.events;

import com.trading.platform.domain.Currency;
import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawalCompletedEvent extends BaseEvent {
    private UUID userId;
    private Currency currency;
    private BigDecimal amount;

    public WithdrawalCompletedEvent() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
