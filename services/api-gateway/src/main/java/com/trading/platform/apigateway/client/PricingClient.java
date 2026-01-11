package com.trading.platform.apigateway.client;
import com.trading.platform.domain.SecurityType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pricing-service")
@Path("/api/v1/pricing/securities")
public interface PricingClient {
    @GET Response getSecurities(@QueryParam("type") SecurityType type);
    @GET @Path("/{symbol}") Response getSecurity(@PathParam("symbol") String symbol);
}
