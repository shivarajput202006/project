package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.profile.dto.SkillRequest;
import com.scamshield.profile.dto.UpdateSkillsRequest;
import com.scamshield.profile.model.SkillCategory;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.util.ProfileException;

class SkillsServiceTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final SkillsService service = new SkillsService(repository, profileService);

    @Test
    void storesSkillsAsUserProvidedWithoutTouchingResumeAnalysis() {
        UserProfile profile = new UserProfile("owner");
        when(profileService.getOrCreateProfile("owner")).thenReturn(profile);
        when(repository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.replace("owner", new UpdateSkillsRequest(List.of(
                new SkillRequest(" Java ", SkillCategory.PROGRAMMING_LANGUAGES))));

        assertEquals("Java", result.get(0).name());
        assertEquals("USER_PROVIDED", result.get(0).source());
        assertEquals(result, profile.getSkills());
        verify(repository).save(profile);
    }

    @Test
    void rejectsDuplicateSkillsIgnoringCase() {
        when(profileService.getOrCreateProfile("owner")).thenReturn(new UserProfile("owner"));
        assertThrows(
                ProfileException.class,
                () -> service.replace("owner", new UpdateSkillsRequest(List.of(
                        new SkillRequest("Java", SkillCategory.PROGRAMMING_LANGUAGES),
                        new SkillRequest(" java ", SkillCategory.OTHER_TECHNICAL_SKILLS)))));
    }
}
