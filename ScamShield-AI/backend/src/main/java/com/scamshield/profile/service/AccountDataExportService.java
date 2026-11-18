package com.scamshield.profile.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.jobfinder.model.SavedJob;
import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.profile.dto.ProfileResponse;
import com.scamshield.profile.dto.SettingsResponse;
import com.scamshield.profile.repository.UserSettingsRepository;
import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.resume.model.Resume;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.resume.service.ResumeService;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;
import com.scamshield.assistant.repository.ConversationRepository;
import com.scamshield.notifications.NotificationRepository;
import com.scamshield.notifications.NotificationPreferenceRepository;

@Service
public class AccountDataExportService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final UserSettingsRepository settingsRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ScamAnalysisRepository scamAnalysisRepository;
    private final URLScanRepository urlScanRepository;
    private final JobMatchRepository jobMatchRepository;
    private final SavedJobRepository savedJobRepository;
    private final ReportRepository reportRepository;
    private final ResumeService resumeService;
    private final ProfilePhotoService photoService;
    private ConversationRepository conversationRepository;
    private NotificationRepository notificationRepository;
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired(required = false)
    public void setAssistantAndNotificationRepositories(
            ConversationRepository conversationRepository,
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository notificationPreferenceRepository) {
        this.conversationRepository = conversationRepository;
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    public AccountDataExportService(
            ObjectMapper objectMapper,
            UserRepository userRepository,
            ProfileService profileService,
            UserSettingsRepository settingsRepository,
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository resumeAnalysisRepository,
            ScamAnalysisRepository scamAnalysisRepository,
            URLScanRepository urlScanRepository,
            JobMatchRepository jobMatchRepository,
            SavedJobRepository savedJobRepository,
            ReportRepository reportRepository,
            ResumeService resumeService,
            ProfilePhotoService photoService) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.settingsRepository = settingsRepository;
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.scamAnalysisRepository = scamAnalysisRepository;
        this.urlScanRepository = urlScanRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.savedJobRepository = savedJobRepository;
        this.reportRepository = reportRepository;
        this.resumeService = resumeService;
        this.photoService = photoService;
    }

    public byte[] create(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated account could not be found."));
        ProfileResponse profile = profileService.getForExport(user);
        SettingsResponse settings = settingsRepository.findByUserId(userId)
                .map(value -> new SettingsResponse(
                        value.isAiProcessingEnabled(),
                        "PRIVATE",
                        "Resume documents are only accessible to the account owner.",
                        value.getUpdatedAt()))
                .orElse(new SettingsResponse(false, "PRIVATE", "Resume documents are private.", null));
        List<Resume> resumes = resumeRepository.findByOwnerIdOrderByUploadedAtDesc(userId);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("exportedAt", Instant.now());
        metadata.put("profile", profile);
        metadata.put("settings", settings);
        metadata.put("resumeFiles", resumes.stream().map(resume -> Map.of(
                "id", resume.getId(),
                "filename", resume.getOriginalFilename(),
                "fileType", resume.getFileType(),
                "uploadedAt", resume.getUploadedAt(),
                "processingStatus", resume.getProcessingStatus())).toList());
        metadata.put("resumeAnalyses", resumeAnalysisRepository.findByOwnerIdOrderByAnalysisTimestampDesc(userId));
        metadata.put("scamAnalyses", scamAnalysisRepository.findByUserIdOrderByAnalysisTimestampDesc(userId));
        metadata.put("urlScans", urlScanRepository.findByUserIdOrderByScanTimestampDesc(userId));
        metadata.put("jobMatches", jobMatchRepository.findByOwnerIdOrderByAnalyzedAtDesc(userId));
        List<SavedJob> savedJobs = savedJobRepository.findByUserIdOrderBySavedAtDesc(userId);
        metadata.put("savedJobs", savedJobs);
        metadata.put("reportHistory", reportRepository.findByOwnerId(userId));
        if (conversationRepository != null) {
            metadata.put("assistantConversations", conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId));
        }
        if (notificationRepository != null) {
            metadata.put("notifications", notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
        }
        if (notificationPreferenceRepository != null) {
            metadata.put("notificationPreferences", notificationPreferenceRepository.findByUserId(userId).orElse(null));
        }

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ZipOutputStream archive = new ZipOutputStream(bytes)) {
            addEntry(archive, "account-data.json", objectMapper.writeValueAsBytes(metadata));
            for (Resume resume : resumes) {
                String extension = resume.getFileType().name().toLowerCase();
                addEntry(
                        archive,
                        "resumes/" + resume.getId() + "." + extension,
                        resumeService.readStoredFile(resume, userId));
            }
            if (profile.photoUrl() != null) {
                ProfilePhotoService.PhotoContent photo = photoService.get(userId);
                String extension = switch (photo.contentType()) {
                    case "image/jpeg" -> "jpg";
                    case "image/png" -> "png";
                    case "image/webp" -> "webp";
                    default -> throw new IllegalStateException("Stored profile photo has an unsupported format.");
                };
                addEntry(archive, "profile/photo." + extension, photo.bytes());
            }
            archive.finish();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("The account data export could not be generated.", exception);
        }
    }

    private void addEntry(ZipOutputStream archive, String name, byte[] content) throws IOException {
        archive.putNextEntry(new ZipEntry(name));
        archive.write(content);
        archive.closeEntry();
    }
}
