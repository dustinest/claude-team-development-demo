package com.trading.platform.transactionhistory.service;

import com.trading.platform.domain.*;
import com.trading.platform.transactionhistory.entity.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    @Transactional
    public Transaction recordTransaction(UUID userId, TransactionType type, Currency currency,
                                         BigDecimal amount, BigDecimal fees, UUID relatedEntityId, String metadata) {
        Transaction txn = new Transaction();
        txn.userId = userId;
        txn.type = type;
        txn.currency = currency;
        txn.amount = amount;
        txn.fees = fees != null ? fees : BigDecimal.ZERO;
        txn.relatedEntityId = relatedEntityId;
        txn.metadata = metadata;
        txn.createdAt = Instant.now();
        txn.persist();
        LOG.info("Recorded transaction: userId={}, type={}, amount={}", userId, type, amount);
        return txn;
    }

    public List<Transaction> getTransactions(UUID userId, TransactionType type) {
        return type != null ?
            Transaction.findByUserAndType(userId, type) :
            Transaction.findByUser(userId);
    }
}
