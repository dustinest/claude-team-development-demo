package com.trading.platform.signup.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(name = "phone_number", unique = true, nullable = false)
    public String phoneNumber;

    // Query methods for duplicate checking
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
