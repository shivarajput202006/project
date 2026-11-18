package com.scamshield.resume.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import com.scamshield.profile.service.PrivacyService;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.resume.dto.ResumeAnalyzeRequest;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.resume.util.ResumeException;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

class ResumeAnalysisServiceTest {
    private final ResumeService resumeService = mock(ResumeService.class);
    private final ResumeRepository resumeRepository = mock(ResumeRepository.class);
    private final ResumeAnalysisRepository analysisRepository = mock(ResumeAnalysisRepository.class);
    private final ResumeExtractionService extractionService = mock(ResumeExtractionService.class);
    private final GeminiResumeService geminiService = mock(GeminiResumeService.class);
    private final ResumeScoringService scoringService = mock(ResumeScoringService.class);
    private final PrivacyService privacyService = mock(PrivacyService.class);
    private final ResumeAnalysisService service = new ResumeAnalysisService(
            resumeService, resumeRepository, analysisRepository, extractionService, geminiService, scoringService,
            privacyService);

    @Test
    void refusesToSendResumeTextToAiWithoutExplicitConsent() {
        assertThrows(
                ResumeException.class,
                () -> service.analyze(null, "resume-id", new ResumeAnalyzeRequest(false)));

        verifyNoInteractions(
                resumeService, resumeRepository, analysisRepository, extractionService, geminiService, privacyService);
    }

    @Test
    void refusesToSendResumeTextWhenAccountPreferenceDisallowsAi() {
        User user = new User(
                "Test User", "user@example.test", "hash", UserRole.USER, java.time.Instant.now());
        user.setId("owner-id");
        UserPrincipal principal = new UserPrincipal(user);
        when(resumeService.authenticatedUserId(principal)).thenReturn("owner-id");
        doThrow(ProfileException.badRequest("AI processing is disabled."))
                .when(privacyService).requireAiProcessingEnabled("owner-id");

        assertThrows(
                ProfileException.class,
                () -> service.analyze(principal, "resume-id", new ResumeAnalyzeRequest(true)));

        verifyNoInteractions(resumeRepository, analysisRepository, extractionService, geminiService);
    }
}
