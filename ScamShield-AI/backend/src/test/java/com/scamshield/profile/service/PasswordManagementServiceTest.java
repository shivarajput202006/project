package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.scamshield.profile.dto.ChangePasswordRequest;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;

class PasswordManagementServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PasswordManagementService service =
            new PasswordManagementService(userRepository, passwordEncoder);

    @Test
    void changesPasswordAndIncrementsTokenVersion() {
        User user = user();
        when(userRepository.findById("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Old password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("Strong!Password2026")).thenReturn("new-hash");

        service.change(new UserPrincipal(user),
                new ChangePasswordRequest("Old password", "Strong!Password2026", "Strong!Password2026"));

        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(1, user.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutSaving() {
        User user = user();
        when(userRepository.findById("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("incorrect", "old-hash")).thenReturn(false);

        assertThrows(ProfileException.class, () -> service.change(
                new UserPrincipal(user),
                new ChangePasswordRequest("incorrect", "Strong!Password2026", "Strong!Password2026")));
    }

    private User user() {
        User user = new User("Account", "owner@example.test", "old-hash", UserRole.USER, Instant.now());
        user.setId("owner");
        return user;
    }
}
