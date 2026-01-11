package com.trading.platform.exchange.resource;

import com.trading.platform.domain.Currency;
import com.trading.platform.exchange.service.ExchangeRateService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.Map;

@Path("/api/v1/exchange")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Currency Exchange", description = "Currency exchange rates and conversion")
public class ExchangeRateResource {

    @Inject
    ExchangeRateService exchangeRateService;

    @GET
    @Path("/rates")
    @Operation(summary = "Get all exchange rates")
    public Response getAllRates() {
        return Response.ok(exchangeRateService.getAllRates()).build();
    }

    @GET
    @Path("/rates/{from}/{to}")
    @Operation(summary = "Get exchange rate between two currencies")
    public Response getRate(@PathParam("from") Currency from, @PathParam("to") Currency to) {
        return exchangeRateService.getRate(from, to)
            .map(rate -> Response.ok(Map.of("from", from, "to", to, "rate", rate)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/convert")
    @Operation(summary = "Convert amount between currencies")
    public Response convert(ConvertRequest request) {
        BigDecimal result = exchangeRateService.convert(
            request.amount, request.from, request.to);
        if (result != null) {
            return Response.ok(Map.of(
                "from", request.from,
                "to", request.to,
                "originalAmount", request.amount,
                "convertedAmount", result
            )).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    public static class ConvertRequest {
        public BigDecimal amount;
        public Currency from;
        public Currency to;
    }
}
