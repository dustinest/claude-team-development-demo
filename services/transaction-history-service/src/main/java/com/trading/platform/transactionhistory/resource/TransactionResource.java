package com.trading.platform.transactionhistory.resource;

import com.trading.platform.domain.TransactionType;
import com.trading.platform.transactionhistory.service.TransactionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

@Path("/api/v1/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transactions", description = "Transaction history")
public class TransactionResource {
    @Inject
    TransactionService transactionService;

    @GET
    @Path("/{userId}")
    @Operation(summary = "Get transaction history")
    public Response getTransactions(@PathParam("userId") UUID userId,
                                     @QueryParam("type") TransactionType type) {
        return Response.ok(transactionService.getTransactions(userId, type)).build();
    }
}
