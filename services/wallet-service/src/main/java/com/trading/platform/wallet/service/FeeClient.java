package com.trading.platform.wallet.service;

import com.trading.platform.domain.Currency;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.math.BigDecimal;

@RegisterRestClient(configKey = "fee-service")
@Path("/api/v1/fees")
public interface FeeClient {
    @GET
    @Path("/exchange")
    FeeResponse getExchangeFee(@QueryParam("from") Currency from,
                                @QueryParam("to") Currency to,
                                @QueryParam("amount") BigDecimal amount);

    class FeeResponse {
        public Currency from;
        public Currency to;
        public BigDecimal amount;
        public BigDecimal fee;
    }
}
