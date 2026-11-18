package com.scamshield.jobfinder.service;

import java.time.Instant;
import java.util.List;

import com.scamshield.jobfinder.dto.JobResponse;
import com.scamshield.jobfinder.dto.JobSaveRequest;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.SavedJob;
import com.scamshield.jobfinder.provider.ConfiguredJobProvider.JobProviderException;
import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.security.UserPrincipal;

import org.springframework.http.HttpStatusCode;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class SavedJobService {
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobProviderService providerService;

    public SavedJobService(
            JobRepository jobRepository,
            SavedJobRepository savedJobRepository,
            JobProviderService providerService) {
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.providerService = providerService;
    }

    public JobResponse save(UserPrincipal principal, String id, JobSaveRequest request) {
        String userId = principal.getUser().getId();
        Job job = savedJobRepository.findByUserIdAndJobId(userId, id)
                .flatMap(saved -> jobRepository.findById(saved.getJobId()))
                .orElseGet(() -> verifyAndSave(id, request));
        if (!savedJobRepository.existsByUserIdAndJobId(userId, job.getId())) {
            try {
                savedJobRepository.save(new SavedJob(userId, job.getId(), Instant.now()));
            } catch (DuplicateKeyException exception) {
                if (!savedJobRepository.existsByUserIdAndJobId(userId, job.getId())) throw exception;
            }
        }
        return JobResponse.from(job);
    }

    public void unsave(UserPrincipal principal, String id) {
        String userId = principal.getUser().getId();
        SavedJob saved = savedJobRepository.findByUserIdAndJobId(userId, id)
                .orElseThrow(SavedJobNotFoundException::new);
        savedJobRepository.deleteByUserIdAndJobId(userId, id);
        if (savedJobRepository.countByJobId(id) == 0) {
            jobRepository.deleteById(id);
        }
    }

    public List<JobResponse> getSaved(UserPrincipal principal) {
        List<String> ids = savedJobRepository.findTop100ByUserIdOrderBySavedAtDesc(principal.getUser().getId())
                .stream()
                .map(SavedJob::getJobId)
                .toList();
        java.util.Map<String, Job> jobsById = jobRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Job::getId, job -> job));
        return ids.stream()
                .map(jobsById::get)
                .filter(java.util.Objects::nonNull)
                .map(JobResponse::from)
                .toList();
    }

    public Job getSavedJob(UserPrincipal principal, String id) {
        boolean belongsToUser = savedJobRepository.existsByUserIdAndJobId(principal.getUser().getId(), id);
        if (!belongsToUser) throw new SavedJobNotFoundException();
        return jobRepository.findById(id).orElseThrow(SavedJobNotFoundException::new);
    }

    private Job verifyAndSave(String id, JobSaveRequest request) {
        if (id == null || !id.startsWith("adzuna-") || id.length() <= "adzuna-".length()) {
            throw new JobProviderException(HttpStatusCode.valueOf(404), "The job was not found in the live provider.");
        }
        String sourceJobId = id.substring("adzuna-".length());
        Job verified = providerService.findById("Adzuna", sourceJobId, request.keyword(), request.location())
                .orElseThrow(() -> new JobProviderException(
                        HttpStatusCode.valueOf(404),
                        "This listing is no longer available from the job provider. Search again to refresh it."));
        return jobRepository.save(verified);
    }

    public static class SavedJobNotFoundException extends RuntimeException {
        public SavedJobNotFoundException() {
            super("The saved job was not found.");
        }
    }
}
