package com.scamshield.notifications.dto;

import com.scamshield.notifications.NotificationPreference;

public record NotificationPreferenceRequest(
        boolean scamAnalysisEnabled,
        boolean urlScanEnabled,
        boolean jobEnabled,
        boolean resumeEnabled,
        boolean jobMatchEnabled,
        boolean reportEnabled,
        boolean systemEnabled) {
    public static NotificationPreferenceRequest from(NotificationPreference preference) {
        return new NotificationPreferenceRequest(
                preference.isScamAnalysisEnabled(),
                preference.isUrlScanEnabled(),
                preference.isJobEnabled(),
                preference.isResumeEnabled(),
                preference.isJobMatchEnabled(),
                preference.isReportEnabled(),
                preference.isSystemEnabled());
    }
}
