package com.scamshield.resume.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.resume.dto.ResumeAnalyzeRequest;
import com.scamshield.resume.dto.ResumeAnalysisResponse;
import com.scamshield.resume.dto.SkillAnalysisResponse;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.resume.util.ResumeException;
import com.scamshield.profile.service.PrivacyService;
import com.scamshield.security.UserPrincipal;
import com.scamshield.notifications.NotificationEventPublisher;
import com.scamshield.notifications.NotificationType;

@Service
public class ResumeAnalysisService {
    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final ResumeExtractionService extractionService;
    private final GeminiResumeService geminiResumeService;
    private final ResumeScoringService scoringService;
    private final PrivacyService privacyService;
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired(required = false)
    public void setNotificationEventPublisher(NotificationEventPublisher publisher) {
        this.notificationEventPublisher = publisher;
    }

    public ResumeAnalysisService(
            ResumeService resumeService,
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository analysisRepository,
            ResumeExtractionService extractionService,
            GeminiResumeService geminiResumeService,
            ResumeScoringService scoringService,
            PrivacyService privacyService) {
        this.resumeService = resumeService;
        this.resumeRepository = resumeRepository;
        this.analysisRepository = analysisRepository;
        this.extractionService = extractionService;
        this.geminiResumeService = geminiResumeService;
        this.scoringService = scoringService;
        this.privacyService = privacyService;
    }

    public ResumeAnalysisResponse analyze(
            UserPrincipal principal, String resumeId, ResumeAnalyzeRequest request) {
        if (request == null || !request.consentToAiProcessing()) {
            throw ResumeException.badRequest(
                    "Consent is required before sending resume text to the configured AI service.");
        }
        String ownerId = resumeService.authenticatedUserId(principal);
        privacyService.requireAiProcessingEnabled(ownerId);
        var resume = resumeService.getOwnedResume(resumeId, ownerId);
        byte[] content = resumeService.readStoredFile(resume, ownerId);
        ResumeExtractionService.ExtractedDocument extracted =
                extractionService.extract(resume.getFileType(), content);
        var data = geminiResumeService.analyze(extracted.text());
        var score = scoringService.score(data, extracted.text());
        var skillAnalysis = new SkillAnalysisResponse(
                data.technicalSkills(),
                data.softSkills(),
                data.recommendedRoles().stream()
                        .map(role -> new SkillAnalysisResponse.RoleSkillGap(
                                role.title(), role.matchingSkills(), role.missingSkills()))
                        .toList());
        ResumeAnalysis saved = analysisRepository.save(new ResumeAnalysis(
                resumeId, ownerId, data, skillAnalysis, score, Instant.now(), extracted.limitations()));
        resume.markAnalyzed();
        resumeRepository.save(resume);
        if (notificationEventPublisher != null && saved.getId() != null) {
            notificationEventPublisher.publish(
                    ownerId, "RESUME_ANALYSIS:" + saved.getId(), NotificationType.RESUME,
                    "Resume analysis complete", "Your resume analysis is ready to review.",
                    "/resume-analyzer");
        }
        return ResumeAnalysisResponse.from(saved);
    }

    public ResumeAnalysis latest(UserPrincipal principal, String resumeId) {
        String ownerId = resumeService.authenticatedUserId(principal);
        resumeService.getOwnedResume(resumeId, ownerId);
        return analysisRepository.findFirstByResumeIdAndOwnerIdOrderByAnalysisTimestampDesc(resumeId, ownerId)
                .orElseThrow(ResumeException::analysisNotFound);
    }
}
