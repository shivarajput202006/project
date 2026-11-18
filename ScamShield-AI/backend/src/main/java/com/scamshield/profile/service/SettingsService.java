package com.scamshield.profile.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.scamshield.profile.dto.SettingsResponse;
import com.scamshield.profile.dto.UpdateSettingsRequest;
import com.scamshield.profile.model.UserSettings;
import com.scamshield.profile.repository.UserSettingsRepository;

@Service
public class SettingsService {
    private final UserSettingsRepository settingsRepository;

    public SettingsService(UserSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public SettingsResponse get(String userId) {
        return toResponse(getOrCreate(userId));
    }

    public SettingsResponse update(String userId, UpdateSettingsRequest request) {
        UserSettings settings = getOrCreate(userId);
        settings.setAiProcessingEnabled(request.aiProcessingEnabled());
        return toResponse(settingsRepository.save(settings));
    }

    public boolean isAiProcessingEnabled(String userId) {
        return getOrCreate(userId).isAiProcessingEnabled();
    }

    private UserSettings getOrCreate(String userId) {
        return settingsRepository.findByUserId(userId).orElseGet(() -> {
            try {
                return settingsRepository.save(new UserSettings(userId, false));
            } catch (DuplicateKeyException exception) {
                return settingsRepository.findByUserId(userId).orElseThrow(() -> exception);
            }
        });
    }

    private SettingsResponse toResponse(UserSettings settings) {
        return new SettingsResponse(
                settings.isAiProcessingEnabled(),
                "PRIVATE",
                "Resume documents are only accessible to you. AI resume analysis requires this setting "
                        + "and separate confirmation each time you analyze a resume.",
                settings.getUpdatedAt());
    }
}
