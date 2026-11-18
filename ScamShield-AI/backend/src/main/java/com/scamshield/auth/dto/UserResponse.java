package com.scamshield.auth.dto;

import java.time.Instant;

import com.scamshield.user.User;
import com.scamshield.user.UserRole;

public record UserResponse(String id, String fullName, String email, UserRole role, Instant createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt());
    }
}
