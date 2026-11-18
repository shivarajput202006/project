package com.scamshield.notifications;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_preferences")
public class NotificationPreference {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private boolean scamAnalysisEnabled = true;
    private boolean urlScanEnabled = true;
    private boolean jobEnabled = true;
    private boolean resumeEnabled = true;
    private boolean jobMatchEnabled = true;
    private boolean reportEnabled = true;
    private boolean systemEnabled = true;
    private Instant updatedAt;

    protected NotificationPreference() {
    }

    public NotificationPreference(String userId) {
        this.userId = userId;
        this.updatedAt = Instant.now();
    }

    public boolean isEnabled(NotificationType type) {
        return switch (type) {
            case SECURITY, ADMIN -> true;
            case SCAM_ANALYSIS -> scamAnalysisEnabled;
            case URL_SCAN -> urlScanEnabled;
            case JOB -> jobEnabled;
            case RESUME -> resumeEnabled;
            case JOB_MATCH -> jobMatchEnabled;
            case REPORT -> reportEnabled;
            case SYSTEM -> systemEnabled;
        };
    }

    public void update(
            boolean scamAnalysisEnabled,
            boolean urlScanEnabled,
            boolean jobEnabled,
            boolean resumeEnabled,
            boolean jobMatchEnabled,
            boolean reportEnabled,
            boolean systemEnabled) {
        this.scamAnalysisEnabled = scamAnalysisEnabled;
        this.urlScanEnabled = urlScanEnabled;
        this.jobEnabled = jobEnabled;
        this.resumeEnabled = resumeEnabled;
        this.jobMatchEnabled = jobMatchEnabled;
        this.reportEnabled = reportEnabled;
        this.systemEnabled = systemEnabled;
        this.updatedAt = Instant.now();
    }

    public String getUserId() { return userId; }
    public boolean isScamAnalysisEnabled() { return scamAnalysisEnabled; }
    public boolean isUrlScanEnabled() { return urlScanEnabled; }
    public boolean isJobEnabled() { return jobEnabled; }
    public boolean isResumeEnabled() { return resumeEnabled; }
    public boolean isJobMatchEnabled() { return jobMatchEnabled; }
    public boolean isReportEnabled() { return reportEnabled; }
    public boolean isSystemEnabled() { return systemEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
}
