package com.scamshield.resume.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.resume.model.Resume;

public interface ResumeRepository extends MongoRepository<Resume, String> {
    Optional<Resume> findByIdAndOwnerId(String id, String ownerId);

    List<Resume> findTop25ByOwnerIdOrderByUploadedAtDesc(String ownerId);

    List<Resume> findByOwnerIdOrderByUploadedAtDesc(String ownerId);

    void deleteByIdAndOwnerId(String id, String ownerId);

    long deleteByOwnerId(String ownerId);
}
