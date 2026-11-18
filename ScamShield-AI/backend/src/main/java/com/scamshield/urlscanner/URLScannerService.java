package com.scamshield.urlscanner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.security.UserPrincipal;
import com.scamshield.notifications.NotificationEventPublisher;
import com.scamshield.notifications.NotificationType;

@Service
public class URLScannerService {
    private final URLScanRepository repository;
    private final URLValidationService validationService;
    private final DomainAnalysisService domainAnalysisService;
    private final ReputationCheckService reputationCheckService;
    private final RedirectAnalysisService redirectAnalysisService;
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired(required = false)
    public void setNotificationEventPublisher(NotificationEventPublisher publisher) {
        this.notificationEventPublisher = publisher;
    }

    public URLScannerService(
            URLScanRepository repository,
            URLValidationService validationService,
            DomainAnalysisService domainAnalysisService,
            ReputationCheckService reputationCheckService,
            RedirectAnalysisService redirectAnalysisService) {
        this.repository = repository;
        this.validationService = validationService;
        this.domainAnalysisService = domainAnalysisService;
        this.reputationCheckService = reputationCheckService;
        this.redirectAnalysisService = redirectAnalysisService;
    }

    public URLScanResponse scan(UserPrincipal principal, URLScanRequest request) {
        SafeURLValidator.ValidatedUrl validated = validationService.validate(request.url());
        DomainAnalysisService.Analysis domainAnalysis = domainAnalysisService.analyze(validated);
        URLScanResponse.ReputationFinding reputation = reputationCheckService.check(validated.submittedUrl());
        List<URLRiskIndicator> indicators = domainAnalysis.indicators();
        int riskScore = indicators.stream()
                .mapToInt(indicator -> "MEDIUM".equals(indicator.severity()) ? 18 : 10)
                .sum();
        riskScore = Math.min(riskScore, 69);

        URLRiskStatus status;
        if ("CONFIRMED_MATCH".equals(reputation.status())) {
            status = URLRiskStatus.HIGH_RISK;
            riskScore = 95;
        } else if (!indicators.isEmpty()) {
            status = URLRiskStatus.NEEDS_REVIEW;
        } else if ("NO_MATCH".equals(reputation.status())) {
            status = URLRiskStatus.LOW_RISK;
        } else {
            status = URLRiskStatus.UNVERIFIED;
        }

        List<String> recommendations = recommendations(validated, reputation, indicators, status);
        URLScan saved = repository.save(new URLScan(
                principal.getUser().getId(),
                validated.submittedUrl(),
                validated.normalizedDomain(),
                validated.scheme(),
                validated.hostnameType(),
                status,
                riskScore,
                indicators,
                reputation,
                redirectAnalysisService.summarize(),
                recommendations,
                Instant.now()));
        if (notificationEventPublisher != null && saved.getId() != null) {
            notificationEventPublisher.publish(
                    principal.getUser().getId(), "URL_SCAN:" + saved.getId(), NotificationType.URL_SCAN,
                    "URL scan complete", "Your URL scan results are ready to review.",
                    "/url-scanner");
        }
        return saved.toResponse();
    }

    public List<URLScanResponse> history(UserPrincipal principal) {
        return repository.findTop25ByUserIdOrderByScanTimestampDesc(principal.getUser().getId())
                .stream()
                .map(URLScan::toResponse)
                .toList();
    }

    public URLScanResponse get(UserPrincipal principal, String id) {
        return repository.findByIdAndUserId(id, principal.getUser().getId())
                .orElseThrow(URLScanNotFoundException::new)
                .toResponse();
    }

    public void delete(UserPrincipal principal, String id) {
        if (repository.deleteByIdAndUserId(id, principal.getUser().getId()) == 0) {
            throw new URLScanNotFoundException();
        }
    }

    private List<String> recommendations(
            SafeURLValidator.ValidatedUrl url,
            URLScanResponse.ReputationFinding reputation,
            List<URLRiskIndicator> indicators,
            URLRiskStatus status) {
        List<String> result = new ArrayList<>();
        if (status == URLRiskStatus.HIGH_RISK) {
            result.add("Do not open this link or enter information on it. The configured reputation provider reported a threat match.");
        } else {
            result.add("Verify the domain using a trusted source before entering passwords, payment details, or personal information.");
        }
        if ("NOT_CONFIGURED".equals(reputation.status())) {
            result.add("No threat-intelligence provider is configured; this result remains unverified.");
        } else if ("UNAVAILABLE".equals(reputation.status()) || "RATE_LIMITED".equals(reputation.status())) {
            result.add("The reputation provider was unavailable or rate-limited; safety could not be confirmed.");
        } else if ("NO_MATCH".equals(reputation.status())) {
            result.add("No reputation match was returned at scan time. This does not prove the URL is safe.");
        }
        if (!indicators.isEmpty()) {
            result.add("Review the listed heuristic indicators; they can be false positives and are not confirmed threats.");
        }
        if ("http".equals(url.scheme())) {
            result.add("This URL uses HTTP without transport encryption; HTTP alone does not mean the site is malicious.");
        }
        result.add("The submitted site was not contacted and its redirects were not followed.");
        return List.copyOf(result);
    }

    public static class URLScanNotFoundException extends RuntimeException {
        public URLScanNotFoundException() {
            super("URL scan was not found.");
        }
    }
}
