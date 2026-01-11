package com.trading.platform.apigateway.client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "user-signup-service")
@Path("/api/v1/signup")
public interface UserSignupClient {
    @POST Response signup(Object request);
}
