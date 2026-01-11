package com.trading.platform.wallet.service;

import com.trading.platform.domain.Currency;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.math.BigDecimal;

@RegisterRestClient(configKey = "exchange-rate-service")
@Path("/api/v1/exchange")
public interface ExchangeRateClient {
    @GET
    @Path("/rates/{from}/{to}")
    ExchangeRateResponse getRate(@PathParam("from") Currency from, @PathParam("to") Currency to);

    class ExchangeRateResponse {
        public Currency from;
        public Currency to;
        public BigDecimal rate;
    }
}
