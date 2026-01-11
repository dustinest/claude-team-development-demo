package com.trading.platform.trading.entity;

import com.trading.platform.domain.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false)
    public String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false)
    public TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    public OrderType orderType;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal quantity;

    @Column(name = "price_per_unit", precision = 19, scale = 2, nullable = false)
    public BigDecimal pricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Currency currency;

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    public BigDecimal totalAmount;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal fees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TradeStatus status;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "completed_at")
    public Instant completedAt;
}
