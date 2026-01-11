package com.trading.platform.trading.service;

import com.trading.platform.domain.Currency;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.math.BigDecimal;
import java.util.UUID;

@RegisterRestClient(configKey = "wallet-service")
@Path("/api/v1/wallets")
public interface WalletClient {
    @GET
    @Path("/{userId}/balances/{currency}")
    BalanceResponse getBalance(@PathParam("userId") UUID userId, @PathParam("currency") Currency currency);

    default void validateFunds(UUID userId, Currency currency, BigDecimal required) {
        BalanceResponse balance = getBalance(userId, currency);
        if (balance.balance.compareTo(required) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
    }

    class BalanceResponse {
        public UUID userId;
        public Currency currency;
        public BigDecimal balance;
    }
}
