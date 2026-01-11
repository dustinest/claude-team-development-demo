package com.trading.platform.portfolio.service;

import com.trading.platform.domain.*;
import com.trading.platform.events.TradeCompletedEvent;
import com.trading.platform.portfolio.entity.Holding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PortfolioService {
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioService.class);

    @Transactional
    public void processTradeEvent(TradeCompletedEvent event) {
        Holding holding = Holding.findByUserAndSymbol(event.getUserId(), event.getSymbol())
            .orElseGet(() -> createNewHolding(event.getUserId(), event.getSymbol(), event.getCurrency()));

        if (event.getTradeType() == TradeType.BUY) {
            processBuy(holding, event.getQuantity(), event.getPricePerUnit());
        } else {
            processSell(holding, event.getQuantity());
        }

        holding.updatedAt = Instant.now();
        holding.persist();

        LOG.info("Updated holding: userId={}, symbol={}, quantity={}, avgPrice={}",
            holding.userId, holding.symbol, holding.quantity, holding.averagePrice);
    }

    private void processBuy(Holding holding, BigDecimal quantity, BigDecimal price) {
        BigDecimal totalCost = holding.quantity.multiply(holding.averagePrice);
        BigDecimal newCost = quantity.multiply(price);
        holding.quantity = holding.quantity.add(quantity);
        holding.averagePrice = totalCost.add(newCost).divide(holding.quantity, 2, RoundingMode.HALF_UP);
    }

    private void processSell(Holding holding, BigDecimal quantity) {
        holding.quantity = holding.quantity.subtract(quantity);
        if (holding.quantity.compareTo(BigDecimal.ZERO) == 0) {
            holding.averagePrice = BigDecimal.ZERO;
        }
    }

    private Holding createNewHolding(UUID userId, String symbol, Currency currency) {
        Holding holding = new Holding();
        holding.userId = userId;
        holding.symbol = symbol;
        holding.currency = currency;
        holding.quantity = BigDecimal.ZERO;
        holding.averagePrice = BigDecimal.ZERO;
        return holding;
    }

    public List<Holding> getPortfolio(UUID userId) {
        return Holding.findByUser(userId);
    }
}
