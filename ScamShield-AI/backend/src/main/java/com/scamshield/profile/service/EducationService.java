package com.scamshield.profile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.scamshield.profile.dto.EducationRequest;
import com.scamshield.profile.model.Education;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.profile.util.ProfileValidationUtil;

@Service
public class EducationService {
    private final UserProfileRepository profileRepository;
    private final ProfileService profileService;

    public EducationService(UserProfileRepository profileRepository, ProfileService profileService) {
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }

    public List<Education> list(String userId) {
        return profileService.getOrCreateProfile(userId).getEducation();
    }

    public Education add(String userId, EducationRequest request) {
        ProfileValidationUtil.validateEducation(request);
        UserProfile profile = profileService.getOrCreateProfile(userId);
        List<Education> updated = new ArrayList<>(profile.getEducation());
        Education education = toEducation(UUID.randomUUID().toString(), request);
        updated.add(education);
        profile.replaceEducation(updated);
        profileRepository.save(profile);
        return education;
    }

    public Education update(String userId, String id, EducationRequest request) {
        ProfileValidationUtil.validateEducation(request);
        UserProfile profile = profileService.getOrCreateProfile(userId);
        List<Education> updated = new ArrayList<>(profile.getEducation());
        int index = findIndex(updated, id);
        Education education = toEducation(id, request);
        updated.set(index, education);
        profile.replaceEducation(updated);
        profileRepository.save(profile);
        return education;
    }

    public void delete(String userId, String id) {
        UserProfile profile = profileService.getOrCreateProfile(userId);
        List<Education> updated = new ArrayList<>(profile.getEducation());
        updated.remove(findIndex(updated, id));
        profile.replaceEducation(updated);
        profileRepository.save(profile);
    }

    private int findIndex(List<Education> education, String id) {
        for (int index = 0; index < education.size(); index++) {
            if (education.get(index).getId().equals(id)) return index;
        }
        throw ProfileException.notFound("Education record was not found.");
    }

    private Education toEducation(String id, EducationRequest request) {
        return new Education(
                id,
                request.institutionName().trim(),
                request.degree().trim(),
                request.course().trim(),
                request.specialization() == null || request.specialization().isBlank()
                        ? null : request.specialization().trim(),
                request.startYear(),
                request.endYear(),
                request.gradeType(),
                request.gradeValue(),
                request.status());
    }
}
