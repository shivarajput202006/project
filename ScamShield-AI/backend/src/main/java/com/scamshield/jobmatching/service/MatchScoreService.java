package com.scamshield.jobmatching.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobmatching.dto.EducationComparisonResponse;
import com.scamshield.jobmatching.dto.ExperienceComparisonResponse;
import com.scamshield.jobmatching.dto.MatchCriterionResponse;
import com.scamshield.jobmatching.dto.SkillComparisonResponse;
import com.scamshield.jobmatching.dto.JobMatchRequest;

@Service
public class MatchScoreService {
    private static final String BASE_MODEL_VERSION = "job-match-v1";
    private final int skillWeight;
    private final int experienceWeight;
    private final int educationWeight;
    private final int locationModeWeight;
    private final int jobTypeWeight;

    public MatchScoreService() {
        this(50, 20, 10, 10, 10);
    }

    @Autowired
    public MatchScoreService(
            @Value("${app.job-matching.weight.skills:50}") int skillWeight,
            @Value("${app.job-matching.weight.experience:20}") int experienceWeight,
            @Value("${app.job-matching.weight.education:10}") int educationWeight,
            @Value("${app.job-matching.weight.location-mode:10}") int locationModeWeight,
            @Value("${app.job-matching.weight.job-type:10}") int jobTypeWeight) {
        int configuredTotal = skillWeight + experienceWeight + educationWeight + locationModeWeight + jobTypeWeight;
        if (skillWeight < 0 || skillWeight > 100 || experienceWeight < 0 || experienceWeight > 100
                || educationWeight < 0 || educationWeight > 100 || locationModeWeight < 0
                || locationModeWeight > 100 || jobTypeWeight < 0 || jobTypeWeight > 100
                || configuredTotal != 100) {
            throw new IllegalArgumentException("Job-matching scoring weights must be non-negative and total exactly 100.");
        }
        this.skillWeight = skillWeight;
        this.experienceWeight = experienceWeight;
        this.educationWeight = educationWeight;
        this.locationModeWeight = locationModeWeight;
        this.jobTypeWeight = jobTypeWeight;
    }

    public String modelVersion() {
        return BASE_MODEL_VERSION + "-s" + skillWeight + "-e" + experienceWeight + "-d" + educationWeight
                + "-l" + locationModeWeight + "-t" + jobTypeWeight;
    }

    public ScoreResult score(
            JobMatchRequest request,
            Job job,
            SkillComparisonResponse skills,
            ExperienceComparisonResponse experience,
            EducationComparisonResponse education) {
        List<Criterion> criteria = new ArrayList<>();
        Integer skillScore = skills.matchedListingSkills().size() + skills.missingListingSkills().size() == 0
                ? null
                : (int) Math.round(
                        skills.matchedListingSkills().size() * 100.0
                                / (skills.matchedListingSkills().size() + skills.missingListingSkills().size()));
        criteria.add(new Criterion(
                "Technical skills",
                skillWeight,
                skillScore,
                skillScore == null
                        ? "Not scored because the listing contains no comparable skill terms."
                        : "Matched listed skills divided by distinct skills identified in the provider listing. "
                                + "Provider data does not mark these as required or preferred."));

        criteria.add(new Criterion(
                "Experience",
                experienceWeight,
                experience.score(),
                experience.explanation()));
        criteria.add(new Criterion(
                "Education",
                educationWeight,
                education.score(),
                education.explanation()));

        Integer locationScore = locationScore(request.location(), job.getLocation());
        Integer modeScore = compatibilityScore(request.workMode(), job.getWorkMode());
        Integer locationModeScore = averageKnown(locationScore, modeScore);
        String locationExplanation = locationModeExplanation(locationScore, modeScore);
        criteria.add(new Criterion(
                "Location and work mode", locationModeWeight, locationModeScore, locationExplanation));

        Integer jobTypeScore = compatibilityScore(request.jobType(), job.getJobType());
        criteria.add(new Criterion(
                "Job type",
                jobTypeWeight,
                jobTypeScore,
                jobTypeExplanation(jobTypeScore)));

        int applicableWeight = criteria.stream()
                .filter(criterion -> criterion.score() != null)
                .mapToInt(Criterion::weight)
                .sum();
        if (applicableWeight == 0) {
            List<MatchCriterionResponse> response = criteria.stream()
                    .map(criterion -> new MatchCriterionResponse(
                            criterion.name(),
                            criterion.weight(),
                            criterion.score(),
                            0,
                            criterion.explanation()))
                    .toList();
            return new ScoreResult(null, response);
        }

        double weightedScore = criteria.stream()
                .filter(criterion -> criterion.score() != null)
                .mapToDouble(criterion -> criterion.score() * criterion.weight())
                .sum();
        Integer total = (int) Math.round(weightedScore / applicableWeight);
        List<MatchCriterionResponse> response = toResponses(criteria, applicableWeight);
        return new ScoreResult(total, response);
    }

