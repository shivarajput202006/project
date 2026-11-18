package com.scamshield.profile.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.scamshield.profile.model.EducationStatus;
import com.scamshield.profile.model.GradeType;

public record EducationRequest(
        @NotBlank @Size(max = 150) String institutionName,
        @NotBlank @Size(max = 100) String degree,
        @NotBlank @Size(max = 120) String course,
        @Size(max = 120) String specialization,
        @Min(1950) @Max(2100) int startYear,
        @Min(1950) @Max(2100) Integer endYear,
        GradeType gradeType,
        BigDecimal gradeValue,
        @NotNull EducationStatus status) {
}
