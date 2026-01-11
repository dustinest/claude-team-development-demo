package com.trading.platform.wallet.resource;

import com.trading.platform.domain.Currency;
import com.trading.platform.wallet.service.WalletService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/wallets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Wallets", description = "Wallet and balance management")
public class WalletResource {
    @Inject
    WalletService walletService;

    @GET
    @Path("/{userId}/balances")
    @Operation(summary = "Get all balances for user")
    public Response getBalances(@PathParam("userId") UUID userId) {
        return Response.ok(walletService.getBalances(userId)).build();
    }

    @GET
    @Path("/{userId}/balances/{currency}")
    @Operation(summary = "Get balance for specific currency")
    public Response getBalance(@PathParam("userId") UUID userId,
                                @PathParam("currency") Currency currency) {
        BigDecimal balance = walletService.getBalance(userId, currency);
        return Response.ok(Map.of("userId", userId, "currency", currency, "balance", balance)).build();
    }

    @POST
    @Path("/{userId}/deposit")
    @Operation(summary = "Deposit funds")
    public Response deposit(@PathParam("userId") UUID userId, DepositRequest request) {
        try {
            var balance = walletService.deposit(userId, request.currency, request.amount);
            return Response.ok(balance).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{userId}/withdraw")
    @Operation(summary = "Withdraw funds")
    public Response withdraw(@PathParam("userId") UUID userId, WithdrawRequest request) {
        try {
            var balance = walletService.withdraw(userId, request.currency, request.amount);
            return Response.ok(balance).build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{userId}/exchange")
    @Operation(summary = "Exchange currency")
    public Response exchange(@PathParam("userId") UUID userId, ExchangeRequest request) {
        try {
            var result = walletService.exchange(userId, request.fromCurrency, request.toCurrency, request.amount);
            return Response.ok(Map.of(
                "fromBalance", result.fromBalance,
                "toBalance", result.toBalance,
                "convertedAmount", result.convertedAmount,
                "exchangeRate", result.exchangeRate,
                "fee", result.fee
            )).build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage())).build();
        }
    }

    public static class DepositRequest {
        public Currency currency;
        public BigDecimal amount;
    }

    public static class WithdrawRequest {
        public Currency currency;
        public BigDecimal amount;
    }

    public static class ExchangeRequest {
        public Currency fromCurrency;
        public Currency toCurrency;
        public BigDecimal amount;
    }
}
