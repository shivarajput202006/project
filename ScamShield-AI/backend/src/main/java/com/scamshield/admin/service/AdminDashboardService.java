package com.scamshield.admin.service;

import org.springframework.stereotype.Service;

import com.scamshield.admin.dto.AdminMetricsResponse;
import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.UserRepository;

@Service
public class AdminDashboardService {
    private final UserRepository userRepository;
    private final ScamAnalysisRepository scamAnalysisRepository;
    private final URLScanRepository urlScanRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            ScamAnalysisRepository scamAnalysisRepository,
            URLScanRepository urlScanRepository,
            ResumeAnalysisRepository resumeAnalysisRepository,
            JobMatchRepository jobMatchRepository,
            JobRepository jobRepository) {
        this.userRepository = userRepository;
        this.scamAnalysisRepository = scamAnalysisRepository;
        this.urlScanRepository = urlScanRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.jobRepository = jobRepository;
    }

    public AdminMetricsResponse getMetrics() {
        return new AdminMetricsResponse(
                userRepository.count(),
                null,
                scamAnalysisRepository.count(),
                urlScanRepository.count(),
                resumeAnalysisRepository.count(),
                null,
                jobMatchRepository.count(),
                jobRepository.count());
    }
}
