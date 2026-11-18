package com.scamshield.jobmatching.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobmatching.dto.SkillComparisonResponse;
import com.scamshield.resume.dto.ExtractedResumeData;

@Service
public class SkillMatchingService {
    private static final String NOTE =
            "Skills are normalized for common aliases and compared once each. The provider does not distinguish "
                    + "required from preferred skills or verify that a listing's mentioned skills are employer requirements; "
                    + "missing skills mean only that the resume analysis did not evidence them.";
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("js", "javascript"),
            Map.entry("javascript", "javascript"),
            Map.entry("node", "nodejs"),
            Map.entry("node.js", "nodejs"),
            Map.entry("nodejs", "nodejs"),
            Map.entry("reactjs", "react"),
            Map.entry("react.js", "react"),
            Map.entry("springboot", "springboot"),
            Map.entry("springsecurity", "springsecurity"),
            Map.entry("springframework", "spring"),
            Map.entry("py", "python"),
            Map.entry("python3", "python"),
            Map.entry("ts", "typescript"),
            Map.entry("csharp", "c#"),
            Map.entry(".net", "dotnet"),
            Map.entry("k8s", "kubernetes"),
            Map.entry("postgres", "postgresql"),
            Map.entry("postgresql", "postgresql"),
            Map.entry("mongo", "mongodb"),
            Map.entry("mongodb", "mongodb"),
            Map.entry("amazonwebservices", "aws"),
            Map.entry("aws", "aws"),
            Map.entry("cpp", "c++"));

    public SkillComparisonResponse compare(ExtractedResumeData resume, Job job) {
        List<String> resumeSkills = resume == null ? List.of() : safe(resume.technicalSkills());
        List<String> listedSkills = safe(job.getRequiredSkills());
        if (listedSkills.isEmpty()) {
            return SkillComparisonResponse.unavailable(
                    resumeSkills, "No skills could be identified in this provider listing, so skill overlap was not scored.");
        }

        Set<String> resumeNames = normalize(resumeSkills);
        LinkedHashMap<String, String> distinctListingSkills = new LinkedHashMap<>();
        for (String skill : listedSkills) {
            distinctListingSkills.putIfAbsent(canonical(skill), skill.trim());
        }
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, String> entry : distinctListingSkills.entrySet()) {
            (resumeNames.contains(entry.getKey()) ? matched : missing).add(entry.getValue());
        }
        return new SkillComparisonResponse(
                resumeSkills,
                matched,
                missing,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                NOTE);
    }

    public Set<String> normalize(Set<String> values) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (values != null) values.stream().filter(value -> value != null && !value.isBlank())
                .map(this::canonical).forEach(result::add);
        return result;
    }

    private Set<String> normalize(List<String> values) {
        return normalize(values == null ? Set.of() : new LinkedHashSet<>(values));
    }

    private String canonical(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#.]", "");
        return ALIASES.getOrDefault(normalized, normalized);
    }

    private List<String> safe(List<String> values) {
        if (values == null) return List.of();
        return values.stream().filter(value -> value != null && !value.isBlank())
                .map(String::trim).distinct().toList();
    }
}
