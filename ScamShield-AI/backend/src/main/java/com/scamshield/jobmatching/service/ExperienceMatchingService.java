package com.scamshield.jobmatching.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobmatching.dto.ExperienceComparisonResponse;
import com.scamshield.resume.dto.ExtractedResumeData;

@Service
public class ExperienceMatchingService {
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT),
            DateTimeFormatter.ofPattern("MMM uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ROOT));

    public ExperienceComparisonResponse compare(ExtractedResumeData resume, Job job) {
        JobExperience requiredLevel = job.getExperience();
        Integer years = documentedYears(resume);
        if (requiredLevel == null) {
            return new ExperienceComparisonResponse(
                    false, years, null, null, "The listing does not provide a comparable experience level.");
        }
        if (years == null) {
            return new ExperienceComparisonResponse(
                    false,
                    null,
                    requiredLevel,
                    null,
                    "Reliable dated work-experience ranges were not available in the analyzed resume; no experience was assumed.");
        }
        int score = levelScore(requiredLevel, years);
        return new ExperienceComparisonResponse(
                true,
                years,
                requiredLevel,
                score,
                "Compares years supported by dated resume experience ranges with the provider's broad experience category. "
                        + "The category is approximate, not a qualification decision.");
    }

    private Integer documentedYears(ExtractedResumeData resume) {
        if (resume == null || resume.experience() == null || resume.experience().isEmpty()) return null;
        List<Range> ranges = new ArrayList<>();
        for (ExtractedResumeData.Experience item : resume.experience()) {
            if (item == null) continue;
            YearMonth start = parse(item.startDate());
            YearMonth end = parse(item.endDate());
            if (start != null && end != null && !end.isBefore(start)) {
                ranges.add(new Range(start, end));
            }
        }
        if (ranges.isEmpty()) return null;
        ranges.sort(Comparator.comparing(Range::start));
        long months = 0;
        YearMonth start = ranges.get(0).start();
        YearMonth end = ranges.get(0).end();
        for (int i = 1; i < ranges.size(); i++) {
            Range next = ranges.get(i);
            if (!next.start().isAfter(end.plusMonths(1))) {
                if (next.end().isAfter(end)) end = next.end();
            } else {
                months += ChronoUnit.MONTHS.between(start, end) + 1;
                start = next.start();
                end = next.end();
            }
        }
        months += ChronoUnit.MONTHS.between(start, end) + 1;
        return (int) (months / 12);
    }

    private YearMonth parse(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.equalsIgnoreCase("present") || normalized.equalsIgnoreCase("current")) {
            return YearMonth.now();
        }
        try {
            if (normalized.matches("\\d{4}")) return YearMonth.of(Integer.parseInt(normalized), 1);
        } catch (NumberFormatException exception) {
            return null;
        }
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return YearMonth.parse(normalized, format);
            } catch (DateTimeParseException ignored) {
                // Try the next explicitly supported date format.
            }
        }
        return null;
    }

    private int levelScore(JobExperience level, int years) {
        return switch (level) {
            case FRESHER -> years == 0 ? 100 : years == 1 ? 50 : 0;
            case ENTRY_LEVEL -> years <= 1 ? 100 : years == 2 ? 50 : 0;
            case ONE_TO_THREE_YEARS -> years >= 1 && years <= 3 ? 100 : years == 0 || years <= 5 ? 50 : 0;
            case THREE_TO_FIVE_YEARS -> years >= 3 && years <= 5
                    ? 100
                    : years == 2 || (years > 5 && years <= 7) ? 50 : 0;
        };
    }

    private record Range(YearMonth start, YearMonth end) {
    }
}
