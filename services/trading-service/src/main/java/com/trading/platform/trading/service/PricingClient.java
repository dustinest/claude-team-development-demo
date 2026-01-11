package com.trading.platform.trading.service;

import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.math.BigDecimal;

@RegisterRestClient(configKey = "pricing-service")
@Path("/api/v1/pricing")
public interface PricingClient {
    @GET
    @Path("/securities/{symbol}/price")
    PriceResponse getPrice(@PathParam("symbol") String symbol);

    class PriceResponse {
        public String symbol;
        public BigDecimal price;
    }
}
