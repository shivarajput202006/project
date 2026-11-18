package com.scamshield.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.scamshield.profile.dto.EducationRequest;
import com.scamshield.profile.model.EducationStatus;
import com.scamshield.profile.model.UserProfile;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.profile.util.ProfileException;

class EducationServiceTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final EducationService service = new EducationService(repository, profileService);

    @Test
    void addsUpdatesAndDeletesEducationForOwner() {
        UserProfile profile = new UserProfile("owner");
        when(profileService.getOrCreateProfile("owner")).thenReturn(profile);
        when(repository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        EducationRequest request = new EducationRequest(
                "Example University", "BSc", "Computer Science", null, 2022, null,
                null, null, EducationStatus.IN_PROGRESS);

        var added = service.add("owner", request);
        var updated = service.update("owner", added.getId(), new EducationRequest(
                "Example University", "BSc", "Computer Science", "Systems", 2022, 2025,
                null, null, EducationStatus.COMPLETED));

        assertEquals("Systems", updated.getSpecialization());
        assertEquals(List.of(updated), service.list("owner"));
        service.delete("owner", added.getId());
        assertEquals(List.of(), service.list("owner"));
        verify(repository, org.mockito.Mockito.times(3)).save(profile);
    }

    @Test
    void refusesToUpdateEducationNotOwnedByProfile() {
        when(profileService.getOrCreateProfile("owner")).thenReturn(new UserProfile("owner"));
        assertThrows(
                ProfileException.class,
                () -> service.delete("owner", "another-users-education"));
    }
}
