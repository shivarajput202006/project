package com.scamshield.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.scamshield.user.User;
import com.scamshield.user.UserRole;

class JwtServiceTest {
    private final JwtService service = new JwtService(
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=", 3_600_000);

    @Test
    void passwordChangeTokenVersionInvalidatesEarlierTokens() {
        User user = new User("Test User", "user@example.test", "hash", UserRole.USER, Instant.now());
        String oldToken = service.generateToken(user);
        assertTrue(service.isTokenValid(oldToken, user.getEmail(), 0));

        user.updatePasswordHash("new-hash");

        assertFalse(service.isTokenValid(oldToken, user.getEmail(), user.getTokenVersion()));
        String newToken = service.generateToken(user);
        assertTrue(service.isTokenValid(newToken, user.getEmail(), user.getTokenVersion()));
    }
}
