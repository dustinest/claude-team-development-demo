package com.trading.platform.user.resource;

import com.trading.platform.user.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Path("/{userId}")
    @Operation(summary = "Get user by ID")
    public Response getUser(@PathParam("userId") UUID userId) {
        return userService.findById(userId)
            .map(user -> Response.ok(user).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "User not found", "userId", userId))
                .build());
    }

    @GET
    @Operation(summary = "Get all users")
    public Response getAllUsers() {
        return Response.ok(userService.findAll()).build();
    }

    @GET
    @Path("/email/{email}")
    @Operation(summary = "Get user by email")
    public Response getUserByEmail(@PathParam("email") String email) {
        return userService.findByEmail(email)
            .map(user -> Response.ok(user).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/username/{username}")
    @Operation(summary = "Get user by username")
    public Response getUserByUsername(@PathParam("username") String username) {
        return userService.findByUsername(username)
            .map(user -> Response.ok(user).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
