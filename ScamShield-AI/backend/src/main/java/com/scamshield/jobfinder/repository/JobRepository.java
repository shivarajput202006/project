package com.scamshield.jobfinder.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.jobfinder.model.Job;

public interface JobRepository extends MongoRepository<Job, String> {
    Optional<Job> findBySourceNameAndSourceJobId(String sourceName, String sourceJobId);
}
