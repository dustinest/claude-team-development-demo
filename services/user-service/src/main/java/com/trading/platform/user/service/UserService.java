package com.trading.platform.user.service;

import com.trading.platform.events.UserCreatedEvent;
import com.trading.platform.user.dto.CreateUserRequest;
import com.trading.platform.user.entity.UserEntity;
import com.trading.platform.user.exception.ConflictException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@ApplicationScoped
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Inject
    @Channel("user-events-out")
    Emitter<UserCreatedEvent> userEventsEmitter;

    /**
     * Create user directly via REST endpoint (new flow).
     * Validates, persists, and publishes event.
     */
    public UserEntity createUserDirect(CreateUserRequest request) {
        LOG.info("Creating user directly: email={}, username={}", request.getEmail(), request.getUsername());

        // Step 1: Validate input is not null/blank
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Step 2: Validate email format
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            LOG.warn("Invalid email format: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email format");
        }

        // Step 3 & 4: Create and persist user in transaction
        UserEntity user = persistUserInTransaction(request);

        // Step 5: Publish event notification (after successful persistence, outside transaction)
        UserCreatedEvent event = new UserCreatedEvent(
            user.id,
            user.email,
            user.username,
            user.phoneNumber
        );

        LOG.info("Publishing UserCreated event for userId={}", user.id);
        userEventsEmitter.send(event);

        return user;
    }

    @Transactional
    UserEntity persistUserInTransaction(CreateUserRequest request) {
        // Check for duplicates (in same transaction as insert)
        if (UserEntity.existsByEmail(request.getEmail())) {
            LOG.warn("Duplicate email: {}", request.getEmail());
            throw new ConflictException("Email already registered");
        }
        if (UserEntity.existsByUsername(request.getUsername())) {
            LOG.warn("Duplicate username: {}", request.getUsername());
            throw new ConflictException("Username already taken");
        }
        if (UserEntity.existsByPhoneNumber(request.getPhoneNumber())) {
            LOG.warn("Duplicate phone: {}", request.getPhoneNumber());
            throw new ConflictException("Phone number already registered");
        }

        // Create and persist user
        UserEntity user = new UserEntity();
        user.id = UUID.randomUUID();
        user.email = request.getEmail();
        user.username = request.getUsername();
        user.phoneNumber = request.getPhoneNumber();
        user.createdAt = Instant.now();
        user.persist();

        LOG.info("User persisted: id={}, email={}, username={}", user.id, user.email, user.username);
        return user;
    }

    /**
     * Create user from Kafka event (old flow, kept for backward compatibility).
     * This method is idempotent - if user already exists, it returns existing user.
     */
    @Transactional
    public UserEntity createUser(UserCreatedEvent event) {
        // Check if user already exists (idempotent operation)
        Optional<UserEntity> existingUser = UserEntity.findByEmail(event.getEmail());
        if (existingUser.isPresent()) {
            LOG.info("User with email {} already exists (idempotent), skipping creation", event.getEmail());
            return existingUser.get();
        }

        UserEntity user = new UserEntity();
        user.id = event.getUserId();
        user.email = event.getEmail();
        user.username = event.getUsername();
        user.phoneNumber = event.getPhoneNumber();
        user.createdAt = Instant.now();
        user.persist();

        LOG.info("Created user from event: id={}, email={}, username={}",
            user.id, user.email, user.username);

        return user;
    }

    public Optional<UserEntity> findById(UUID userId) {
        return UserEntity.findByIdOptional(userId);
    }

    public Optional<UserEntity> findByEmail(String email) {
        return UserEntity.findByEmail(email);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return UserEntity.findByUsername(username);
    }

    public List<UserEntity> findAll() {
        return UserEntity.listAll();
    }
}
