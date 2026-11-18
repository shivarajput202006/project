package com.scamshield.jobmatching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.jobmatching.model.JobMatch;

public interface JobMatchRepository extends MongoRepository<JobMatch, String> {
    Optional<JobMatch> findByOwnerIdAndResumeIdAndSourceNameAndSourceJobId(
            String ownerId, String resumeId, String sourceName, String sourceJobId);

    Optional<JobMatch> findFirstByOwnerIdAndSourceJobIdOrderByAnalyzedAtDesc(String ownerId, String sourceJobId);

    List<JobMatch> findTop100ByOwnerIdAndResumeIdOrderByAnalyzedAtDesc(String ownerId, String resumeId);

    List<JobMatch> findTop100ByOwnerIdOrderByAnalyzedAtDesc(String ownerId);

    List<JobMatch> findByOwnerIdOrderByAnalyzedAtDesc(String ownerId);

    long countByOwnerId(String ownerId);

    Optional<JobMatch> findFirstByOwnerIdOrderByAnalyzedAtDesc(String ownerId);

    Optional<JobMatch> findByIdAndOwnerId(String id, String ownerId);

    void deleteByOwnerIdAndResumeId(String ownerId, String resumeId);

    long deleteByOwnerId(String ownerId);
}
