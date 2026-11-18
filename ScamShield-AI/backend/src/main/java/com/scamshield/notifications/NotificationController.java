package com.scamshield.notifications;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.notifications.dto.NotificationCountResponse;
import com.scamshield.notifications.dto.NotificationPreferenceRequest;
import com.scamshield.notifications.dto.NotificationResponse;
import com.scamshield.security.UserPrincipal;

@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationReadService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final NotificationRateLimiter rateLimiter;

    public NotificationController(
            NotificationReadService notificationService,
            NotificationPreferenceService preferenceService,
            NotificationRateLimiter rateLimiter) {
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping
    public Page<NotificationResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return notificationService.list(principal.getUser().getId(), filter, page, size);
    }

    @GetMapping("/unread-count")
    public NotificationCountResponse unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.unreadCount(principal.getUser().getId());
    }

    @GetMapping("/{id}")
    public NotificationResponse get(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        return notificationService.get(principal.getUser().getId(), id);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        String userId = principal.getUser().getId();
        rateLimiter.check(userId);
        return notificationService.markRead(userId, id);
    }

    @PatchMapping("/{id}/unread")
    public NotificationResponse markUnread(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        String userId = principal.getUser().getId();
        rateLimiter.check(userId);
        return notificationService.markUnread(userId, id);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal.getUser().getId();
        rateLimiter.check(userId);
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        String userId = principal.getUser().getId();
        rateLimiter.check(userId);
        notificationService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal.getUser().getId();
        rateLimiter.check(userId);
        notificationService.deleteAll(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public NotificationPreferenceRequest preferences(@AuthenticationPrincipal UserPrincipal principal) {
        return preferenceService.get(principal.getUser().getId());
    }

    @PutMapping("/preferences")
    public NotificationPreferenceRequest updatePreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        String userId = principal.getUser().getId();
        rateLimiter.check(userId);
        return preferenceService.update(userId, request);
    }
}
