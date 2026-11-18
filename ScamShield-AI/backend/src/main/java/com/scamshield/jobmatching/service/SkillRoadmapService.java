package com.scamshield.jobmatching.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobmatching.dto.SkillComparisonResponse;
import com.scamshield.jobmatching.dto.SkillRoadmapItem;
import com.scamshield.resume.dto.ExtractedResumeData;

@Service
public class SkillRoadmapService {
    public List<SkillRoadmapItem> build(
            Job job, ExtractedResumeData resume, SkillComparisonResponse comparison) {
        List<String> currentSkills = resume == null || resume.technicalSkills() == null
                ? List.of()
                : resume.technicalSkills().stream().filter(skill -> skill != null && !skill.isBlank()).limit(3).toList();
        return comparison.missingListingSkills().stream()
                .limit(5)
                .map(skill -> {
                    String foundation = currentSkills.isEmpty()
                            ? "Start with the documented job description and an introductory tutorial."
                            : "Relate the topic to resume-documented skills such as "
                                    + String.join(", ", currentSkills) + ".";
                    return new SkillRoadmapItem(
                            skill,
                            "This skill was mentioned in the " + job.getSourceName()
                                    + " listing but was not evidenced in the analyzed resume. Its absence is not evidence of ability or inability.",
                            List.of(
                                    "Learn the core concepts and vocabulary for " + skill + ".",
                                    foundation,
                                    "Practice a small implementation and record what you personally built."),
                            "Extend a small " + job.getTitle() + "-related project to demonstrate " + skill
                                    + ", then document the implementation and your contribution.");
                })
                .toList();
    }
}
