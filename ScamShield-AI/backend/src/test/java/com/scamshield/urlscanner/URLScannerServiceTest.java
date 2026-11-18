package com.scamshield.urlscanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

@ExtendWith(MockitoExtension.class)
class URLScannerServiceTest {
    @Mock
    private URLScanRepository repository;

    @Mock
    private ReputationCheckService reputationCheckService;

    private URLScannerService service;

    @BeforeEach
    void setUp() {
        SafeURLValidator validator = new SafeURLValidator();
        service = new URLScannerService(
                repository,
                new URLValidationService(validator),
                new DomainAnalysisService(),
                reputationCheckService,
                new RedirectAnalysisService());
        when(repository.save(org.mockito.ArgumentMatchers.any(URLScan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void leavesAUrlUnverifiedWhenNoReputationEvidenceExists() {
        when(reputationCheckService.check(anyString())).thenReturn(finding("NOT_CONFIGURED"));

        URLScanResponse response = service.scan(principal(), new URLScanRequest("https://example.com/jobs"));

        assertEquals(URLRiskStatus.UNVERIFIED, response.riskStatus());
        assertEquals(0, response.riskScore());
        assertTrue(response.securityIndicators().isEmpty());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(
                scan -> scan.getUserId().equals("authenticated-user")));
    }

    @Test
    void aProviderNoMatchIsNotTreatedAsProofThatTheUrlIsSafe() {
        when(reputationCheckService.check(anyString())).thenReturn(finding("NO_MATCH"));

        URLScanResponse response = service.scan(principal(), new URLScanRequest("https://example.com/jobs"));

        assertEquals(URLRiskStatus.LOW_RISK, response.riskStatus());
        assertTrue(response.recommendations().stream().anyMatch(text -> text.contains("does not prove")));
    }

    @Test
    void onlyAConfirmedProviderMatchProducesHighRisk() {
        when(reputationCheckService.check(anyString())).thenReturn(finding("CONFIRMED_MATCH"));

        URLScanResponse response = service.scan(principal(), new URLScanRequest("https://example.com/jobs"));

        assertEquals(URLRiskStatus.HIGH_RISK, response.riskStatus());
        assertEquals(95, response.riskScore());
    }

    @Test
    void heuristicSignalsRequestReviewWithoutClaimingAConfirmedThreat() {
        when(reputationCheckService.check(anyString())).thenReturn(finding("NO_MATCH"));

        URLScanResponse response = service.scan(principal(), new URLScanRequest("https://paypa1.example/jobs"));

        assertEquals(URLRiskStatus.NEEDS_REVIEW, response.riskStatus());
        assertTrue(response.riskScore() < 70);
        assertTrue(response.securityIndicators().stream().allMatch(URLRiskIndicator::heuristic));
    }

    private URLScanResponse.ReputationFinding finding(String status) {
        return new URLScanResponse.ReputationFinding(
                "Google Safe Browsing",
                status,
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(),
                "Provider result.");
    }

    private UserPrincipal principal() {
        User user = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        user.setId("authenticated-user");
        return new UserPrincipal(user);
    }
}
