package com.scamshield.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationReadServiceTest {
    @Mock private NotificationRepository repository;
    @Mock private MongoTemplate mongoTemplate;
    private NotificationReadService service;

    @BeforeEach
    void setUp() {
        service = new NotificationReadService(repository, mongoTemplate);
    }

    @Test
    void listsOnlyRequestedUsersJobsCategory() {
        when(repository.findByUserIdAndTypeIn(eq("owner"), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        assertTrue(service.list("owner", "JOBS", 0, 20).isEmpty());

        verify(repository).findByUserIdAndTypeIn(
                eq("owner"),
                eq(java.util.List.of(NotificationType.JOB, NotificationType.JOB_MATCH)),
                any(PageRequest.class));
    }

    @Test
    void marksOwnerNotificationAsRead() {
        Notification notification = new Notification(
                "owner", "Title", "Message", NotificationType.SECURITY, NotificationPriority.HIGH,
                "/settings", "SECURITY:1", Instant.now());
        when(repository.findByIdAndUserId("0123456789abcdef01234567", "owner"))
                .thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);

        var response = service.markRead("owner", "0123456789abcdef01234567");

        assertTrue(response.isRead());
        assertTrue(notification.isRead());
        assertTrue(notification.getReadAt() != null);
    }

    @Test
    void hidesNotificationsOwnedByAnotherUser() {
        when(repository.findByIdAndUserId("0123456789abcdef01234567", "owner"))
                .thenReturn(Optional.empty());

        NotificationException exception = assertThrows(
                NotificationException.class, () -> service.get("owner", "0123456789abcdef01234567"));

        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void rejectsMalformedNotificationIdsBeforeDatabaseAccess() {
        NotificationException exception = assertThrows(
                NotificationException.class, () -> service.delete("owner", "not-an-object-id"));

        assertEquals(400, exception.getStatus().value());
        verify(repository, org.mockito.Mockito.never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    void marksAllNotificationsReadForAuthenticatedOwnerOnly() {
        service.markAllRead("owner");

        verify(mongoTemplate).updateMulti(any(), any(), eq(Notification.class));
    }

    @Test
    void preferenceCannotDisableSecurityNotifications() {
        NotificationPreference preference = new NotificationPreference("owner");
        preference.update(false, false, false, false, false, false, false);

        assertTrue(preference.isEnabled(NotificationType.SECURITY));
        assertTrue(preference.isEnabled(NotificationType.ADMIN));
        assertFalse(preference.isEnabled(NotificationType.RESUME));
    }
}
