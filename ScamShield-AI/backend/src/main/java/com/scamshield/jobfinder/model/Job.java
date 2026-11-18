package com.scamshield.jobfinder.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jobs")
@CompoundIndex(name = "job_provider_id_idx", def = "{'sourceName': 1, 'sourceJobId': 1}", unique = true)
public class Job {
    @Id
    private String id;

    private String sourceName;
    private String sourceJobId;
    private String title;
    private String company;
    private String location;
    private JobWorkMode workMode;
    private JobExperience experience;
    private JobType jobType;
    private List<String> requiredSkills;
    private String description;
    private Double salaryMin;
    private Double salaryMax;
    private String salaryCurrency;
    private boolean salaryIsPredicted;
    private Instant postedAt;
    private Instant fetchedAt;
    private Instant lastUpdatedAt;
    private JobVerificationStatus verificationStatus;
    private String providerReference;
    private String officialApplyUrl;

    protected Job() {
    }

    public Job(
            String id,
            String sourceName,
            String sourceJobId,
            String title,
            String company,
            String location,
            JobWorkMode workMode,
            JobExperience experience,
            JobType jobType,
            List<String> requiredSkills,
            String description,
            Double salaryMin,
            Double salaryMax,
            String salaryCurrency,
            boolean salaryIsPredicted,
            Instant postedAt,
            Instant fetchedAt,
            Instant lastUpdatedAt,
            JobVerificationStatus verificationStatus,
            String providerReference,
            String officialApplyUrl) {
        this.id = id;
        this.sourceName = sourceName;
        this.sourceJobId = sourceJobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.workMode = workMode;
        this.experience = experience;
        this.jobType = jobType;
        this.requiredSkills = List.copyOf(requiredSkills);
        this.description = description;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.salaryIsPredicted = salaryIsPredicted;
        this.postedAt = postedAt;
        this.fetchedAt = fetchedAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.verificationStatus = verificationStatus;
        this.providerReference = providerReference;
        this.officialApplyUrl = officialApplyUrl;
    }

    public String getId() { return id; }
    public String getSourceName() { return sourceName; }
    public String getSourceJobId() { return sourceJobId; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public JobWorkMode getWorkMode() { return workMode; }
    public JobExperience getExperience() { return experience; }
    public JobType getJobType() { return jobType; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public String getDescription() { return description; }
    public Double getSalaryMin() { return salaryMin; }
    public Double getSalaryMax() { return salaryMax; }
    public String getSalaryCurrency() { return salaryCurrency; }
    public boolean isSalaryIsPredicted() { return salaryIsPredicted; }
    public Instant getPostedAt() { return postedAt; }
    public Instant getFetchedAt() { return fetchedAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public JobVerificationStatus getVerificationStatus() { return verificationStatus; }
    public String getProviderReference() { return providerReference; }
    public String getOfficialApplyUrl() { return officialApplyUrl; }
}
