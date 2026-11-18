package com.scamshield.profile.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSkillsRequest(
        @NotNull @Size(max = 100) List<@Valid SkillRequest> skills) {
}
