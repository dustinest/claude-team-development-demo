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
    @Path("/{userId}/buy/amount")
    @Operation(summary = "Execute buy order by dollar amount")
    public Response buyByAmount(@PathParam("userId") UUID userId, BuyByAmountRequest request) {
        try {
            var trade = tradingService.executeBuy(userId, request.symbol,
                request.currency, OrderType.BY_AMOUNT, request.amount);
            return Response.ok(trade).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{userId}/buy/quantity")
    @Operation(summary = "Execute buy order by share quantity")
    public Response buyByQuantity(@PathParam("userId") UUID userId, BuyByQuantityRequest request) {
        try {
            var trade = tradingService.executeBuy(userId, request.symbol,
                request.currency, OrderType.BY_QUANTITY, request.quantity);
            return Response.ok(trade).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{userId}/sell/amount")
    @Operation(summary = "Execute sell order by dollar amount")
    public Response sellByAmount(@PathParam("userId") UUID userId, SellByAmountRequest request) {
        try {
            var trade = tradingService.executeSell(userId, request.symbol,
                request.currency, OrderType.BY_AMOUNT, request.amount);
            return Response.ok(trade).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{userId}/sell/quantity")
    @Operation(summary = "Execute sell order by share quantity")
    public Response sellByQuantity(@PathParam("userId") UUID userId, SellByQuantityRequest request) {
        try {
            var trade = tradingService.executeSell(userId, request.symbol,
                request.currency, OrderType.BY_QUANTITY, request.quantity);
            return Response.ok(trade).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    public static class BuyByAmountRequest {
        public String symbol;
        public BigDecimal amount;
        public Currency currency;
    }

    public static class BuyByQuantityRequest {
        public String symbol;
        public BigDecimal quantity;
        public Currency currency;
    }

    public static class SellByAmountRequest {
        public String symbol;
        public BigDecimal amount;
        public Currency currency;
    }

    public static class SellByQuantityRequest {
        public String symbol;
        public BigDecimal quantity;
        public Currency currency;
    }
}
