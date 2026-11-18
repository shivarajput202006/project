package com.scamshield.profile.model;

import java.math.BigDecimal;

public class Education {
    private String id;
    private String institutionName;
    private String degree;
    private String course;
    private String specialization;
    private int startYear;
    private Integer endYear;
    private GradeType gradeType;
    private BigDecimal gradeValue;
    private EducationStatus status;

    protected Education() {
    }

    public Education(
            String id,
            String institutionName,
            String degree,
            String course,
            String specialization,
            int startYear,
            Integer endYear,
            GradeType gradeType,
            BigDecimal gradeValue,
            EducationStatus status) {
        this.id = id;
        this.institutionName = institutionName;
        this.degree = degree;
        this.course = course;
        this.specialization = specialization;
        this.startYear = startYear;
        this.endYear = endYear;
        this.gradeType = gradeType;
        this.gradeValue = gradeValue;
        this.status = status;
    }

    public String getId() { return id; }
    public String getInstitutionName() { return institutionName; }
    public String getDegree() { return degree; }
    public String getCourse() { return course; }
    public String getSpecialization() { return specialization; }
    public int getStartYear() { return startYear; }
    public Integer getEndYear() { return endYear; }
    public GradeType getGradeType() { return gradeType; }
    public BigDecimal getGradeValue() { return gradeValue; }
    public EducationStatus getStatus() { return status; }
}
