package com.trading.platform.pricing.service;

import com.trading.platform.domain.SecurityType;
import com.trading.platform.pricing.entity.Security;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PricingService {
    private static final Logger LOG = LoggerFactory.getLogger(PricingService.class);

    private final Map<String, Security> securities = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        LOG.info("Initializing securities pricing service with mock data");

        // Initialize 10 stocks
        addSecurity("AAPL", "Apple Inc.", SecurityType.STOCK, new BigDecimal("175.50"));
        addSecurity("GOOGL", "Alphabet Inc.", SecurityType.STOCK, new BigDecimal("140.25"));
        addSecurity("MSFT", "Microsoft Corp.", SecurityType.STOCK, new BigDecimal("380.00"));
        addSecurity("AMZN", "Amazon.com Inc.", SecurityType.STOCK, new BigDecimal("155.75"));
        addSecurity("TSLA", "Tesla Inc.", SecurityType.STOCK, new BigDecimal("245.30"));
        addSecurity("META", "Meta Platforms", SecurityType.STOCK, new BigDecimal("485.00"));
        addSecurity("NVDA", "NVIDIA Corp.", SecurityType.STOCK, new BigDecimal("495.25"));
        addSecurity("JPM", "JPMorgan Chase", SecurityType.STOCK, new BigDecimal("180.50"));
        addSecurity("JNJ", "Johnson & Johnson", SecurityType.STOCK, new BigDecimal("160.00"));
        addSecurity("V", "Visa Inc.", SecurityType.STOCK, new BigDecimal("275.75"));

        // Initialize 5 stock indexes
        addSecurity("SPY", "S&P 500 ETF", SecurityType.STOCK_INDEX, new BigDecimal("480.00"));
        addSecurity("QQQ", "NASDAQ 100 ETF", SecurityType.STOCK_INDEX, new BigDecimal("410.50"));
        addSecurity("DIA", "Dow Jones ETF", SecurityType.STOCK_INDEX, new BigDecimal("380.25"));
        addSecurity("IWM", "Russell 2000 ETF", SecurityType.STOCK_INDEX, new BigDecimal("198.75"));
        addSecurity("VTI", "Total Market ETF", SecurityType.STOCK_INDEX, new BigDecimal("245.00"));

        // Initialize 5 bond indexes
        addSecurity("AGG", "US Aggregate Bond", SecurityType.BOND_INDEX, new BigDecimal("102.50"));
        addSecurity("TLT", "20+ Year Treasury", SecurityType.BOND_INDEX, new BigDecimal("95.75"));
        addSecurity("BND", "Total Bond Market", SecurityType.BOND_INDEX, new BigDecimal("78.25"));
        addSecurity("LQD", "Investment Grade Corp", SecurityType.BOND_INDEX, new BigDecimal("110.00"));
        addSecurity("HYG", "High Yield Corp", SecurityType.BOND_INDEX, new BigDecimal("82.50"));

        LOG.info("Initialized {} securities", securities.size());
    }

    private void addSecurity(String symbol, String name, SecurityType type, BigDecimal price) {
        Security security = new Security(symbol, name, type, price);
        securities.put(symbol, security);
        LOG.debug("Added security: {} - {} at ${}", symbol, name, price);
    }

    @Scheduled(every = "30s")
    public void updatePrices() {
        LOG.debug("Updating security prices");
        int updatedCount = 0;

        for (Security security : securities.values()) {
            // Fluctuate ±2%
            double fluctuation = 1.0 + (random.nextDouble() * 0.04 - 0.02);
            BigDecimal newPrice = security.getCurrentPrice()
                .multiply(BigDecimal.valueOf(fluctuation))
                .setScale(2, RoundingMode.HALF_UP);

            // Update high/low
            if (newPrice.compareTo(security.getHighPrice()) > 0) {
                security.setHighPrice(newPrice);
            }
            if (newPrice.compareTo(security.getLowPrice()) < 0) {
                security.setLowPrice(newPrice);
            }

            security.setCurrentPrice(newPrice);
            security.setLastUpdated(Instant.now());
            updatedCount++;
        }

        LOG.debug("Updated {} security prices", updatedCount);
    }

    public List<Security> getAllSecurities() {
        return new ArrayList<>(securities.values());
    }

    public Optional<Security> getSecurity(String symbol) {
        return Optional.ofNullable(securities.get(symbol.toUpperCase()));
    }

    public List<Security> getSecuritiesByType(SecurityType type) {
        return securities.values().stream()
            .filter(s -> s.getType() == type)
            .toList();
    }

    public BigDecimal getCurrentPrice(String symbol) {
        return getSecurity(symbol)
            .map(Security::getCurrentPrice)
            .orElse(null);
    }
}
