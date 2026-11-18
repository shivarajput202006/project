package com.scamshield.scamdetector.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.scamdetector.model.ScamAnalysis;

public interface ScamAnalysisRepository extends MongoRepository<ScamAnalysis, String> {
    List<ScamAnalysis> findTop25ByUserIdOrderByAnalysisTimestampDesc(String userId);

    List<ScamAnalysis> findTop5ByUserIdOrderByAnalysisTimestampDesc(String userId);

    Optional<ScamAnalysis> findTop1ByUserIdOrderByAnalysisTimestampDesc(String userId);

    List<ScamAnalysis> findByUserIdOrderByAnalysisTimestampDesc(String userId);

    long countByUserId(String userId);

    Optional<ScamAnalysis> findByIdAndUserId(String id, String userId);

    long deleteByUserId(String userId);
}
