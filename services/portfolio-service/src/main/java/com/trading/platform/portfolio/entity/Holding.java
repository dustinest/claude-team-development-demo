package com.trading.platform.portfolio.entity;

import com.trading.platform.domain.Currency;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "symbol"})
})
public class Holding extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false)
    public String symbol;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "average_price", precision = 19, scale = 2, nullable = false)
    public BigDecimal averagePrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Currency currency;

    @Column(name = "updated_at")
    public Instant updatedAt;

    public static Optional<Holding> findByUserAndSymbol(UUID userId, String symbol) {
        return find("userId = ?1 and symbol = ?2", userId, symbol).firstResultOptional();
    }

    public static List<Holding> findByUser(UUID userId) {
        return find("userId", userId).list();
    }
}
