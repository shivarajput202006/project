package com.scamshield.scamdetector.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.scamdetector.dto.ScamAnalysisRequest;
import com.scamshield.scamdetector.dto.ScamAnalysisResponse;
import com.scamshield.scamdetector.exception.ScamDetectorException;
import com.scamshield.scamdetector.model.AiAnalysisResult;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.scamdetector.util.DetectedFileType;
import com.scamshield.scamdetector.util.FileValidationUtil;
import com.scamshield.security.UserPrincipal;
import com.scamshield.notifications.NotificationEventPublisher;
import com.scamshield.notifications.NotificationType;

@Service
public class ScamDetectorService {
    private static final Pattern EMAIL_ADDRESS =
            Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE_NUMBER =
            Pattern.compile("(?<!\\w)\\+?\\d[\\d\\s().-]{7,}\\d(?!\\w)");

    private final ScamAnalysisRepository repository;
    private final GeminiAnalysisService geminiAnalysisService;
    private final DocumentExtractionService documentExtractionService;
    private final FileValidationUtil fileValidationUtil;
    private final EvidenceVerificationService evidenceVerificationService;
    private final ObjectMapper objectMapper;
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired(required = false)
    public void setNotificationEventPublisher(NotificationEventPublisher publisher) {
        this.notificationEventPublisher = publisher;
    }

    public ScamDetectorService(
            ScamAnalysisRepository repository,
            GeminiAnalysisService geminiAnalysisService,
            DocumentExtractionService documentExtractionService,
            FileValidationUtil fileValidationUtil,
            EvidenceVerificationService evidenceVerificationService,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.geminiAnalysisService = geminiAnalysisService;
        this.documentExtractionService = documentExtractionService;
        this.fileValidationUtil = fileValidationUtil;
        this.evidenceVerificationService = evidenceVerificationService;
        this.objectMapper = objectMapper;
    }

    public ScamAnalysisResponse analyzeText(UserPrincipal principal, ScamAnalysisRequest.Text request) {
        return analyze(
                principal,
                InputType.TEXT,
                "Text message review",
                request.text(),
                null,
                null,
                List.of());
    }

    public ScamAnalysisResponse analyzeEmail(
            UserPrincipal principal, ScamAnalysisRequest.EmailMessage request) {
        String content = jsonContent(request);
        return analyze(
                principal,
                InputType.EMAIL,
                "Email review",
                content,
                null,
                null,
                List.of());
    }

    public ScamAnalysisResponse analyzeUrl(UserPrincipal principal, ScamAnalysisRequest.Url request) {
        EvidenceVerificationService.UrlVerification verification =
                evidenceVerificationService.verifyUrl(request.url());
        return analyze(
                principal,
                InputType.URL,
                "Link submitted for review",
                verification.normalizedUrl(),
                null,
                null,
                verification.limitations());
    }

    public ScamAnalysisResponse analyzeFile(UserPrincipal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ScamDetectorException.badRequest("Choose a non-empty image, PDF, or DOCX file.");
        }
        if (file.getSize() > FileValidationUtil.MAX_FILE_BYTES) {
            throw ScamDetectorException.badRequest("Files must be 5 MB or smaller.");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw ScamDetectorException.badRequest("The uploaded file could not be read.");
        }
        DetectedFileType detectedType =
                fileValidationUtil.validate(file.getOriginalFilename(), content);
        if (detectedType.inputType() == InputType.IMAGE) {
            return analyze(
                    principal,
                    InputType.IMAGE,
                    "Image submitted for review",
                    "Analyze this user-provided image for job or recruitment scam warning signs.",
                    detectedType.mimeType(),
                    content,
                    List.of());
        }

        DocumentExtractionService.ExtractedDocument extracted =
                documentExtractionService.extract(detectedType, content);
        return analyze(
                principal,
                detectedType.inputType(),
                detectedType == DetectedFileType.PDF
                        ? "PDF document review"
                        : "DOCX document review",
                extracted.text(),
                null,
                null,
                extracted.limitations());
    }

    public List<ScamAnalysisResponse> getHistory(UserPrincipal principal) {
        String userId = authenticatedUserId(principal);
        return repository.findTop25ByUserIdOrderByAnalysisTimestampDesc(userId)
                .stream()
                .map(ScamAnalysisResponse::from)
                .toList();
    }

    public ScamAnalysisResponse getReport(UserPrincipal principal, String analysisId) {
        String userId = authenticatedUserId(principal);
        ScamAnalysis report = repository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(ScamDetectorException::notFound);
        return ScamAnalysisResponse.from(report);
    }

    private ScamAnalysisResponse analyze(
            UserPrincipal principal,
            InputType inputType,
            String inputSummary,
            String submittedText,
            String imageMimeType,
            byte[] imageContent,
            List<String> limitations) {
        String userId = authenticatedUserId(principal);
        AiAnalysisResult result = redactSensitiveDetails(geminiAnalysisService.analyze(
                inputType, submittedText, imageMimeType, imageContent, limitations));
        ScamAnalysis saved = repository.save(new ScamAnalysis(
                userId,
                inputType,
                inputSummary,
                result,
                Instant.now()));
        if (notificationEventPublisher != null && saved.getId() != null) {
            notificationEventPublisher.publish(
                    userId, "SCAM_ANALYSIS:" + saved.getId(), NotificationType.SCAM_ANALYSIS,
                    "Scam analysis complete", "Your scam analysis is ready to review.",
                    "/scam-detector");
        }
        return ScamAnalysisResponse.from(saved);
    }

    private String authenticatedUserId(UserPrincipal principal) {
        String userId = principal == null || principal.getUser() == null
                ? null
                : principal.getUser().getId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Authenticated user has no database identity.");
        }
        return userId;
    }

    private String jsonContent(ScamAnalysisRequest.EmailMessage request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not encode the validated email input.", exception);
        }
    }

    private AiAnalysisResult redactSensitiveDetails(AiAnalysisResult result) {
        return new AiAnalysisResult(
                result.riskStatus(),
                result.riskScore(),
                redact(result.summary()),
                result.indicators().stream()
                        .map(indicator -> new com.scamshield.scamdetector.model.RiskIndicator(
                                redact(indicator.category()),
                                redact(indicator.title()),
                                redact(indicator.details()),
                                indicator.evidenceType()))
                        .toList(),
                result.evidence().stream()
                        .map(item -> new com.scamshield.scamdetector.model.EvidenceItem(
                                item.evidenceType(),
                                redact(item.description()),
                                null,
                                null))
                        .toList(),
                "UNVERIFIED",
                result.recommendations().stream().map(this::redact).toList(),
                result.limitations().stream().map(this::redact).toList());
    }

    private String redact(String value) {
        return PHONE_NUMBER.matcher(EMAIL_ADDRESS.matcher(value).replaceAll("[email redacted]"))
                .replaceAll("[phone redacted]");
    }
}
