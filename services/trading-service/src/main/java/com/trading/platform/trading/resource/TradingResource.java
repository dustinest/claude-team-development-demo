package com.trading.platform.trading.resource;

import com.trading.platform.domain.*;
import com.trading.platform.trading.service.TradingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/trades")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Trading", description = "Securities trading operations")
public class TradingResource {
    @Inject
    TradingService tradingService;

    @POST
    @Path("/buy")
    @Operation(summary = "Execute buy order")
    public Response buy(TradeRequest request) {
        try {
            // Validate request
            validateTradeRequest(request);

            // Execute buy based on order type
            var trade = tradingService.executeBuy(
                request.userId,
                request.symbol,
                request.currency,
                request.orderType,
                request.orderType == OrderType.BY_AMOUNT ? request.amount : request.quantity
            );

            return Response.ok(trade).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to execute buy order: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/sell")
    @Operation(summary = "Execute sell order")
    public Response sell(TradeRequest request) {
        try {
            // Validate request
            validateTradeRequest(request);

            // Execute sell based on order type
            var trade = tradingService.executeSell(
                request.userId,
                request.symbol,
                request.currency,
                request.orderType,
                request.orderType == OrderType.BY_AMOUNT ? request.amount : request.quantity
            );

            return Response.ok(trade).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to execute sell order: " + e.getMessage()))
                .build();
        }
    }

    private void validateTradeRequest(TradeRequest request) {
        if (request.userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.symbol == null || request.symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (request.currency == null) {
            throw new IllegalArgumentException("currency is required");
        }
        if (request.orderType == null) {
            throw new IllegalArgumentException("orderType is required");
        }
        if (request.orderType == OrderType.BY_AMOUNT && (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("amount must be positive when orderType is BY_AMOUNT");
        }
        if (request.orderType == OrderType.BY_QUANTITY && (request.quantity == null || request.quantity.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("quantity must be positive when orderType is BY_QUANTITY");
        }
    }

    public static class TradeRequest {
        public UUID userId;
        public String symbol;
        public Currency currency;
        public OrderType orderType;
        public BigDecimal amount;    // Used when orderType = BY_AMOUNT
        public BigDecimal quantity;  // Used when orderType = BY_QUANTITY
    }
}
