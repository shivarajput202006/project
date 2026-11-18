package com.scamshield.reports.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;

@Document(collection = "report_metadata")
@CompoundIndex(
        name = "report_owner_source_idx",
        def = "{'ownerId': 1, 'sourceModule': 1, 'sourceRecordId': 1}",
        unique = true)
@CompoundIndex(name = "report_owner_type_created_idx", def = "{'ownerId': 1, 'type': 1, 'createdAt': -1}")
public class ReportMetadata {
    @Id
    private String id;

    private String ownerId;
    private String sourceModule;
    private String sourceRecordId;
    private String title;
    private ReportType type;
    private ReportStatus status;
    private Instant createdAt;
    private Instant hiddenAt;

    protected ReportMetadata() {
    }

    public ReportMetadata(
            String ownerId,
            String sourceModule,
            String sourceRecordId,
            String title,
            ReportType type,
            ReportStatus status,
            Instant createdAt,
            Instant hiddenAt) {
        this.ownerId = ownerId;
        this.sourceModule = sourceModule;
        this.sourceRecordId = sourceRecordId;
        this.title = title;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.hiddenAt = hiddenAt;
    }

    public void hideAt(Instant timestamp) {
        hiddenAt = timestamp;
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getSourceModule() { return sourceModule; }
    public String getSourceRecordId() { return sourceRecordId; }
    public String getTitle() { return title; }
    public ReportType getType() { return type; }
    public ReportStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getHiddenAt() { return hiddenAt; }
}
