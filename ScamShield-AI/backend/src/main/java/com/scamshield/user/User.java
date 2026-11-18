package com.scamshield.user;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private UserRole role;

    private Instant createdAt;

    private long tokenVersion;

    protected User() {
    }

    public User(String fullName, String email, String passwordHash, UserRole role, Instant createdAt) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    public void updateFullName(String fullName) {
        this.fullName = fullName;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        tokenVersion++;
    }

    public void invalidateSessions() {
        tokenVersion++;
    }
}
