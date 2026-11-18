package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.profile.dto.ProfileResponse;
import com.scamshield.profile.repository.UserSettingsRepository;
import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.resume.service.ResumeService;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;

class AccountDataExportServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final UserSettingsRepository settingsRepository = mock(UserSettingsRepository.class);
    private final ResumeRepository resumeRepository = mock(ResumeRepository.class);
    private final ResumeAnalysisRepository resumeAnalysisRepository = mock(ResumeAnalysisRepository.class);
    private final ScamAnalysisRepository scamAnalysisRepository = mock(ScamAnalysisRepository.class);
    private final URLScanRepository urlScanRepository = mock(URLScanRepository.class);
    private final JobMatchRepository jobMatchRepository = mock(JobMatchRepository.class);
    private final SavedJobRepository savedJobRepository = mock(SavedJobRepository.class);
    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final ResumeService resumeService = mock(ResumeService.class);
    private final ProfilePhotoService photoService = mock(ProfilePhotoService.class);
    private final AccountDataExportService service = new AccountDataExportService(
            new ObjectMapper().findAndRegisterModules(), userRepository, profileService, settingsRepository,
            resumeRepository, resumeAnalysisRepository, scamAnalysisRepository, urlScanRepository,
            jobMatchRepository, savedJobRepository, reportRepository, resumeService, photoService);

    @Test
    void exportsPrivateUserDataWithoutPasswordHash() throws Exception {
        User user = new User("Owner", "owner@example.test", "secret-hash", UserRole.USER, Instant.now());
        user.setId("owner");
        when(userRepository.findById("owner")).thenReturn(Optional.of(user));
        when(profileService.getForExport(user)).thenReturn(new ProfileResponse(
                "Owner", "owner@example.test", null, null, null, null, null, null,
                List.of(), List.of(), 0));
        when(settingsRepository.findByUserId("owner")).thenReturn(Optional.empty());
        when(resumeRepository.findByOwnerIdOrderByUploadedAtDesc("owner")).thenReturn(List.of());
        when(resumeAnalysisRepository.findByOwnerIdOrderByAnalysisTimestampDesc("owner")).thenReturn(List.of());
        when(scamAnalysisRepository.findByUserIdOrderByAnalysisTimestampDesc("owner")).thenReturn(List.of());
        when(urlScanRepository.findByUserIdOrderByScanTimestampDesc("owner")).thenReturn(List.of());
        when(jobMatchRepository.findByOwnerIdOrderByAnalyzedAtDesc("owner")).thenReturn(List.of());
        when(savedJobRepository.findByUserIdOrderBySavedAtDesc("owner")).thenReturn(List.of());
        when(reportRepository.findByOwnerId("owner")).thenReturn(List.of());

        byte[] archive = service.create("owner");
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            assertTrue(zip.getNextEntry().getName().equals("account-data.json"));
            String json = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(json.contains("owner@example.test"));
            assertFalse(json.contains("secret-hash"));
        }
    }
}
