package com.scamshield.jobfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.jobfinder.dto.JobSaveRequest;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.model.SavedJob;
import com.scamshield.jobfinder.provider.ConfiguredJobProvider.JobProviderException;
import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {
    @Mock
    private JobRepository jobRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private JobProviderService providerService;

    private SavedJobService service;

    @BeforeEach
    void setUp() {
        service = new SavedJobService(jobRepository, savedJobRepository, providerService);
    }

    @Test
    void savesOnlyAListingFreshlyFoundAtTheConfiguredProvider() {
        Job job = job();
        when(savedJobRepository.findByUserIdAndJobId("user-1", job.getId())).thenReturn(Optional.empty());
        when(providerService.findById("Adzuna", "123", "Software Engineer", "London"))
                .thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);
        when(savedJobRepository.existsByUserIdAndJobId("user-1", job.getId())).thenReturn(false);
        when(savedJobRepository.save(any(SavedJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.save(principal("user-1"), job.getId(), new JobSaveRequest("Software Engineer", "London"));

        assertEquals("adzuna-123", saved.id());
        verify(providerService).findById("Adzuna", "123", "Software Engineer", "London");
        verify(savedJobRepository).save(any(SavedJob.class));
    }

    @Test
    void duplicateSaveIsIdempotentAndDoesNotCallTheProviderAgain() {
        Job job = job();
        when(savedJobRepository.findByUserIdAndJobId("user-1", job.getId()))
                .thenReturn(Optional.of(new SavedJob("user-1", job.getId(), Instant.now())));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(savedJobRepository.existsByUserIdAndJobId("user-1", job.getId())).thenReturn(true);

        var saved = service.save(principal("user-1"), job.getId(), new JobSaveRequest("ignored", ""));

        assertEquals(job.getId(), saved.id());
        verify(providerService, never()).findById(anyString(), anyString(), anyString(), anyString());
        verify(savedJobRepository, never()).save(any(SavedJob.class));
    }

    @Test
    void cannotUnsaveAnotherUsersListing() {
        when(savedJobRepository.findByUserIdAndJobId("user-2", "adzuna-123")).thenReturn(Optional.empty());

        assertThrows(
                SavedJobService.SavedJobNotFoundException.class,
                () -> service.unsave(principal("user-2"), "adzuna-123"));

        verify(savedJobRepository, never()).deleteByUserIdAndJobId(any(), any());
    }

    @Test
    void refusesToSaveAnIdentifierThatIsNotFromTheConfiguredProvider() {
        assertThrows(
                JobProviderException.class,
                () -> service.save(principal("user-1"), "external-123", new JobSaveRequest("Engineer", "London")));
        verify(providerService, never()).findById(anyString(), anyString(), anyString(), anyString());
    }

    private Job job() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Job(
                "adzuna-123", "Adzuna", "123", "Software Engineer", "Example Co", "London",
                null, null, null, List.of("java"), "Java role", null, null, null, false, null, now, now,
                JobVerificationStatus.SOURCE_LISTED, "https://adzuna.co.uk/jobs/land/ad/123", null);
    }

    private UserPrincipal principal(String id) {
        User user = new User("Test User", "user@example.com", "hash", UserRole.USER, Instant.now());
        user.setId(id);
        return new UserPrincipal(user);
    }
}
