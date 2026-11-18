package com.scamshield.dashboard;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.auth.dto.UserResponse;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScan;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.SavedJob;
import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.notifications.NotificationRepository;

@Service
public class DashboardService {
    private final ScamAnalysisRepository scamAnalysisRepository;
    private final URLScanRepository urlScanRepository;
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    public void setNotificationRepository(NotificationRepository repository) {
        this.notificationRepository = repository;
    }

    public DashboardService(
            ScamAnalysisRepository scamAnalysisRepository,
            URLScanRepository urlScanRepository,
            JobRepository jobRepository,
            SavedJobRepository savedJobRepository,
            ResumeAnalysisRepository resumeAnalysisRepository) {
        this.scamAnalysisRepository = scamAnalysisRepository;
        this.urlScanRepository = urlScanRepository;
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    public DashboardResponse getOverview(UserPrincipal principal) {
        String userId = principal.getUser().getId();
        List<RecentActivityResponse> scamActivities = scamAnalysisRepository
                .findTop5ByUserIdOrderByAnalysisTimestampDesc(userId)
                .stream()
                .map(analysis -> new RecentActivityResponse(
                        analysis.getId(),
                        "SCAM_ANALYSIS",
                        "Scam analysis · " + analysis.getRiskStatus().name(),
                        analysis.getSummary(),
                        analysis.getAnalysisTimestamp(),
                        analysis.getRiskStatus().name()))
                .toList();
        List<RecentActivityResponse> urlActivities = urlScanRepository
                .findTop5ByUserIdOrderByScanTimestampDesc(userId)
                .stream()
                .map(scan -> new RecentActivityResponse(
                        scan.getId(),
                        "URL_SCAN",
                        "URL scan · " + scan.getRiskStatus().name(),
                        scan.getNormalizedDomain(),
                        scan.getScanTimestamp(),
                        scan.getRiskStatus().name()))
                .toList();
        List<RecentActivityResponse> resumeActivities = resumeAnalysisRepository
                .findTop5ByOwnerIdOrderByAnalysisTimestampDesc(userId)
                .stream()
                .map(analysis -> new RecentActivityResponse(
                        analysis.getId(),
                        "RESUME_ANALYSIS",
                        "Resume analysis completed",
                        "Resume feedback and role suggestions are available.",
                        analysis.getAnalysisTimestamp(),
                        analysis.getScore() != null && analysis.getScore().available()
                                ? "Score available"
                                : "Score unavailable"))
                .toList();
        List<RecentActivityResponse> activities = java.util.stream.Stream
                .concat(
                        java.util.stream.Stream.concat(scamActivities.stream(), urlActivities.stream()),
                        resumeActivities.stream())
                .sorted(java.util.Comparator.comparing(RecentActivityResponse::createdAt).reversed())
                .limit(5)
                .toList();
        List<SavedJob> savedEntries = savedJobRepository.findTop5ByUserIdOrderBySavedAtDesc(userId);
        List<DashboardResponse.NotificationPreview> notifications = notificationRepository == null
                ? List.of()
                : notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(notification -> new DashboardResponse.NotificationPreview(
                                notification.getId(),
                                notification.getTitle(),
                                notification.getMessage(),
                                notification.getCreatedAt()))
                        .toList();
        java.util.Map<String, Job> jobsById = jobRepository.findAllById(
                        savedEntries.stream().map(SavedJob::getJobId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(Job::getId, job -> job));
        List<DashboardResponse.SavedJobPreview> savedJobs = savedEntries.stream()
                .map(saved -> jobsById.get(saved.getJobId()))
                .filter(java.util.Objects::nonNull)
                .map(job -> new DashboardResponse.SavedJobPreview(job.getId(), job.getTitle(), job.getCompany()))
                .toList();
        return new DashboardResponse(
                UserResponse.from(principal.getUser()),
                new DashboardStats(
                        scamAnalysisRepository.countByUserId(userId),
                        urlScanRepository.countByUserId(userId),
                        savedJobRepository.countByUserId(userId),
                        resumeAnalysisRepository.countByOwnerId(userId),
                        0),
                activities,
                savedJobs,
                notifications);
    }
}
