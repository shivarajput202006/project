package com.scamshield.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.scamshield.profile.model.SkillCategory;

public record SkillRequest(
        @NotBlank @Size(max = 60) String name,
        @NotNull SkillCategory category) {
}
