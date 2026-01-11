package com.trading.platform.apigateway.resource;

import com.trading.platform.apigateway.client.*;
import com.trading.platform.domain.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GatewayResource {

    @Inject @RestClient UserSignupClient userSignupClient;
    @Inject @RestClient UserClient userClient;
    @Inject @RestClient WalletClient walletClient;
    @Inject @RestClient PricingClient pricingClient;
    @Inject @RestClient TradingClient tradingClient;
    @Inject @RestClient PortfolioClient portfolioClient;
    @Inject @RestClient TransactionClient transactionClient;

    @POST
    @Path("/signup")
    @Tag(name = "Gateway")
    @Operation(summary = "Register new user")
    public Response signup(SignupRequest request) {
        return userSignupClient.signup(request);
    }

    @GET
    @Path("/users/{userId}")
    public Response getUser(@PathParam("userId") UUID userId) {
        return userClient.getUser(userId);
    }

    @GET
    @Path("/wallets/{userId}/balances")
    public Response getBalances(@PathParam("userId") UUID userId) {
        return walletClient.getBalances(userId);
    }

    @POST
    @Path("/wallets/{userId}/deposit")
    public Response deposit(@PathParam("userId") UUID userId, WalletRequest request) {
        return walletClient.deposit(userId, request);
    }

    @POST
    @Path("/wallets/{userId}/withdraw")
    public Response withdraw(@PathParam("userId") UUID userId, WalletRequest request) {
        return walletClient.withdraw(userId, request);
    }

    @POST
    @Path("/wallets/{userId}/exchange")
    public Response exchange(@PathParam("userId") UUID userId, ExchangeRequest request) {
        return walletClient.exchange(userId, request);
    }

    @GET
    @Path("/securities")
    public Response getSecurities(@QueryParam("type") SecurityType type) {
        return pricingClient.getSecurities(type);
    }

    @GET
    @Path("/securities/{symbol}")
    public Response getSecurity(@PathParam("symbol") String symbol) {
        return pricingClient.getSecurity(symbol);
    }

    @POST
    @Path("/trades/buy")
    public Response buy(TradeRequest request) {
        return tradingClient.buy(request);
    }

    @POST
    @Path("/trades/sell")
    public Response sell(TradeRequest request) {
        return tradingClient.sell(request);
    }

    @GET
    @Path("/portfolios/{userId}")
    public Response getPortfolio(@PathParam("userId") UUID userId) {
        return portfolioClient.getPortfolio(userId);
    }

    @GET
    @Path("/transactions/{userId}")
    public Response getTransactions(@PathParam("userId") UUID userId,
                                     @QueryParam("type") TransactionType type) {
        return transactionClient.getTransactions(userId, type);
    }

    public static class SignupRequest {
        public String email;
        public String username;
        public String phoneNumber;
    }

    public static class WalletRequest {
        public Currency currency;
        public BigDecimal amount;
    }

    public static class ExchangeRequest {
        public Currency fromCurrency;
        public Currency toCurrency;
        public BigDecimal amount;
    }

    public static class TradeRequest {
        public UUID userId;
        public String symbol;
        public Currency currency;
        public OrderType orderType;
        public BigDecimal amount;
        public BigDecimal quantity;
    }
}
