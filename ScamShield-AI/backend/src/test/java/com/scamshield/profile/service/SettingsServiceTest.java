package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.scamshield.profile.dto.UpdateSettingsRequest;
import com.scamshield.profile.model.UserSettings;
import com.scamshield.profile.repository.UserSettingsRepository;

class SettingsServiceTest {
    private final UserSettingsRepository repository = mock(UserSettingsRepository.class);
    private final SettingsService service = new SettingsService(repository);

    @Test
    void defaultsAiProcessingOffAndPersistsUserPreference() {
        AtomicReference<UserSettings> stored = new AtomicReference<>();
        when(repository.findByUserId("owner")).thenAnswer(invocation -> Optional.ofNullable(stored.get()));
        when(repository.save(any(UserSettings.class))).thenAnswer(invocation -> {
            UserSettings settings = invocation.getArgument(0);
            stored.set(settings);
            return settings;
        });

        assertFalse(service.get("owner").aiProcessingEnabled());
        assertTrue(service.update("owner", new UpdateSettingsRequest(true)).aiProcessingEnabled());
        assertTrue(service.isAiProcessingEnabled("owner"));
    }
}
