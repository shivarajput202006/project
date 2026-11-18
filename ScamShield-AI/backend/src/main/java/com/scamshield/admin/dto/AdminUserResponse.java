package com.scamshield.admin.dto;

import java.time.Instant;

import com.scamshield.user.User;
import com.scamshield.user.UserRole;

public record AdminUserResponse(String id, String fullName, String email, UserRole role, Instant createdAt) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
