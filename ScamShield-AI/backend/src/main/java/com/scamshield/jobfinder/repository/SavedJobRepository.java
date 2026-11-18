package com.scamshield.jobfinder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.jobfinder.model.SavedJob;

public interface SavedJobRepository extends MongoRepository<SavedJob, String> {
    List<SavedJob> findTop5ByUserIdOrderBySavedAtDesc(String userId);

    List<SavedJob> findTop100ByUserIdOrderBySavedAtDesc(String userId);

    List<SavedJob> findByUserIdOrderBySavedAtDesc(String userId);

    Optional<SavedJob> findByUserIdAndJobId(String userId, String jobId);

    boolean existsByUserIdAndJobId(String userId, String jobId);

    long countByUserId(String userId);

    long countByJobId(String jobId);

    long deleteByUserIdAndJobId(String userId, String jobId);

    long deleteByUserId(String userId);
}
