package com.trading.platform.portfolio.resource;

import com.trading.platform.portfolio.service.PortfolioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

@Path("/api/v1/portfolios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Portfolio", description = "Portfolio management")
public class PortfolioResource {
    @Inject
    PortfolioService portfolioService;

    @GET
    @Path("/{userId}")
    @Operation(summary = "Get user portfolio")
    public Response getPortfolio(@PathParam("userId") UUID userId) {
        return Response.ok(portfolioService.getPortfolio(userId)).build();
    }
}
