package com.scamshield.jobmatching.dto;

import java.util.List;

public record SkillRoadmapItem(
        String skill,
        String whyItMatters,
        List<String> learningSteps,
        String practiceProject) {
    public SkillRoadmapItem {
        learningSteps = List.copyOf(learningSteps);
    }
}
