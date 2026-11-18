package com.scamshield.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.jobfinder.model.SavedJob;
import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.repository.UserSettingsRepository;
import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.security.JwtService;
import com.scamshield.security.UserPrincipal;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProfileControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository profileRepository;
    @Autowired private UserSettingsRepository settingsRepository;
    @Autowired private SavedJobRepository savedJobRepository;
    @Autowired private ScamAnalysisRepository scamRepository;
    @Autowired private URLScanRepository urlRepository;
    @Autowired private ResumeAnalysisRepository resumeAnalysisRepository;
    @Autowired private ResumeRepository resumeRepository;
    @Autowired private JobMatchRepository jobMatchRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private GridFsTemplate gridFsTemplate;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    private final List<String> testUsers = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (String id : testUsers) {
            gridFsTemplate.delete(Query.query(Criteria.where("metadata.ownerId").is(id)));
            reportRepository.deleteByOwnerId(id);
            jobMatchRepository.deleteByOwnerId(id);
            resumeAnalysisRepository.deleteByOwnerId(id);
            resumeRepository.deleteByOwnerId(id);
            scamRepository.deleteByUserId(id);
            urlRepository.deleteByUserId(id);
            savedJobRepository.deleteByUserId(id);
            profileRepository.deleteByUserId(id);
            settingsRepository.deleteByUserId(id);
            userRepository.deleteById(id);
        }
    }

    @Test
    void editsAuthenticatedProfileEducationAndSkillsWithoutCrossUserAccess() throws Exception {
        User owner = createUser();
        User other = createUser();

        mockMvc.perform(put("/api/profile/me")
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Updated Owner",
                                  "phoneNumber": "+1 555 123 4567",
                                  "city": "Seattle",
                                  "state": "WA",
                                  "country": "US",
                                  "professionalBio": "Backend engineer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Owner"))
                .andExpect(jsonPath("$.email").value(owner.getEmail()));

        MvcResult addedEducation = mockMvc.perform(post("/api/profile/education")
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "institutionName": "Example University",
                                  "degree": "BSc",
                                  "course": "Computer Science",
                                  "specialization": "Systems",
                                  "startYear": 2020,
                                  "endYear": 2024,
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String educationId = objectMapper.readTree(addedEducation.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(put("/api/profile/education/" + educationId)
                        .with(user(new UserPrincipal(other)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "institutionName": "Other University",
                                  "degree": "BA",
                                  "course": "History",
                                  "startYear": 2020,
                                  "endYear": 2024,
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/profile/skills")
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skills":[{"name":"Java","category":"PROGRAMMING_LANGUAGES"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].source").value("USER_PROVIDED"));

        mockMvc.perform(put("/api/profile/skills")
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skills":[
                                  {"name":"Java","category":"PROGRAMMING_LANGUAGES"},
                                  {"name":" java ","category":"FRAMEWORKS"}
                                ]}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/profile/me").with(user(new UserPrincipal(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.education[0].id").value(educationId))
                .andExpect(jsonPath("$.skills[0].name").value("Java"));

        mockMvc.perform(delete("/api/profile/education/" + educationId)
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void profilePhotoIsValidatedPrivateAndExportable() throws Exception {
        User owner = createUser();
        User other = createUser();
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/r7sAAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "ignored.png", "image/png", png);

        mockMvc.perform(multipart("/api/profile/photo")
                        .file(file)
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("/api/profile/photo"));

        mockMvc.perform(get("/api/profile/photo").with(user(new UserPrincipal(owner))))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(png));
        mockMvc.perform(get("/api/profile/photo").with(user(new UserPrincipal(other))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/profile/data-export")
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }

    @Test
    void preferencesPasswordChangeAndAccountDeletionUseRealOwnerData() throws Exception {
        User owner = createUser();
        var auth = user(new UserPrincipal(owner));

        mockMvc.perform(put("/api/settings")
                        .with(auth)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aiProcessingEnabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiProcessingEnabled").value(true))
                .andExpect(jsonPath("$.resumeVisibility").value("PRIVATE"));

        savedJobRepository.save(new SavedJob(owner.getId(), "provider-job", Instant.now()));
        String oldToken = jwtService.generateToken(owner);
        mockMvc.perform(put("/api/profile/password")
                        .with(auth)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "initial-password",
                                  "newPassword": "Strong!Password2026",
                                  "confirmPassword": "Strong!Password2026"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("SCAMSHIELD_AUTH", 0));

        mockMvc.perform(get("/api/auth/me").cookie(new Cookie("SCAMSHIELD_AUTH", oldToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("email", owner.getEmail(), "password", "Strong!Password2026"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/profile/me")
                        .with(user(new UserPrincipal(owner)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("currentPassword", "Strong!Password2026", "confirmation", "DELETE"))))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("SCAMSHIELD_AUTH", 0));

        assertTrue(userRepository.findById(owner.getId()).isEmpty());
        assertTrue(profileRepository.findByUserId(owner.getId()).isEmpty());
        assertTrue(settingsRepository.findByUserId(owner.getId()).isEmpty());
        assertFalse(savedJobRepository.existsByUserIdAndJobId(owner.getId(), "provider-job"));
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        User user = userRepository.save(new User(
                "Initial Name",
                "profile-" + suffix + "@example.test",
                passwordEncoder.encode("initial-password"),
                UserRole.USER,
                Instant.now()));
        testUsers.add(user.getId());
        return user;
    }
}
