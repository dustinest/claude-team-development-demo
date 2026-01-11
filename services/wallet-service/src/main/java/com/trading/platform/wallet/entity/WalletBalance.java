package com.trading.platform.wallet.entity;

import com.trading.platform.domain.Currency;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "wallet_balances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "currency"})
})
public class WalletBalance extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Currency currency;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "updated_at")
    public Instant updatedAt;

    public static Optional<WalletBalance> findByUserAndCurrency(UUID userId, Currency currency) {
        return find("userId = ?1 and currency = ?2", userId, currency).firstResultOptional();
    }

    public static List<WalletBalance> findByUser(UUID userId) {
        return find("userId", userId).list();
    }
}
