package com.trading.platform.trading.service;

import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.math.BigDecimal;

@RegisterRestClient(configKey = "fee-service")
@Path("/api/v1/fees")
public interface FeeClient {
    @GET
    @Path("/trading/{symbol}")
    FeeResponse getTradingFee(@PathParam("symbol") String symbol, @QueryParam("amount") BigDecimal amount);

    class FeeResponse {
        public String symbol;
        public BigDecimal amount;
        public BigDecimal fee;
    }
}
