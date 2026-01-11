package com.trading.platform.apigateway.client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "trading-service")
@Path("/api/v1/trades")
public interface TradingClient {
    @POST @Path("/buy") Response buy(Object request);
    @POST @Path("/sell") Response sell(Object request);
}
