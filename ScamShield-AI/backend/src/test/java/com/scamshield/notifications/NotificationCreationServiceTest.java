package com.scamshield.notifications;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.notifications.event.NotificationEvent;

@ExtendWith(MockitoExtension.class)
class NotificationCreationServiceTest {
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationPreferenceRepository preferenceRepository;
    @InjectMocks private NotificationCreationService service;

    @Test
    void createsSuccessfulEventWithOnlyApprovedInternalAction() {
        when(preferenceRepository.findByUserId("owner")).thenReturn(Optional.empty());
        when(notificationRepository.existsByUserIdAndEventKey("owner", "URL_SCAN:scan-id")).thenReturn(false);

        service.createFromEvent(new NotificationEvent(
                "owner", "URL_SCAN:scan-id", NotificationType.URL_SCAN, NotificationPriority.NORMAL,
                "Scan complete", "Your scan is ready.", "https://attacker.example"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertNull(captor.getValue().getActionUrl());
        assertTrue(captor.getValue().getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void neverCreatesDisabledCategoryNotification() {
        NotificationPreference preferences = new NotificationPreference("owner");
        preferences.update(true, true, true, false, true, true, true);
        when(preferenceRepository.findByUserId("owner")).thenReturn(Optional.of(preferences));

        service.createFromEvent(new NotificationEvent(
                "owner", "RESUME:analysis-id", NotificationType.RESUME, NotificationPriority.NORMAL,
                "Resume complete", "Your analysis is ready.", "/resume-analyzer"));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void skipsAlreadyProcessedEvent() {
        when(preferenceRepository.findByUserId(eq("owner"))).thenReturn(Optional.empty());
        when(notificationRepository.existsByUserIdAndEventKey("owner", "REPORT:report-id")).thenReturn(true);

        service.createFromEvent(new NotificationEvent(
                "owner", "REPORT:report-id", NotificationType.REPORT, NotificationPriority.NORMAL,
                "Report exported", "Your report is ready.", "/reports"));

        verify(notificationRepository, never()).save(any());
    }
}
