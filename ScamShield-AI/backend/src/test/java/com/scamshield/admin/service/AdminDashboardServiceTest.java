package com.scamshield.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.UserRepository;

class AdminDashboardServiceTest {
    @Test
    void reportsPersistedPlatformCountsAndLeavesUntrackedMetricsUnavailable() {
        UserRepository users = mock(UserRepository.class);
        ScamAnalysisRepository scamAnalyses = mock(ScamAnalysisRepository.class);
        URLScanRepository urlScans = mock(URLScanRepository.class);
        ResumeAnalysisRepository resumes = mock(ResumeAnalysisRepository.class);
        JobMatchRepository jobMatches = mock(JobMatchRepository.class);
        JobRepository jobs = mock(JobRepository.class);
        when(users.count()).thenReturn(12L);
        when(scamAnalyses.count()).thenReturn(23L);
        when(urlScans.count()).thenReturn(34L);
        when(resumes.count()).thenReturn(45L);
        when(jobMatches.count()).thenReturn(56L);
        when(jobs.count()).thenReturn(67L);

        var metrics = new AdminDashboardService(users, scamAnalyses, urlScans, resumes, jobMatches, jobs)
                .getMetrics();

        assertEquals(12L, metrics.totalUsers());
        assertNull(metrics.activeUsers());
        assertEquals(23L, metrics.scamAnalyses());
        assertEquals(34L, metrics.urlScans());
        assertEquals(45L, metrics.resumeAnalyses());
        assertNull(metrics.jobSearches());
        assertEquals(56L, metrics.jobMatchAnalyses());
        assertEquals(67L, metrics.storedJobListings());
    }
}
