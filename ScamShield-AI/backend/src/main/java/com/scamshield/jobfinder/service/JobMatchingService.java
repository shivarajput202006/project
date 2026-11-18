package com.scamshield.jobfinder.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.scamshield.jobfinder.dto.JobMatchResponse;
import com.scamshield.jobfinder.model.Job;

import org.springframework.stereotype.Service;

@Service
public class JobMatchingService {
    public JobMatchResponse match(Set<String> resumeSkills, Job job) {
        Set<String> normalizedResumeSkills = normalize(resumeSkills);
        List<String> required = normalize(job.getRequiredSkills()).stream().toList();
        List<String> matching = required.stream().filter(normalizedResumeSkills::contains).toList();
        List<String> missing = required.stream().filter(skill -> !normalizedResumeSkills.contains(skill)).toList();
        int score = required.isEmpty() ? 0 : (int) Math.round(matching.size() * 100.0 / required.size());
        return new JobMatchResponse(
                true,
                score,
                matching,
                missing,
                "Score = matching skills mentioned in the listing divided by all extracted listing skills. "
                        + "This is a skills overlap indicator, not a qualification or hiring prediction.");
    }

    public JobMatchResponse unavailable() {
        return new JobMatchResponse(
                false,
                0,
                List.of(),
                List.of(),
                "Resume analysis is not available yet. No resume skills were assumed.");
    }

    private Set<String> normalize(Set<String> values) {
        if (values == null) return Set.of();
        return normalize(values.stream().toList());
    }

    private Set<String> normalize(List<String> values) {
        if (values == null) return Set.of();
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
