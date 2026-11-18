package com.scamshield.dashboard;

public record DashboardStats(
        long totalScamAnalyses,
        long urlsScanned,
        long savedJobs,
        long resumeAnalyses,
        long reportsSubmitted) {
    public static DashboardStats empty() {
        return new DashboardStats(0, 0, 0, 0, 0);
    }
}
