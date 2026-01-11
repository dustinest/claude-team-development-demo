package com.trading.platform.fee.resource;

import com.trading.platform.domain.Currency;
import com.trading.platform.fee.entity.FeeRule;
import com.trading.platform.fee.service.FeeCalculationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.Map;

@Path("/api/v1/fees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Fees", description = "Fee calculation and management")
public class FeeResource {

    @Inject
    FeeCalculationService feeService;

    @GET
    @Path("/trading/{symbol}")
    @Operation(summary = "Calculate trading fee", description = "Calculate fee for trading a security")
    public Response getTradingFee(
            @PathParam("symbol") String symbol,
            @QueryParam("amount") BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Amount must be positive"))
                .build();
        }

        BigDecimal fee = feeService.calculateTradingFee(symbol, amount);
        return Response.ok(Map.of(
            "symbol", symbol,
            "amount", amount,
            "fee", fee
        )).build();
    }

    @GET
    @Path("/exchange")
    @Operation(summary = "Calculate exchange fee", description = "Calculate fee for currency exchange")
    public Response getExchangeFee(
            @QueryParam("from") Currency from,
            @QueryParam("to") Currency to,
            @QueryParam("amount") BigDecimal amount) {

        if (from == null || to == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "from and to currencies are required"))
                .build();
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Amount must be positive"))
                .build();
        }

        BigDecimal fee = feeService.calculateExchangeFee(from, to, amount);
        return Response.ok(Map.of(
            "from", from,
            "to", to,
            "amount", amount,
            "fee", fee
        )).build();
    }

    @GET
    @Path("/rules/trading")
    @Operation(summary = "Get all trading fee rules")
    public Response getAllTradingFees() {
        return Response.ok(FeeRule.findAllTradingFees()).build();
    }

    @GET
    @Path("/rules/exchange")
    @Operation(summary = "Get all exchange fee rules")
    public Response getAllExchangeFees() {
        return Response.ok(FeeRule.findAllExchangeFees()).build();
    }

    @POST
    @Path("/rules/trading")
    @Operation(summary = "Create trading fee rule")
    public Response createTradingFee(CreateTradingFeeRequest request) {
        FeeRule rule = feeService.createTradingFee(
            request.symbol, request.fixedFee, request.percentageFee);
        return Response.status(Response.Status.CREATED).entity(rule).build();
    }

    @POST
    @Path("/rules/exchange")
    @Operation(summary = "Create exchange fee rule")
    public Response createExchangeFee(CreateExchangeFeeRequest request) {
        FeeRule rule = feeService.createExchangeFee(
            request.from, request.to, request.fixedFee, request.percentageFee);
        return Response.status(Response.Status.CREATED).entity(rule).build();
    }

    public static class CreateTradingFeeRequest {
        public String symbol;
        public BigDecimal fixedFee;
        public BigDecimal percentageFee;
    }

    public static class CreateExchangeFeeRequest {
        public Currency from;
        public Currency to;
        public BigDecimal fixedFee;
        public BigDecimal percentageFee;
    }
}
