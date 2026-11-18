package com.scamshield.resume.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.dto.ResumeScoreResponse;

@Service
public class ResumeScoringService {
    private static final String DISCLAIMER =
            "Advisory checklist only; this is not an official ATS score and does not predict screening outcomes.";
    private static final Pattern SECTION_HEADING = Pattern.compile(
            "(?im)^\\s*(education|experience|work history|skills|projects|certifications)\\s*[:\\-]?\\s*$");

    public ResumeScoreResponse score(ExtractedResumeData data, String documentText) {
        List<ResumeScoreResponse.CriterionScore> criteria = new ArrayList<>();
        int coreSections = 0;
        var personal = data.personalInfo();
        if (hasText(personal.name()) || hasText(personal.email()) || hasText(personal.phone())) coreSections++;
        if (!data.education().isEmpty()) coreSections++;
        if (!data.technicalSkills().isEmpty() || !data.softSkills().isEmpty()) coreSections++;
        if (!data.experience().isEmpty()) coreSections++;
        if (!data.projects().isEmpty()) coreSections++;
        int sectionScore = coreSections * 20;
        criteria.add(criterion(
                "Section completeness",
                sectionScore,
                coreSections + " of 5 core sections have information supported by the resume.",
                "Add or clarify contact details, education, skills, experience, and projects where applicable."));

        int skillScore = Math.min(100, (data.technicalSkills().size() + data.softSkills().size()) * 12);
        criteria.add(criterion(
                "Skills coverage",
                skillScore,
                "Based on " + (data.technicalSkills().size() + data.softSkills().size())
                        + " explicit skills identified in the document.",
                "List relevant skills explicitly and group technical and interpersonal skills."));

        int projectScore = Math.min(100, data.projects().stream()
                .mapToInt(project -> hasText(project.description()) ? 25 : 10)
                .sum());
        criteria.add(criterion(
                "Project evidence",
                projectScore,
                data.projects().size() + " projects were identified; detailed descriptions contribute more.",
                "Describe each project’s goal, your contribution, technologies, and measurable outcome when available."));

        int experienceScore = Math.min(100, data.experience().stream()
                .mapToInt(experience -> hasText(experience.summary()) ? 20 : 10)
                .sum());
        criteria.add(criterion(
                "Experience detail",
                experienceScore,
                data.experience().size() + " work or internship entries were identified; described entries contribute more.",
                "Use concise accomplishment statements and include dates or outcomes only when accurate."));

        long headingCount = SECTION_HEADING.matcher(documentText).results().count();
        int readabilityScore = Math.min(100, (int) headingCount * 20);
        criteria.add(criterion(
                "Text readability",
                readabilityScore,
                headingCount + " recognizable section headings were found in extracted text. Visual layout is not assessed.",
                "Use clear, consistent section headings and a simple text-readable layout."));

        int roleScore = data.recommendedRoles().isEmpty()
                ? 0
                : Math.min(100, data.recommendedRoles().get(0).matchingSkills().size() * 20);
        criteria.add(criterion(
                "Role alignment",
                roleScore,
                "Based on explicit skill overlap with the first suggested role; recommendations are advisory.",
                "Tailor the resume to a specific role using only skills and experience you can support."));

        long usableCharacters = documentText.codePoints().filter(Character::isLetterOrDigit).count();
        boolean available = coreSections >= 2 && usableCharacters >= 150;
        Integer overall = available
                ? (int) Math.round(criteria.stream().mapToInt(ResumeScoreResponse.CriterionScore::score)
                        .average().orElse(0))
                : null;
        return new ResumeScoreResponse(
                available,
                overall,
                available ? "Advisory resume feedback score" : "Unavailable",
                criteria,
                DISCLAIMER);
    }

    private ResumeScoreResponse.CriterionScore criterion(
            String name, int score, String explanation, String improvementAction) {
        return new ResumeScoreResponse.CriterionScore(name, score, explanation, improvementAction);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
