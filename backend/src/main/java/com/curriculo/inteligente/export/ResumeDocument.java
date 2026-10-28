package com.curriculo.inteligente.export;

import java.util.List;

public record ResumeDocument(
        Header header,
        String summary,
        List<ExperienceLine> experiences,
        List<SkillLine> skills,
        List<EducationLine> education,
        List<LanguageLine> languages,
        List<ProjectLine> projects,
        List<CertificationLine> certifications) {

    public record Header(
            String fullName,
            String targetRole,
            String city,
            String state,
            String workMode,
            String professionalEmail,
            String phone,
            String linkedinUrl,
            String githubUrl,
            String portfolioUrl,
            String otherProfessionalLinks,
            Integer age,
            boolean includeAge,
            boolean hasPhoto) {
    }

    public record ExperienceLine(
            String id,
            String company,
            String roleTitle,
            Integer startYear,
            Integer startMonth,
            Integer endYear,
            Integer endMonth,
            boolean currentJob,
            String projectLabel,
            String responsibilities,
            String tools,
            String results,
            String detailLevel) {
    }

    public record SkillLine(String name, String category) {
    }

    public record EducationLine(String level, String course, String institution, String status, Integer startYear, Integer endYear) {
    }

    public record LanguageLine(String name, String comprehension, String reading, String writing, String conversation) {
    }

    public record ProjectLine(String name, String description, String url, boolean personal) {
    }

    public record CertificationLine(String name, String issuer, Integer year) {
    }
}
