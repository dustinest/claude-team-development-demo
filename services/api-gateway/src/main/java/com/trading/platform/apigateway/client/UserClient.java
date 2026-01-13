package com.trading.platform.apigateway.client;
import com.trading.platform.apigateway.resource.GatewayResource.SignupRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.UUID;

@RegisterRestClient(configKey = "user-service")
@Path("/api/v1/users")
public interface UserClient {
    @POST
    Response createUser(SignupRequest request);

    @GET @Path("/{userId}") Response getUser(@PathParam("userId") UUID userId);
}
