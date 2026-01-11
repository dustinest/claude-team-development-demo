package com.trading.platform.transactionhistory.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.platform.domain.TransactionType;
import com.trading.platform.events.*;
import com.trading.platform.transactionhistory.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(EventConsumer.class);

    @Inject
    TransactionService transactionService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("wallet-events-in")
    public void consumeWalletEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.get("eventType").asText();

            if ("DepositCompleted".equals(eventType)) {
                DepositCompletedEvent e = objectMapper.readValue(message, DepositCompletedEvent.class);
                transactionService.recordTransaction(e.getUserId(), TransactionType.DEPOSIT,
                    e.getCurrency(), e.getAmount(), null, null, "Deposit");
            } else if ("WithdrawalCompleted".equals(eventType)) {
                WithdrawalCompletedEvent e = objectMapper.readValue(message, WithdrawalCompletedEvent.class);
                transactionService.recordTransaction(e.getUserId(), TransactionType.WITHDRAWAL,
                    e.getCurrency(), e.getAmount(), null, null, "Withdrawal");
            } else if ("CurrencyExchanged".equals(eventType)) {
                CurrencyExchangedEvent e = objectMapper.readValue(message, CurrencyExchangedEvent.class);
                transactionService.recordTransaction(e.getUserId(), TransactionType.CURRENCY_EXCHANGE,
                    e.getFromCurrency(), e.getFromAmount(), e.getFees(), null,
                    String.format("Exchange %s to %s", e.getFromCurrency(), e.getToCurrency()));
            }
        } catch (Exception ex) {
            LOG.error("Error processing wallet event", ex);
        }
    }

    @Incoming("trading-events-in")
    public void consumeTradingEvent(String message) {
        try {
            TradeCompletedEvent event = objectMapper.readValue(message, TradeCompletedEvent.class);
            TransactionType type = event.getTradeType() == com.trading.platform.domain.TradeType.BUY ?
                TransactionType.BUY : TransactionType.SELL;
            transactionService.recordTransaction(event.getUserId(), type, event.getCurrency(),
                event.getTotalAmount(), event.getFees(), event.getTradeId(),
                String.format("%s %s shares of %s", event.getTradeType(), event.getQuantity(), event.getSymbol()));
        } catch (Exception ex) {
            LOG.error("Error processing trading event", ex);
        }
    }
}
