package com.scamshield.resume.dto;

import java.util.List;

public record ExtractedResumeData(
        PersonalInfo personalInfo,
        List<Education> education,
        List<String> technicalSkills,
        List<String> softSkills,
        List<Experience> experience,
        List<Project> projects,
        List<String> certifications,
        List<String> strengths,
        List<String> weaknesses,
        List<String> atsFeedback,
        List<ImprovementSuggestion> improvementSuggestions,
        List<RecommendedRole> recommendedRoles) {

    public ExtractedResumeData {
        education = List.copyOf(education);
        technicalSkills = List.copyOf(technicalSkills);
        softSkills = List.copyOf(softSkills);
        experience = List.copyOf(experience);
        projects = List.copyOf(projects);
        certifications = List.copyOf(certifications);
        strengths = List.copyOf(strengths);
        weaknesses = List.copyOf(weaknesses);
        atsFeedback = List.copyOf(atsFeedback);
        improvementSuggestions = List.copyOf(improvementSuggestions);
        recommendedRoles = List.copyOf(recommendedRoles);
    }

    public record PersonalInfo(String name, String email, String phone, String location, String linkedin, String portfolio) {
    }

    public record Education(
            String degree,
            String institution,
            String fieldOfStudy,
            String startDate,
            String endDate,
            String details) {
    }

    public record Experience(
            String kind,
            String title,
            String company,
            String location,
            String startDate,
            String endDate,
            String summary) {
    }

    public record Project(String name, String description, List<String> technologies, String url) {
        public Project {
            technologies = List.copyOf(technologies);
        }
    }

    public record ImprovementSuggestion(String category, String suggestion, String reason) {
    }

    public record RecommendedRole(
            String title,
            String rationale,
            List<String> matchingSkills,
            List<String> missingSkills,
            String experienceLevel) {
        public RecommendedRole {
            matchingSkills = List.copyOf(matchingSkills);
            missingSkills = List.copyOf(missingSkills);
        }
    }
}
