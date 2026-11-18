package com.scamshield.jobmatching.service;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobmatching.dto.EducationComparisonResponse;
import com.scamshield.resume.dto.ExtractedResumeData;

@Service
public class EducationMatchingService {
    private static final List<DegreeRule> DEGREE_RULES = List.of(
            new DegreeRule(5, Pattern.compile("\\b(ph\\.?d\\.?|doctorate)\\b", Pattern.CASE_INSENSITIVE), "doctoral degree"),
            new DegreeRule(4, Pattern.compile("\\b(master'?s?|mba|m\\.?s\\.?|m\\.?sc\\.?|m\\.?tech)\\b", Pattern.CASE_INSENSITIVE), "master's degree"),
            new DegreeRule(3, Pattern.compile("\\b(bachelor'?s?|b\\.?s\\.?|b\\.?sc\\.?|b\\.?e\\.?|b\\.?tech)\\b", Pattern.CASE_INSENSITIVE), "bachelor's degree"),
            new DegreeRule(2, Pattern.compile("\\b(associate'?s?)\\b", Pattern.CASE_INSENSITIVE), "associate degree"),
            new DegreeRule(1, Pattern.compile("\\b(diploma)\\b", Pattern.CASE_INSENSITIVE), "diploma"));

    public EducationComparisonResponse compare(ExtractedResumeData resume, Job job) {
        String description = job.getDescription() == null ? "" : job.getDescription();
        List<Integer> requirementRanks = requirementRanks(description);
        if (requirementRanks.isEmpty()) {
            return unavailable("The listing does not state a recognizable education requirement.");
        }
        if (requirementRanks.stream().distinct().count() > 1) {
            return unavailable("The listing mentions multiple degree levels that cannot be reliably classified as alternatives or minimum requirements.");
        }
        int requiredRank = requirementRanks.get(0);
        List<String> degrees = resume == null || resume.education() == null
                ? List.of()
                : resume.education().stream()
                        .filter(education -> education != null && education.degree() != null
                                && !education.degree().isBlank())
                        .map(education -> education.degree().trim())
                        .toList();
        if (degrees.isEmpty()) {
            return unavailable("The resume analysis did not provide education evidence; education was not treated as absent.");
        }

        int resumeRank = degrees.stream().mapToInt(this::degreeRank).max().orElse(0);
        boolean matches = resumeRank >= requiredRank;
        String required = DEGREE_RULES.stream()
                .filter(rule -> rule.rank() == requiredRank)
                .map(DegreeRule::label)
                .findFirst()
                .orElse("stated degree");
        return new EducationComparisonResponse(
                true,
                String.join(", ", degrees),
                required,
                matches ? 100 : 0,
                matches
                        ? "A degree stated in the resume is at or above the explicit level mentioned in the listing."
                        : "The analyzed resume does not evidence the explicit education level mentioned in the listing; this is not a judgment about unlisted credentials.");
    }

    private List<Integer> requirementRanks(String description) {
        List<Integer> ranks = new ArrayList<>();
        for (DegreeRule rule : DEGREE_RULES) {
            var matcher = rule.pattern().matcher(description);
            while (matcher.find()) {
                int start = Math.max(0, matcher.start() - 50);
                int end = Math.min(description.length(), matcher.end() + 50);
                String context = description.substring(start, end).toLowerCase(Locale.ROOT);
                if (context.matches("(?s).*(degree|education|qualification|require|must have|preferred|minim).*")) {
                    ranks.add(rule.rank());
                }
            }
        }
        return ranks;
    }

    private int degreeRank(String degree) {
        return DEGREE_RULES.stream()
                .filter(rule -> rule.pattern().matcher(degree.toLowerCase(Locale.ROOT)).find())
                .mapToInt(DegreeRule::rank)
                .max()
                .orElse(0);
    }

    private EducationComparisonResponse unavailable(String explanation) {
        return new EducationComparisonResponse(false, null, null, null, explanation);
    }

    private record DegreeRule(int rank, Pattern pattern, String label) {
    }
}
