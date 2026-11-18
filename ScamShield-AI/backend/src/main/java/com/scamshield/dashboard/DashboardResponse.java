package com.scamshield.dashboard;

import java.time.Instant;
import java.util.List;

import com.scamshield.auth.dto.UserResponse;

public record DashboardResponse(
        UserResponse user,
        DashboardStats stats,
        List<RecentActivityResponse> recentActivities,
        List<SavedJobPreview> savedJobs,
        List<NotificationPreview> notifications) {
    public DashboardResponse {
        recentActivities = List.copyOf(recentActivities);
        savedJobs = List.copyOf(savedJobs);
        notifications = List.copyOf(notifications);
    }

    public record SavedJobPreview(String id, String title, String company) {
    }

    public record NotificationPreview(String id, String title, String message, Instant createdAt) {
    }
}
