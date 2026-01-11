package com.trading.platform.apigateway.client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.UUID;

@RegisterRestClient(configKey = "user-service")
@Path("/api/v1/users")
public interface UserClient {
    @GET @Path("/{userId}") Response getUser(@PathParam("userId") UUID userId);
}
