package com.scamshield.profile.dto;

import java.util.List;

import com.scamshield.profile.model.Education;
import com.scamshield.profile.model.ProfileSkill;

public record ProfileResponse(
        String fullName,
        String email,
        String phoneNumber,
        String city,
        String state,
        String country,
        String professionalBio,
        String photoUrl,
        List<Education> education,
        List<ProfileSkill> skills,
        int completionPercentage) {
}
