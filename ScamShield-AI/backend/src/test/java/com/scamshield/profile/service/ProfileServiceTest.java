package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.scamshield.profile.dto.UpdateProfileRequest;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;

class ProfileServiceTest {
    private final UserProfileRepository profileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final ProfileService service = new ProfileService(profileRepository, userRepository);

    @Test
    void updatesUserOwnedProfileAndKeepsAuthenticationFieldsReadOnly() {
        User user = user("owner-id", "before@example.com", UserRole.USER);
        UserProfile profile = new UserProfile("owner-id");
        when(userRepository.findById("owner-id")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.findByUserId("owner-id")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(
                new UserPrincipal(user),
                new UpdateProfileRequest("  Updated Name  ", "+1 555 123 4567", "Seattle", "WA", "US", "Builder"));

        assertEquals("Updated Name", response.fullName());
        assertEquals("before@example.com", response.email());
        assertEquals("Seattle", response.city());
        assertEquals(UserRole.USER, user.getRole());
        verify(userRepository).save(user);
        verify(profileRepository).save(profile);
    }

    private User user(String id, String email, UserRole role) {
        User user = new User("Before", email, "bcrypt-hash", role, Instant.now());
        user.setId(id);
        return user;
    }
}
