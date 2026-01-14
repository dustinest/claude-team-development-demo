package com.trading.platform.portfolio.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.platform.events.TradeCompletedEvent;
import com.trading.platform.portfolio.service.PortfolioService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TradeEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(TradeEventConsumer.class);

    @Inject
    PortfolioService portfolioService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("trading-events-in")
    @Blocking
    public void consumeTradeCompletedEvent(String message) {
        try {
            TradeCompletedEvent event = objectMapper.readValue(message, TradeCompletedEvent.class);
            LOG.info("Received TradeCompletedEvent: tradeId={}, userId={}, symbol={}",
                event.getTradeId(), event.getUserId(), event.getSymbol());
            portfolioService.processTradeEvent(event);
        } catch (Exception e) {
            LOG.error("Error processing trade event: {}", e.getMessage(), e);
        }
    }
}
