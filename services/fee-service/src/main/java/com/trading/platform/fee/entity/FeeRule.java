package com.trading.platform.fee.entity;

import com.trading.platform.domain.Currency;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "fee_rules")
public class FeeRule extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "rule_type", nullable = false)
    public String ruleType; // "TRADING" or "EXCHANGE"

    @Column(name = "symbol")
    public String symbol; // For trading fees (null for exchange)

    @Enumerated(EnumType.STRING)
    @Column(name = "from_currency")
    public Currency fromCurrency; // For exchange fees

    @Enumerated(EnumType.STRING)
    @Column(name = "to_currency")
    public Currency toCurrency; // For exchange fees

    @Column(name = "fixed_fee", precision = 19, scale = 2)
    public BigDecimal fixedFee;

    @Column(name = "percentage_fee", precision = 5, scale = 4)
    public BigDecimal percentageFee;

    // Finder methods
    public static Optional<FeeRule> findTradingFee(String symbol) {
        return find("ruleType = ?1 and symbol = ?2", "TRADING", symbol).firstResultOptional();
    }

    public static Optional<FeeRule> findExchangeFee(Currency from, Currency to) {
        return find("ruleType = ?1 and fromCurrency = ?2 and toCurrency = ?3",
            "EXCHANGE", from, to).firstResultOptional();
    }

    public static List<FeeRule> findAllTradingFees() {
        return find("ruleType", "TRADING").list();
    }

    public static List<FeeRule> findAllExchangeFees() {
        return find("ruleType", "EXCHANGE").list();
    }
}
