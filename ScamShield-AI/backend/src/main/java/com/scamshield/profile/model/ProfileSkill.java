package com.scamshield.profile.model;

public record ProfileSkill(String name, SkillCategory category, String source) {
    public ProfileSkill(String name, SkillCategory category) {
        this(name, category, "USER_PROVIDED");
    }
}
