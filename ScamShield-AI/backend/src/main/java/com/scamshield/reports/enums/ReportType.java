package com.scamshield.reports.enums;

public enum ReportType {
    SCAM_DETECTION("Scam detection"),
    URL_SCAN("URL scan"),
    RESUME_ANALYSIS("Resume analysis"),
    JOB_MATCHING("Job matching");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
