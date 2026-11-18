package com.scamshield.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.scamshield.admin.dto.AdminSecurityEventResponse;
import com.scamshield.notifications.NotificationRepository;
import com.scamshield.notifications.NotificationType;

@Service
public class AdminMonitoringService {
    private final NotificationRepository notificationRepository;

    public AdminMonitoringService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<AdminSecurityEventResponse> getSecurityEvents() {
        return notificationRepository
                .findTop10ByTypeInOrderByCreatedAtDesc(List.of(NotificationType.SECURITY))
                .stream()
                .map(AdminSecurityEventResponse::from)
                .toList();
    }
}
