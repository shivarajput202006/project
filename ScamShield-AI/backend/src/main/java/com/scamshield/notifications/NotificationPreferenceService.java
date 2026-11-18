package com.scamshield.notifications;

import org.springframework.stereotype.Service;

import com.scamshield.notifications.dto.NotificationPreferenceRequest;

@Service
public class NotificationPreferenceService {
    private final NotificationPreferenceRepository repository;

    public NotificationPreferenceService(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    public NotificationPreferenceRequest get(String userId) {
        return NotificationPreferenceRequest.from(repository.findByUserId(userId)
                .orElseGet(() -> new NotificationPreference(userId)));
    }

    public NotificationPreferenceRequest update(String userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = repository.findByUserId(userId)
                .orElseGet(() -> new NotificationPreference(userId));
        preference.update(
                request.scamAnalysisEnabled(),
                request.urlScanEnabled(),
                request.jobEnabled(),
                request.resumeEnabled(),
                request.jobMatchEnabled(),
                request.reportEnabled(),
                request.systemEnabled());
        return NotificationPreferenceRequest.from(repository.save(preference));
    }
}
