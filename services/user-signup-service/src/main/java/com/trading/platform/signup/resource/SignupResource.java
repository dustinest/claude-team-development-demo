package com.trading.platform.signup.resource;

import com.trading.platform.signup.dto.SignupRequest;
import com.trading.platform.signup.service.SignupService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

@Path("/api/v1/signup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Signup", description = "User registration and signup")
public class SignupResource {

    @Inject
    SignupService signupService;

    @POST
    @Operation(summary = "Register new user", description = "Creates a new user and broadcasts user-created event")
    public Response signup(SignupRequest request) {
        try {
            UUID userId = signupService.signup(request);
            return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                    "userId", userId,
                    "email", request.getEmail(),
                    "username", request.getUsername()
                ))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
}
