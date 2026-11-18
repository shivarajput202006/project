package com.scamshield.resume.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.resume.model.ResumeAnalysis;

public interface ResumeAnalysisRepository extends MongoRepository<ResumeAnalysis, String> {
    List<ResumeAnalysis> findTop5ByOwnerIdOrderByAnalysisTimestampDesc(String ownerId);

    List<ResumeAnalysis> findByOwnerIdOrderByAnalysisTimestampDesc(String ownerId);

    Optional<ResumeAnalysis> findFirstByOwnerIdOrderByAnalysisTimestampDesc(String ownerId);

    Optional<ResumeAnalysis> findByIdAndOwnerId(String id, String ownerId);

    Optional<ResumeAnalysis> findFirstByResumeIdAndOwnerIdOrderByAnalysisTimestampDesc(
            String resumeId, String ownerId);

    List<ResumeAnalysis> findByOwnerIdAndResumeIdInOrderByAnalysisTimestampDesc(
            String ownerId, Collection<String> resumeIds);

    long countByOwnerId(String ownerId);

    void deleteByResumeIdAndOwnerId(String resumeId, String ownerId);

    long deleteByOwnerId(String ownerId);
}
