package com.scamshield.urlscanner;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "url_scans")
@CompoundIndex(name = "user_url_scan_timestamp_idx", def = "{'userId': 1, 'scanTimestamp': -1}")
public class URLScan {
    @Id
    private String id;

    private String userId;
    private String submittedUrl;
    private String normalizedDomain;
    private String scheme;
    private String hostnameType;
    private URLRiskStatus riskStatus;
    private int riskScore;
    private List<URLRiskIndicator> securityIndicators;
    private URLScanResponse.ReputationFinding reputationFinding;
    private URLScanResponse.RedirectSummary redirectSummary;
    private List<String> recommendations;
    private Instant scanTimestamp;

    protected URLScan() {
    }

    public URLScan(
            String userId,
            String submittedUrl,
            String normalizedDomain,
            String scheme,
            String hostnameType,
            URLRiskStatus riskStatus,
            int riskScore,
            List<URLRiskIndicator> securityIndicators,
            URLScanResponse.ReputationFinding reputationFinding,
            URLScanResponse.RedirectSummary redirectSummary,
            List<String> recommendations,
            Instant scanTimestamp) {
        this.userId = userId;
        this.submittedUrl = submittedUrl;
        this.normalizedDomain = normalizedDomain;
        this.scheme = scheme;
        this.hostnameType = hostnameType;
        this.riskStatus = riskStatus;
        this.riskScore = riskScore;
        this.securityIndicators = List.copyOf(securityIndicators);
        this.reputationFinding = reputationFinding;
        this.redirectSummary = redirectSummary;
        this.recommendations = List.copyOf(recommendations);
        this.scanTimestamp = scanTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSubmittedUrl() {
        return submittedUrl;
    }

    public String getNormalizedDomain() {
        return normalizedDomain;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHostnameType() {
        return hostnameType;
    }

    public URLRiskStatus getRiskStatus() {
        return riskStatus;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public List<URLRiskIndicator> getSecurityIndicators() {
        return securityIndicators;
    }

    public URLScanResponse.ReputationFinding getReputationFinding() {
        return reputationFinding;
    }

    public URLScanResponse.RedirectSummary getRedirectSummary() {
        return redirectSummary;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public Instant getScanTimestamp() {
        return scanTimestamp;
    }

    public URLScanResponse toResponse() {
        return new URLScanResponse(
                id,
                submittedUrl,
                normalizedDomain,
                new URLScanResponse.DomainInformation(
                        scheme,
                        hostnameType,
                        "UNAVAILABLE",
                        "Registration details are not queried by this scanner."),
                riskStatus,
                riskScore,
                securityIndicators,
                reputationFinding,
                redirectSummary,
                recommendations,
                scanTimestamp);
    }
}
