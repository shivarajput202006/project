package com.scamshield.reports.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.reports.model.ReportMetadata;
import com.scamshield.reports.enums.ReportType;

public interface ReportRepository extends MongoRepository<ReportMetadata, String> {
    List<ReportMetadata> findByOwnerId(String ownerId);

    List<ReportMetadata> findByOwnerIdAndHiddenAtIsNotNull(String ownerId);

    Optional<ReportMetadata> findByOwnerIdAndSourceModuleAndSourceRecordId(
            String ownerId, String sourceModule, String sourceRecordId);

    long countByOwnerIdAndTypeAndHiddenAtIsNotNull(String ownerId, ReportType type);

    long deleteByOwnerId(String ownerId);
}
