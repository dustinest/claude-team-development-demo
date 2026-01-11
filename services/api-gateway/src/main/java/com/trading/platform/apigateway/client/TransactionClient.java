package com.trading.platform.apigateway.client;
import com.trading.platform.domain.TransactionType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.UUID;

@RegisterRestClient(configKey = "transaction-service")
@Path("/api/v1/transactions")
public interface TransactionClient {
    @GET @Path("/{userId}") Response getTransactions(@PathParam("userId") UUID userId, @QueryParam("type") TransactionType type);
}
