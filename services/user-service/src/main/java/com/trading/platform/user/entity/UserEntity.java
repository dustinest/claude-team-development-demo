package com.trading.platform.user.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(name = "phone_number", unique = true, nullable = false)
    public String phoneNumber;

    @Column(name = "created_at")
    public Instant createdAt;

    // Finder methods
    public static Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public static Optional<UserEntity> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public static Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
        return find("phoneNumber", phoneNumber).firstResultOptional();
    }

    public static boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public static boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public static boolean existsByPhoneNumber(String phoneNumber) {
        return count("phoneNumber", phoneNumber) > 0;
    }
}
