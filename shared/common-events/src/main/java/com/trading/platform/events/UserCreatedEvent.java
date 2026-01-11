package com.trading.platform.events;

import java.util.UUID;

public class UserCreatedEvent extends BaseEvent {
    private UUID userId;
    private String email;
    private String username;
    private String phoneNumber;

    public UserCreatedEvent() {}

    public UserCreatedEvent(UUID userId, String email, String username, String phoneNumber) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    // Getters and setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
