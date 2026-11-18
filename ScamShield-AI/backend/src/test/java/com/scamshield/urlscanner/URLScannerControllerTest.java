package com.scamshield.urlscanner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

@SpringBootTest(
        properties = {
            "spring.data.mongodb.uri=mongodb://localhost:27017/scamshield_test",
            "app.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
            "app.auth.cookie-secure=false",
            "app.cors.allowed-origin=http://localhost:5173",
            "app.url-scanner.google-safe-browsing-api-key="
        })
@AutoConfigureMockMvc
class URLScannerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private URLScanRepository repository;

    @MockitoBean
    private ReputationCheckService reputationCheckService;

    @Test
    void authenticatedScanPersistsOnlyTheAuthenticatedUserAndDoesNotFetchSubmittedUrl() throws Exception {
        when(reputationCheckService.check(anyString())).thenReturn(finding("NOT_CONFIGURED"));
        when(repository.save(any(URLScan.class))).thenAnswer(invocation -> {
            URLScan scan = invocation.getArgument(0);
            scan.setId("saved-url-scan");
            return scan;
        });

        mockMvc.perform(post("/api/url-scanner/scan?userId=attacker")
                        .with(user(authenticatedUser("signed-in-user")))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"url\":\"https://example.com/jobs\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("saved-url-scan"))
                .andExpect(jsonPath("$.normalizedDomain").value("example.com"))
                .andExpect(jsonPath("$.riskStatus").value("UNVERIFIED"))
                .andExpect(jsonPath("$.reputationFinding.status").value("NOT_CONFIGURED"))
                .andExpect(jsonPath("$.redirectSummary.status").value("NOT_CHECKED"));

        verify(reputationCheckService).check("https://example.com/jobs");
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(
                scan -> scan.getUserId().equals("signed-in-user")));
    }

    @Test
    void unauthenticatedScanAndHistoryAreRejected() throws Exception {
        mockMvc.perform(post("/api/url-scanner/scan")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/url-scanner/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historyIsScopedToAuthenticatedUser() throws Exception {
        when(repository.findTop25ByUserIdOrderByScanTimestampDesc("signed-in-user")).thenReturn(List.of());

        mockMvc.perform(get("/api/url-scanner/history?userId=another-user")
                        .with(user(authenticatedUser("signed-in-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(repository).findTop25ByUserIdOrderByScanTimestampDesc("signed-in-user");
    }

    @Test
    void crossUserReportIsNotFound() throws Exception {
        when(repository.findByIdAndUserId("private-scan", "signed-in-user")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/url-scanner/private-scan")
                        .with(user(authenticatedUser("signed-in-user"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("URL scan was not found."));

        verify(repository).findByIdAndUserId("private-scan", "signed-in-user");
    }

    @Test
    void userCanDeleteOwnScanButCannotDeleteAnotherUsersScan() throws Exception {
        when(repository.deleteByIdAndUserId("own-scan", "signed-in-user")).thenReturn(1L);
        when(repository.deleteByIdAndUserId("other-scan", "signed-in-user")).thenReturn(0L);

        mockMvc.perform(delete("/api/url-scanner/own-scan")
                        .with(user(authenticatedUser("signed-in-user")))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/url-scanner/other-scan")
                        .with(user(authenticatedUser("signed-in-user")))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(repository).deleteByIdAndUserId("own-scan", "signed-in-user");
        verify(repository).deleteByIdAndUserId("other-scan", "signed-in-user");
    }

    @Test
    void malformedOrPrivateUrlIsRejectedBeforePersistence() throws Exception {
        mockMvc.perform(post("/api/url-scanner/scan")
                        .with(user(authenticatedUser("signed-in-user")))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"url\":\"http://169.254.169.254/latest/meta-data\"}"))
                .andExpect(status().isBadRequest());
    }

    private URLScanResponse.ReputationFinding finding(String status) {
        return new URLScanResponse.ReputationFinding(
                "Google Safe Browsing",
                status,
                null,
                List.of(),
                "No reputation provider is configured.");
    }

    private UserPrincipal authenticatedUser(String id) {
        User user = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        user.setId(id);
        return new UserPrincipal(user);
    }
}
