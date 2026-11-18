package com.scamshield.jobfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobSaveRequest(
        @NotBlank @Size(max = 120) String keyword,
        @Size(max = 100) String location) {
}