    private List<MatchCriterionResponse> toResponses(List<Criterion> criteria, int applicableWeight) {
        int[] effectiveWeights = new int[criteria.size()];
        List<WeightRemainder> remainders = new ArrayList<>();
        int assigned = 0;
        for (int i = 0; i < criteria.size(); i++) {
            Criterion criterion = criteria.get(i);
            if (criterion.score() == null) continue;
            double exact = criterion.weight() * 100.0 / applicableWeight;
            effectiveWeights[i] = (int) Math.floor(exact);
            assigned += effectiveWeights[i];
            remainders.add(new WeightRemainder(i, exact - effectiveWeights[i]));
        }
        remainders.sort((left, right) -> Double.compare(right.fraction(), left.fraction()));
        for (int i = 0; i < 100 - assigned; i++) {
            effectiveWeights[remainders.get(i).index()]++;
        }
        List<MatchCriterionResponse> response = new ArrayList<>();
        for (int i = 0; i < criteria.size(); i++) {
            Criterion criterion = criteria.get(i);
            response.add(new MatchCriterionResponse(
                    criterion.name(),
                    criterion.weight(),
                    criterion.score(),
                    effectiveWeights[i],
                    criterion.explanation()));
        }
        return List.copyOf(response);
    }

    private Integer locationScore(String preference, String actual) {
        if (blank(preference) || blank(actual)) return null;
        String normalizedPreference = normalize(preference);
        String normalizedActual = normalize(actual);
        return normalizedActual.contains(normalizedPreference) || normalizedPreference.contains(normalizedActual)
                ? 100
                : 0;
    }

    private Integer compatibilityScore(Object preference, Object actual) {
        if (preference == null || actual == null) return null;
        return Objects.equals(preference, actual) ? 100 : 0;
    }

    private Integer averageKnown(Integer first, Integer second) {
        if (first == null && second == null) return null;
        if (first == null) return second;
        if (second == null) return first;
        return (int) Math.round((first + second) / 2.0);
    }

    private String locationModeExplanation(Integer locationScore, Integer modeScore) {
        List<String> details = new ArrayList<>();
        details.add(locationScore == null
                ? "Location was not compared because a preference or listing location is unavailable."
                : "Location " + (locationScore == 100 ? "matches the selected preference." : "differs from the selected preference."));
        details.add(modeScore == null
                ? "Work mode was not compared because a preference or listing value is unavailable."
                : "Work mode " + (modeScore == 100 ? "matches the selected preference." : "differs from the selected preference."));
        return String.join(" ", details);
    }

    private String jobTypeExplanation(Integer score) {
        if (score == null) return "Not scored because the preference or listing value is unavailable.";
        return "Job type" + (score == 100 ? " matches the selected preference." : " differs from the selected preference.");
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private record Criterion(String name, int weight, Integer score, String explanation) {
    }

    private record WeightRemainder(int index, double fraction) {
    }

    public record ScoreResult(Integer score, List<MatchCriterionResponse> criteria) {
        public ScoreResult {
            criteria = List.copyOf(criteria);
        }
    }
}
