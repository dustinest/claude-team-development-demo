package com.trading.platform.apigateway.client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.UUID;

@RegisterRestClient(configKey = "wallet-service")
@Path("/api/v1/wallets")
public interface WalletClient {
    @GET @Path("/{userId}/balances") Response getBalances(@PathParam("userId") UUID userId);
    @POST @Path("/{userId}/deposit") Response deposit(@PathParam("userId") UUID userId, Object request);
    @POST @Path("/{userId}/withdraw") Response withdraw(@PathParam("userId") UUID userId, Object request);
    @POST @Path("/{userId}/exchange") Response exchange(@PathParam("userId") UUID userId, Object request);
}
