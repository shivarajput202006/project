package com.scamshield.profile.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.scamshield.profile.dto.ProfileResponse;
import com.scamshield.profile.dto.UpdateProfileRequest;
import com.scamshield.profile.model.Education;
import com.scamshield.profile.model.ProfileSkill;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRepository;

@Service
public class ProfileService {
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(UserProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public ProfileResponse get(UserPrincipal principal) {
        String userId = userId(principal);
        User user = findUser(userId);
        return toResponse(user, getOrCreateProfile(userId));
    }

    public ProfileResponse getForExport(User user) {
        return toResponse(user, getOrCreateProfile(user.getId()));
    }

    public ProfileResponse update(UserPrincipal principal, UpdateProfileRequest request) {
        String userId = userId(principal);
        User user = findUser(userId);
        String fullName = normalize(request.fullName());
        if (hasControlCharacters(fullName)
                || hasControlCharacters(request.professionalBio())
                || hasControlCharacters(request.city())
                || hasControlCharacters(request.state())
                || hasControlCharacters(request.country())) {
            throw ProfileException.badRequest("Profile fields contain unsupported control characters.");
        }

        user.updateFullName(fullName);
        userRepository.save(user);
        UserProfile profile = getOrCreateProfile(userId);
        profile.updateContact(
                normalizeNullable(request.phoneNumber()),
                normalizeNullable(request.city()),
                normalizeNullable(request.state()),
                normalizeNullable(request.country()),
                normalizeNullable(request.professionalBio()));
        return toResponse(user, profileRepository.save(profile));
    }

    UserProfile getOrCreateProfile(String userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> {
            try {
                return profileRepository.save(new UserProfile(userId));
            } catch (DuplicateKeyException exception) {
                return profileRepository.findByUserId(userId).orElseThrow(() -> exception);
            }
        });
    }

    ProfileResponse toResponse(User user, UserProfile profile) {
        List<Education> education = profile.getEducation();
        List<ProfileSkill> skills = profile.getSkills();
        int completed = 0;
        int total = 8;
        if (notBlank(user.getFullName())) completed++;
        if (notBlank(profile.getPhoneNumber())) completed++;
        if (notBlank(profile.getCity())) completed++;
        if (notBlank(profile.getState())) completed++;
        if (notBlank(profile.getCountry())) completed++;
        if (notBlank(profile.getProfessionalBio())) completed++;
        if (!education.isEmpty()) completed++;
        if (!skills.isEmpty()) completed++;
        return new ProfileResponse(
                user.getFullName(),
                user.getEmail(),
                profile.getPhoneNumber(),
                profile.getCity(),
                profile.getState(),
                profile.getCountry(),
                profile.getProfessionalBio(),
                profile.getPhotoStorageId() == null ? null : "/api/profile/photo",
                education,
                skills,
                completed * 100 / total);
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ProfileException.notFound("The authenticated account could not be found."));
    }

    private String userId(UserPrincipal principal) {
        if (principal == null || principal.getUser() == null
                || principal.getUser().getId() == null || principal.getUser().getId().isBlank()) {
            throw new IllegalStateException("Authenticated user has no database identity.");
        }
        return principal.getUser().getId();
    }

    private boolean hasControlCharacters(String value) {
        return value != null && CONTROL_CHARACTERS.matcher(value).find();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().replaceAll("\\s+", " ");
    }
}
