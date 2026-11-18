package com.scamshield.scamdetector.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.scamdetector.dto.ScamAnalysisRequest;
import com.scamshield.scamdetector.exception.ScamDetectorException;
import com.scamshield.scamdetector.model.AiAnalysisResult;
import com.scamshield.scamdetector.model.EvidenceItem;
import com.scamshield.scamdetector.model.EvidenceType;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskStatus;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.scamdetector.util.FileValidationUtil;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ScamDetectorServiceTest {
    @Mock
    private ScamAnalysisRepository repository;

    @Mock
    private GeminiAnalysisService geminiAnalysisService;

    @Mock
    private DocumentExtractionService documentExtractionService;

    private ScamDetectorService service;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() throws Exception {
        service = new ScamDetectorService(
                repository,
                geminiAnalysisService,
                documentExtractionService,
                new FileValidationUtil(),
                new EvidenceVerificationService(host -> List.of(InetAddress.getByName("8.8.8.8"))),
                new ObjectMapper());
        User user = new User("Morgan Lee", "morgan@example.com", "hash", UserRole.USER, java.time.Instant.now());
        user.setId("authenticated-user");
        principal = new UserPrincipal(user);
    }

    @Test
    void savesOnlyRedactedReportFieldsUnderTheAuthenticatedUserId() {
        doAnswer(invocation -> invocation.getArgument(0))
                .when(repository)
                .save(any(ScamAnalysis.class));
        when(geminiAnalysisService.analyze(
                        InputType.TEXT, "raw body", null, null, List.of()))
                .thenReturn(new AiAnalysisResult(
                        RiskStatus.NEEDS_REVIEW,
                        40,
                        "Contact recruiter@example.com at +1 555 123 4567.",
                        List.of(),
                        List.of(new EvidenceItem(
                                EvidenceType.USER_PROVIDED,
                                "The email recruiter@example.com requested a fee.",
                                null,
                                null)),
                        "UNVERIFIED",
                        List.of("Verify details independently."),
                        List.of()));

        service.analyzeText(principal, new ScamAnalysisRequest.Text("raw body"));

        ArgumentCaptor<ScamAnalysis> analysisCaptor = ArgumentCaptor.forClass(ScamAnalysis.class);
        verify(repository).save(analysisCaptor.capture());
        ScamAnalysis saved = analysisCaptor.getValue();
        assertEquals("authenticated-user", saved.getUserId());
        assertEquals(InputType.TEXT, saved.getInputType());
        assertEquals("Text message review", saved.getInputSummary());
        assertEquals("UNVERIFIED", saved.getCompanyVerificationStatus());
        org.junit.jupiter.api.Assertions.assertFalse(saved.getSummary().contains("recruiter@example.com"));
        org.junit.jupiter.api.Assertions.assertFalse(saved.getEvidence().get(0).description().contains("recruiter@example.com"));
    }

    @Test
    void scopesReportLookupToAuthenticatedUserAndDoesNotReturnAnotherUsersReport() {
        when(repository.findByIdAndUserId("other-users-analysis", "authenticated-user"))
                .thenReturn(Optional.empty());

        ScamDetectorException exception = assertThrows(
                ScamDetectorException.class,
                () -> service.getReport(principal, "other-users-analysis"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(repository).findByIdAndUserId("other-users-analysis", "authenticated-user");
    }

    @Test
    void doesNotPersistWhenGeminiIsUnavailable() {
        when(geminiAnalysisService.analyze(
                        InputType.TEXT, "raw body", null, null, List.of()))
                .thenThrow(ScamDetectorException.unavailable("AI service unavailable"));

        assertThrows(
                ScamDetectorException.class,
                () -> service.analyzeText(principal, new ScamAnalysisRequest.Text("raw body")));

        verify(repository, never()).save(any());
    }
}
