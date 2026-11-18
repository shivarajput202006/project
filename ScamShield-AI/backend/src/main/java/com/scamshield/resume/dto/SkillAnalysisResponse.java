package com.scamshield.resume.dto;

import java.util.List;

public record SkillAnalysisResponse(
        List<String> technicalSkills,
        List<String> softSkills,
        List<RoleSkillGap> roleSkillGaps) {

    public SkillAnalysisResponse {
        technicalSkills = List.copyOf(technicalSkills);
        softSkills = List.copyOf(softSkills);
        roleSkillGaps = List.copyOf(roleSkillGaps);
    }

    public record RoleSkillGap(String role, List<String> evidencedSkills, List<String> notEvidencedSkills) {
        public RoleSkillGap {
            evidencedSkills = List.copyOf(evidencedSkills);
            notEvidencedSkills = List.copyOf(notEvidencedSkills);
        }
    }
}
