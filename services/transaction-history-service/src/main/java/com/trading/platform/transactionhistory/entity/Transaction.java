package com.trading.platform.transactionhistory.entity;

import com.trading.platform.domain.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Currency currency;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal amount;

    @Column(precision = 19, scale = 2, nullable = false)
    public BigDecimal fees = BigDecimal.ZERO;

    @Column(name = "related_entity_id")
    public UUID relatedEntityId;

    @Column(columnDefinition = "TEXT")
    public String metadata;

    @Column(name = "created_at")
    public Instant createdAt;

    public static List<Transaction> findByUser(UUID userId) {
        return find("userId", userId).list();
    }

    public static List<Transaction> findByUserAndType(UUID userId, TransactionType type) {
        return find("userId = ?1 and type = ?2", userId, type).list();
    }
}
