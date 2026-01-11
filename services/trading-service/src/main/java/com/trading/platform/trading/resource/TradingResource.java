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
            BigDecimal amountOrQuantity = request.orderType == OrderType.BY_AMOUNT
                ? request.amount : request.quantity;
            var trade = tradingService.executeBuy(request.userId, request.symbol,
                request.currency, request.orderType, amountOrQuantity);
            return Response.ok(trade).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/sell")
    @Operation(summary = "Execute sell order")
    public Response sell(TradeRequest request) {
        try {
            BigDecimal amountOrQuantity = request.orderType == OrderType.BY_AMOUNT
                ? request.amount : request.quantity;
            var trade = tradingService.executeSell(request.userId, request.symbol,
                request.currency, request.orderType, amountOrQuantity);
            return Response.ok(trade).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    public static class TradeRequest {
        public UUID userId;
        public String symbol;
        public Currency currency;
        public OrderType orderType;
        public BigDecimal amount;
        public BigDecimal quantity;
    }
}
