package com.trading.platform.apigateway.client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.UUID;

@RegisterRestClient(configKey = "portfolio-service")
@Path("/api/v1/portfolios")
public interface PortfolioClient {
    @GET @Path("/{userId}") Response getPortfolio(@PathParam("userId") UUID userId);
}
