package com.trading.platform.user.service;

import com.trading.platform.events.UserCreatedEvent;
import com.trading.platform.user.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public UserEntity createUser(UserCreatedEvent event) {
        // Check for duplicates
        if (UserEntity.existsByEmail(event.getEmail())) {
            LOG.warn("User with email {} already exists", event.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        if (UserEntity.existsByUsername(event.getUsername())) {
            LOG.warn("User with username {} already exists", event.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (UserEntity.existsByPhoneNumber(event.getPhoneNumber())) {
            LOG.warn("User with phone number {} already exists", event.getPhoneNumber());
            throw new IllegalArgumentException("Phone number already exists");
        }

        UserEntity user = new UserEntity();
        user.id = event.getUserId();
        user.email = event.getEmail();
        user.username = event.getUsername();
        user.phoneNumber = event.getPhoneNumber();
        user.createdAt = Instant.now();
        user.persist();

        LOG.info("Created user: id={}, email={}, username={}",
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
