package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.profile.dto.DeleteAccountRequest;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.repository.UserSettingsRepository;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.user.UserRole;

class AccountManagementServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final GridFsTemplate gridFsTemplate = mock(GridFsTemplate.class);
    private final UserProfileRepository profileRepository = mock(UserProfileRepository.class);
    private final UserSettingsRepository settingsRepository = mock(UserSettingsRepository.class);
    private final ResumeRepository resumeRepository = mock(ResumeRepository.class);
    private final ResumeAnalysisRepository resumeAnalysisRepository = mock(ResumeAnalysisRepository.class);
    private final ScamAnalysisRepository scamAnalysisRepository = mock(ScamAnalysisRepository.class);
    private final URLScanRepository urlScanRepository = mock(URLScanRepository.class);
    private final JobMatchRepository jobMatchRepository = mock(JobMatchRepository.class);
    private final SavedJobRepository savedJobRepository = mock(SavedJobRepository.class);
    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final AccountManagementService service = new AccountManagementService(
            userRepository, passwordEncoder, gridFsTemplate, profileRepository, settingsRepository,
            resumeRepository, resumeAnalysisRepository, scamAnalysisRepository, urlScanRepository,
            jobMatchRepository, savedJobRepository, reportRepository);

    @Test
    void requiresReauthenticationAndExplicitConfirmationBeforePurgingOwnedData() {
        User user = user();
        when(userRepository.findById("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "hash")).thenReturn(true);

        service.deleteAccount(new UserPrincipal(user), new DeleteAccountRequest("current", "DELETE"));

        verify(gridFsTemplate).delete(org.mockito.ArgumentMatchers.any(
                org.springframework.data.mongodb.core.query.Query.class));
        verify(profileRepository).deleteByUserId("owner");
        verify(settingsRepository).deleteByUserId("owner");
        verify(resumeRepository).deleteByOwnerId("owner");
        verify(resumeAnalysisRepository).deleteByOwnerId("owner");
        verify(scamAnalysisRepository).deleteByUserId("owner");
        verify(urlScanRepository).deleteByUserId("owner");
        verify(jobMatchRepository).deleteByOwnerId("owner");
        verify(savedJobRepository).deleteByUserId("owner");
        verify(reportRepository).deleteByOwnerId("owner");
        verify(userRepository).delete(user);
    }

    @Test
    void wrongPasswordOrConfirmationDoesNotStartDeletion() {
        User user = user();
        when(userRepository.findById("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        assertThrows(ProfileException.class, () -> service.deleteAccount(
                new UserPrincipal(user), new DeleteAccountRequest("wrong", "DELETE")));
        verify(userRepository, never()).delete(user);

        when(passwordEncoder.matches("current", "hash")).thenReturn(true);
        assertThrows(ProfileException.class, () -> service.deleteAccount(
                new UserPrincipal(user), new DeleteAccountRequest("current", "NO")));
        verify(userRepository, never()).delete(user);
    }

    private User user() {
        User user = new User("Account", "owner@example.test", "hash", UserRole.USER, Instant.now());
        user.setId("owner");
        return user;
    }
}
