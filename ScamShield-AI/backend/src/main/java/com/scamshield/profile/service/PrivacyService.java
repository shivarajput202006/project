package com.scamshield.profile.service;

import org.springframework.stereotype.Service;

import com.scamshield.profile.util.ProfileException;

@Service
public class PrivacyService {
    private final SettingsService settingsService;

    public PrivacyService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void requireAiProcessingEnabled(String userId) {
        if (!settingsService.isAiProcessingEnabled(userId)) {
            throw ProfileException.badRequest(
                    "Enable AI processing in Settings before using AI-powered features.");
        }
    }
}
