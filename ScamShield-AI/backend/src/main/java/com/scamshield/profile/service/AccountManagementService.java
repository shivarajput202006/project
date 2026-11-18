package com.scamshield.profile.service;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.scamshield.notifications.NotificationRepository;
import com.scamshield.notifications.NotificationPreferenceRepository;
import com.scamshield.assistant.repository.ConversationRepository;

@Service
public class AccountManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GridFsTemplate gridFsTemplate;
    private final UserProfileRepository profileRepository;
    private final UserSettingsRepository settingsRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ScamAnalysisRepository scamAnalysisRepository;
    private final URLScanRepository urlScanRepository;
    private final JobMatchRepository jobMatchRepository;
    private final SavedJobRepository savedJobRepository;
    private final ReportRepository reportRepository;
    private NotificationRepository notificationRepository;
    private NotificationPreferenceRepository notificationPreferenceRepository;
    private ConversationRepository conversationRepository;

    @Autowired(required = false)
    public void setNotificationRepositories(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            ConversationRepository conversationRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.conversationRepository = conversationRepository;
    }

    public AccountManagementService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            GridFsTemplate gridFsTemplate,
            UserProfileRepository profileRepository,
            UserSettingsRepository settingsRepository,
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository resumeAnalysisRepository,
            ScamAnalysisRepository scamAnalysisRepository,
            URLScanRepository urlScanRepository,
            JobMatchRepository jobMatchRepository,
            SavedJobRepository savedJobRepository,
            ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.gridFsTemplate = gridFsTemplate;
        this.profileRepository = profileRepository;
        this.settingsRepository = settingsRepository;
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.scamAnalysisRepository = scamAnalysisRepository;
        this.urlScanRepository = urlScanRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.savedJobRepository = savedJobRepository;
        this.reportRepository = reportRepository;
    }

    public void deleteAccount(UserPrincipal principal, DeleteAccountRequest request) {
        String ownerId = principal.getUser().getId();
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> ProfileException.notFound("The authenticated account could not be found."));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ProfileException.unauthorized("Current password is incorrect.");
        }
        if (!"DELETE".equals(request.confirmation())) {
            throw ProfileException.badRequest("Type DELETE to confirm permanent account removal.");
        }

        gridFsTemplate.delete(Query.query(Criteria.where("metadata.ownerId").is(ownerId)));
        reportRepository.deleteByOwnerId(ownerId);
        jobMatchRepository.deleteByOwnerId(ownerId);
        resumeAnalysisRepository.deleteByOwnerId(ownerId);
        resumeRepository.deleteByOwnerId(ownerId);
        scamAnalysisRepository.deleteByUserId(ownerId);
        urlScanRepository.deleteByUserId(ownerId);
        savedJobRepository.deleteByUserId(ownerId);
        profileRepository.deleteByUserId(ownerId);
        settingsRepository.deleteByUserId(ownerId);
        if (notificationRepository != null) notificationRepository.deleteByUserId(ownerId);
        if (notificationPreferenceRepository != null) notificationPreferenceRepository.deleteByUserId(ownerId);
        if (conversationRepository != null) conversationRepository.deleteByUserId(ownerId);
        userRepository.delete(user);
    }
}
