package com.scamshield.notifications;

import java.time.Instant;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.scamshield.notifications.event.NotificationEvent;

@Service
public class NotificationCreationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationCreationService.class);
    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            "/dashboard",
            "/scam-detector",
            "/url-scanner",
            "/live-jobs",
            "/resume-analyzer",
            "/job-matching",
            "/reports",
            "/profile",
            "/settings",
            "/notifications");

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationCreationService(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public void createFromEvent(NotificationEvent event) {
        if (event == null || event.userId() == null || event.userId().isBlank()
                || event.eventKey() == null || event.eventKey().isBlank()) return;
        try {
            NotificationPreference preferences = preferenceRepository.findByUserId(event.userId())
                    .orElseGet(() -> new NotificationPreference(event.userId()));
            if (!preferences.isEnabled(event.type())) return;
            if (notificationRepository.existsByUserIdAndEventKey(event.userId(), event.eventKey())) return;
            String action = safeAction(event.actionUrl());
            notificationRepository.save(new Notification(
                    event.userId(),
                    clean(event.title(), 100),
                    clean(event.message(), 500),
                    event.type(),
                    event.type() == NotificationType.SECURITY
                            ? NotificationPriority.HIGH
                            : event.priority(),
                    action,
                    event.eventKey(),
                    Instant.now()));
        } catch (DuplicateKeyException ignored) {
            // A unique event key makes concurrent event delivery idempotent.
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not persist a notification event of type {}", event.type());
        }
    }

    private String safeAction(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) return null;
        if (actionUrl.startsWith("/") && !actionUrl.startsWith("//") && ALLOWED_ACTIONS.contains(actionUrl)) {
            return actionUrl;
        }
        return null;
    }

    private String clean(String value, int maxLength) {
        if (value == null) return "";
        String cleaned = value.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
