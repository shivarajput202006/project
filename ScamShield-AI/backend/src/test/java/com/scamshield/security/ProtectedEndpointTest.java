package com.scamshield.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

@SpringBootTest(
        properties = {
            "spring.data.mongodb.uri=mongodb://localhost:27017/scamshield_test",
            "app.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
            "app.auth.cookie-secure=false",
            "app.cors.allowed-origin=http://localhost:5173",
            "app.jobs.adzuna.app-id=",
            "app.jobs.adzuna.app-key="
        })
@AutoConfigureMockMvc
class ProtectedEndpointTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void meEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Authentication is required.")));
    }

    @Test
    void dashboardOverviewRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Authentication is required.")));
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/overview"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists());
    }

    @Test
    void scamDetectorHistoryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/scam-detector/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void jobSearchAndSavedJobsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/jobs/search"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(get("/api/jobs/saved"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(get("/api/jobs/recommendations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void resumeHistoryAndResumeDetailsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/resume/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(get("/api/resume/owned-resume-id"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void jobMatchingEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/job-matching/recommendations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(get("/api/job-matching/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(get("/api/job-matching/provider-job-id"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(post("/api/job-matching/analyze")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(post("/api/job-matching/refresh")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/job-matching/history/match-id")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reportEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reports/categories"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reports/SCAM_DETECTION:record-id"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reports/SCAM_DETECTION:record-id/download"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/reports/SCAM_DETECTION:record-id")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileAndSettingsEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profile/education"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profile/photo"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/profile/data-export")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/profile/me")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"x\",\"confirmation\":\"DELETE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void notificationEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/notifications/0123456789abcdef01234567"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/notifications/preferences"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/notifications/0123456789abcdef01234567/read")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/notifications/0123456789abcdef01234567/unread")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/notifications/read-all")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/notifications/0123456789abcdef01234567")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/notifications")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                        "/api/notifications/preferences")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileRejectsInvalidEducationAndProfileInput() throws Exception {
        var principal = SecurityMockMvcRequestPostProcessors.user(jobPrincipal());
        mockMvc.perform(post("/api/profile/education")
                        .with(principal)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "institutionName": "",
                                  "degree": "BSc",
                                  "course": "Computing",
                                  "startYear": 2025,
                                  "endYear": 2020,
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/profile/me")
                        .with(principal)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("{\"fullName\":\"\",\"email\":\"attacker@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportsRejectInvalidFiltersAndPagination() throws Exception {
        var principal = SecurityMockMvcRequestPostProcessors.user(jobPrincipal());
        mockMvc.perform(get("/api/reports").param("page", "0").with(principal))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reports").param("type", "NOT_A_REPORT").with(principal))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reports").param("size", "51").with(principal))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reports").param("fromDate", "not-a-date").with(principal))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jobMatchingRejectsInvalidPagingAndFilterValues() throws Exception {
        mockMvc.perform(get("/api/job-matching/recommendations")
                        .param("page", "0")
                        .with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/job-matching/recommendations")
                        .param("workMode", "TELEPORT")
                        .with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/job-matching/analyze")
                        .with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content("""
                                {"resumeId":"","page":0,"size":51}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void configuredJobFinderReportsMissingProviderCredentialsWithoutInventingResults() throws Exception {
        mockMvc.perform(get("/api/jobs/search").with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Live job search is unavailable. Configure ADZUNA_APP_ID and ADZUNA_APP_KEY on the backend."));
        mockMvc.perform(get("/api/jobs/filters").with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerAvailable").value(false));
    }

    @Test
    void jobSearchRejectsInvalidPageSizeAndFilterValues() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("size", "51")
                        .with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/jobs/search")
                        .param("workMode", "TELEPORT")
                        .with(SecurityMockMvcRequestPostProcessors.user(jobPrincipal())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void csrfEndpointReturnsTokenAndSetsReadableCsrfCookie() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void registrationWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Morgan Lee",
                                  "email": "morgan@example.com",
                                  "password": "Correct horse 42!",
                                  "confirmPassword": "Correct horse 42!"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Request could not be verified or is not permitted."));
    }

    private UserPrincipal jobPrincipal() {
        User user = new User("Test User", "jobs@example.com", "password-hash", UserRole.USER,
                java.time.Instant.parse("2026-01-01T00:00:00Z"));
        user.setId("job-finder-test-user");
        return new UserPrincipal(user);
    }

    private UserPrincipal adminPrincipal() {
        User user = new User("Admin User", "admin@example.com", "password-hash", UserRole.ADMIN,
                java.time.Instant.parse("2026-01-01T00:00:00Z"));
        user.setId("admin-test-user");
        return new UserPrincipal(user);
    }
}
