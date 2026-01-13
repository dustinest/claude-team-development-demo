package com.trading.platform.user.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.platform.events.UserCreatedEvent;
import com.trading.platform.user.service.UserService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UserEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(UserEventConsumer.class);

    @Inject
    UserService userService;

    @Inject
    ObjectMapper objectMapper;

    @Blocking
    @Incoming("user-events-in")
    public void consumeUserCreatedEvent(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            LOG.info("Received UserCreatedEvent: userId={}, email={}, username={}",
                event.getUserId(), event.getEmail(), event.getUsername());

            userService.createUser(event);
            LOG.info("Successfully processed UserCreatedEvent for userId={}", event.getUserId());
        } catch (Exception e) {
            LOG.error("Error processing UserCreatedEvent: {}", e.getMessage(), e);
            // In production, you might want to send to a dead letter queue
        }
    }
}
