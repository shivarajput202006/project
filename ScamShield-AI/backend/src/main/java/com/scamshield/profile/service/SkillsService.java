package com.scamshield.profile.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.scamshield.profile.dto.SkillRequest;
import com.scamshield.profile.dto.UpdateSkillsRequest;
import com.scamshield.profile.model.ProfileSkill;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.util.ProfileException;

@Service
public class SkillsService {
    private final UserProfileRepository profileRepository;
    private final ProfileService profileService;

    public SkillsService(UserProfileRepository profileRepository, ProfileService profileService) {
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }

    public List<ProfileSkill> replace(String userId, UpdateSkillsRequest request) {
        Set<String> normalizedNames = new HashSet<>();
        List<ProfileSkill> skills = request.skills().stream().map(skill -> {
            String name = skill.name().trim().replaceAll("\\s+", " ");
            if (!normalizedNames.add(name.toLowerCase(Locale.ROOT))) {
                throw ProfileException.badRequest("Skill names must be unique.");
            }
            return new ProfileSkill(name, skill.category());
        }).toList();

        UserProfile profile = profileService.getOrCreateProfile(userId);
        profile.replaceSkills(skills);
        profileRepository.save(profile);
        return skills;
    }
}
