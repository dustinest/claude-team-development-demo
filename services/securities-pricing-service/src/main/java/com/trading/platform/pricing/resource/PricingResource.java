package com.trading.platform.pricing.resource;

import com.trading.platform.domain.SecurityType;
import com.trading.platform.pricing.service.PricingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/api/v1/pricing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Securities Pricing", description = "Securities pricing and market data")
public class PricingResource {

    @Inject
    PricingService pricingService;

    @GET
    @Path("/securities")
    @Operation(summary = "Get all securities", description = "Returns list of all available securities with current prices")
    public Response getAllSecurities(@QueryParam("type") SecurityType type) {
        if (type != null) {
            return Response.ok(pricingService.getSecuritiesByType(type)).build();
        }
        return Response.ok(pricingService.getAllSecurities()).build();
    }

    @GET
    @Path("/securities/{symbol}")
    @Operation(summary = "Get security by symbol", description = "Returns a single security with current price")
    public Response getSecurity(@PathParam("symbol") String symbol) {
        return pricingService.getSecurity(symbol)
            .map(s -> Response.ok(s).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Security not found", "symbol", symbol))
                .build());
    }

    @GET
    @Path("/securities/{symbol}/price")
    @Operation(summary = "Get current price", description = "Returns current price for a security")
    public Response getCurrentPrice(@PathParam("symbol") String symbol) {
        var price = pricingService.getCurrentPrice(symbol);
        if (price != null) {
            return Response.ok(Map.of("symbol", symbol, "price", price)).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
            .entity(Map.of("error", "Security not found", "symbol", symbol))
            .build();
    }

    @GET
    @Path("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public Response health() {
        return Response.ok(Map.of(
            "status", "UP",
            "service", "securities-pricing-service",
            "securitiesCount", pricingService.getAllSecurities().size()
        )).build();
    }
}
