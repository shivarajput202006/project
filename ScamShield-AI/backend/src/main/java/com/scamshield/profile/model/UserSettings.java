package com.scamshield.profile.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_settings")
public class UserSettings {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private boolean aiProcessingEnabled;
    private Instant updatedAt;

    protected UserSettings() {
    }

    public UserSettings(String userId, boolean aiProcessingEnabled) {
        this.userId = userId;
        this.aiProcessingEnabled = aiProcessingEnabled;
        this.updatedAt = Instant.now();
    }

    public void setAiProcessingEnabled(boolean enabled) {
        aiProcessingEnabled = enabled;
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public boolean isAiProcessingEnabled() { return aiProcessingEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
}
