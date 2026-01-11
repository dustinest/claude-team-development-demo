package com.trading.platform.signup.service;

import com.trading.platform.events.UserCreatedEvent;
import com.trading.platform.signup.dto.SignupRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class SignupService {
    private static final Logger LOG = LoggerFactory.getLogger(SignupService.class);

    @org.eclipse.microprofile.reactive.messaging.Channel("user-events-out")
    io.smallrye.reactive.messaging.MutinyEmitter<UserCreatedEvent> userEventsEmitter;

    public UUID signup(SignupRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Generate UUID for new user
        UUID userId = UUID.randomUUID();

        // Create and publish event
        UserCreatedEvent event = new UserCreatedEvent(
            userId,
            request.getEmail(),
            request.getUsername(),
            request.getPhoneNumber()
        );

        userEventsEmitter.send(event);
        LOG.info("User signup completed: userId={}, email={}, username={}",
            userId, request.getEmail(), request.getUsername());

        return userId;
    }
}
