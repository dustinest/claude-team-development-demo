package com.trading.platform.user.resource;

import com.trading.platform.user.dto.CreateUserRequest;
import com.trading.platform.user.entity.UserEntity;
import com.trading.platform.user.exception.ConflictException;
import com.trading.platform.user.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management")
public class UserResource {
    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Inject
    UserService userService;

    @POST
    @Operation(summary = "Create new user", description = "Register a new user with email, username, and phone number")
    public Response createUser(CreateUserRequest request) {
        try {
            UserEntity user = userService.createUserDirect(request);
            return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                    "userId", user.id,
                    "email", user.email,
                    "username", user.username
                ))
                .build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Bad request: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (ConflictException e) {
            LOG.warn("Conflict: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

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
