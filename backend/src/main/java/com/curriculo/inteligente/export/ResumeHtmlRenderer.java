package com.curriculo.inteligente.export;

import java.util.List;
import java.util.stream.Collectors;

public final class ResumeHtmlRenderer {

    private ResumeHtmlRenderer() {
    }

    public static String render(ResumeDocument document, String photoDataUri) {
        ResumeDocument.Header header = document.header();
        StringBuilder html = new StringBuilder();
        html.append("""
                <!DOCTYPE html>
                <html lang="pt-BR"><head><meta charset="UTF-8"/>
                <style>
                @page { size: A4; margin: 16mm 17mm 16mm 17mm; }
                body { font-family: Arial, Helvetica, sans-serif; font-weight: 400; color: #111; font-size: 10.5pt; line-height: 1.35; }
                h1 { font-family: Arial, Helvetica, sans-serif; font-size: 16.5pt; font-weight: 700; color: #1F4E79; margin: 0 0 2pt 0; }
                .role { font-family: Arial, Helvetica, sans-serif; font-size: 10.5pt; font-weight: 700; margin: 0 0 2pt 0; }
                .meta, .contact { font-size: 10pt; font-weight: 400; margin: 0; }
                a { color: #1F4E79; font-weight: 400; text-decoration: underline; }
                h2 { font-family: Arial, Helvetica, sans-serif; font-size: 12pt; font-weight: 700; color: #1F4E79; border-bottom: 0.6pt solid #1F4E79; margin: 10pt 0 4pt 0; padding-bottom: 1pt; }
                .tech { font-size: 9pt; border-top: 1pt solid #1F4E79; border-bottom: 1pt solid #1F4E79; padding: 3pt 0; }
                .project { font-size: 9.5pt; font-style: italic; margin: 0 0 2pt 0; }
                ul { list-style: none; margin: 2pt 0 0 0; padding: 0; }
                li { margin: 0 0 1pt 0; padding-left: 10pt; font-size: 10.5pt; line-height: 14.2pt; }
                li:before { content: "•"; display: inline-block; width: 10pt; margin-left: -10pt; font-size: 7pt; line-height: 14.2pt; }
                .job { font-family: Arial, Helvetica, sans-serif; font-size: 10.5pt; font-weight: 700; margin: 6pt 0 0 0; }
                strong { font-weight: 700; }
                table.head { width: 100%; border-collapse: collapse; }
                td.photo { width: 92px; vertical-align: top; text-align: right; }
                img.photo { width: 92px; height: 110px; }
                </style></head><body>
                """);
        html.append("<table class=\"head\"><tr><td>");
        html.append("<h1>").append(esc(header.fullName())).append("</h1>");
        html.append("<p class=\"role\">").append(esc(header.targetRole())).append("</p>");
        html.append("<p class=\"meta\">").append(esc(place(header)));
        if (header.includeAge() && header.age() != null) {
            html.append(" · ").append(header.age()).append(" anos");
        }
        if (header.workMode() != null) {
            html.append(" · ").append(esc(workMode(header.workMode())));
        }
        html.append("</p>");
        line(html, "E-mail", header.professionalEmail(), true);
        line(html, "Telefone/WhatsApp", header.phone(), true);
        line(html, "LinkedIn", header.linkedinUrl(), true);
        line(html, "GitHub", header.githubUrl(), true);
        line(html, "Portfólio", header.portfolioUrl(), true);
        line(html, "Outros", header.otherProfessionalLinks(), false);
        html.append("</td>");
        if (photoDataUri != null && !photoDataUri.isBlank()) {
            html.append("<td class=\"photo\"><img class=\"photo\" src=\"").append(photoDataUri).append("\" alt=\"\"/></td>");
        }
        html.append("</tr></table>");
        String technologies = technologies(document.skills());
        if (!technologies.isBlank()) {
            html.append("<p class=\"tech\"><strong>Tecnologias:</strong> ").append(esc(technologies)).append("</p>");
        }
        section(html, "Resumo profissional", esc(document.summary()));
        html.append("<h2>Experiência profissional</h2>");
        for (ResumeDocument.ExperienceLine job : document.experiences()) {
            html.append("<p class=\"job\">").append(esc(job.company())).append(" — ").append(esc(job.roleTitle()))
                    .append(" · ").append(esc(period(job))).append("</p>");
            if (job.projectLabel() != null && !job.projectLabel().isBlank()) {
                html.append("<p class=\"project\">Projeto: ").append(esc(job.projectLabel())).append("</p>");
            }
            if ("DETAILED".equals(job.detailLevel())) {
                html.append("<ul>");
                bullet(html, job.responsibilities());
                bullet(html, job.results());
                html.append("</ul>");
            } else if (job.responsibilities() != null && !job.responsibilities().isBlank()) {
                html.append("<p>").append(esc(firstSentence(job.responsibilities()))).append("</p>");
            }
        }
        groupedSkills(html, document.skills());
        html.append("<h2>Formação acadêmica</h2>");
        for (ResumeDocument.EducationLine education : document.education()) {
            html.append("<p><strong>").append(esc(join(education.course(), education.institution()))).append("</strong><br/>")
                    .append(esc(education.level())).append(" · ").append(esc(status(education.status())));
            if (education.endYear() != null) {
                html.append(" · ").append(education.endYear());
            }
            html.append("</p>");
        }
        if (!document.certifications().isEmpty()) {
            html.append("<h2>Cursos e certificações</h2>");
            for (ResumeDocument.CertificationLine item : document.certifications()) {
                html.append("<p>").append(esc(item.name()));
                if (item.issuer() != null) {
                    html.append(" — ").append(esc(item.issuer()));
                }
                if (item.year() != null) {
                    html.append(" · ").append(item.year());
                }
                html.append("</p>");
            }
        }
        if (!document.languages().isEmpty()) {
            html.append("<h2>Idiomas</h2><p>");
            html.append(document.languages().stream().map(ResumeHtmlRenderer::language).collect(Collectors.joining(" · ")));
            html.append("</p>");
        }
        if (!document.projects().isEmpty()) {
            html.append("<h2>Projetos</h2>");
            for (ResumeDocument.ProjectLine project : document.projects()) {
                html.append("<p><strong>").append(esc(project.name())).append("</strong>");
                if (project.personal()) {
                    html.append(" (projeto pessoal)");
                }
                if (project.description() != null) {
                    html.append("<br/>").append(esc(project.description()));
                }
                if (project.url() != null && !project.url().isBlank()) {
                    html.append("<br/><a href=\"").append(esc(project.url())).append("\">").append(esc(project.url())).append("</a>");
                }
                html.append("</p>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }

    private static void section(StringBuilder html, String title, String body) {
        html.append("<h2>").append(title).append("</h2>");
        if (body != null && !body.isBlank()) {
            html.append("<p>").append(body).append("</p>");
        }
    }

    private static void line(StringBuilder html, String label, String value, boolean link) {
        if (value == null || value.isBlank()) {
            return;
        }
        html.append("<p class=\"contact\">").append(label).append(": ");
        if (link && (value.startsWith("http") || value.contains("@"))) {
            String href = value.contains("@") && !value.startsWith("mailto:") ? "mailto:" + value : value;
            html.append("<a href=\"").append(esc(href)).append("\">").append(esc(value)).append("</a>");
        } else {
            html.append(esc(value));
        }
        html.append("</p>");
    }

    private static void bullet(StringBuilder html, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        for (String part : text.split("\\n+")) {
            if (!part.isBlank()) {
                html.append("<li>").append(esc(part.trim())).append("</li>");
            }
        }
    }

    private static void groupedSkills(StringBuilder html, List<ResumeDocument.SkillLine> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        html.append("<h2>Competências técnicas</h2>");
        skills.stream().collect(Collectors.groupingBy(ResumeDocument.SkillLine::category)).forEach((category, lines) ->
                html.append("<p><strong>").append(esc(categoryLabel(category))).append(":</strong> ")
                        .append(esc(lines.stream().map(ResumeDocument.SkillLine::name).collect(Collectors.joining(", "))))
                        .append("</p>"));
    }

    private static String technologies(List<ResumeDocument.SkillLine> skills) {
        if (skills == null) {
            return "";
        }
        return skills.stream()
                .filter(skill -> "TECHNICAL".equals(skill.category()) || "TOOL".equals(skill.category()))
                .map(ResumeDocument.SkillLine::name)
                .collect(Collectors.joining(", "));
    }

    private static String language(ResumeDocument.LanguageLine language) {
        StringBuilder line = new StringBuilder("<strong>").append(esc(language.name())).append(":</strong> ");
        line.append(esc(label(language.comprehension())));
        return line.toString();
    }

    private static String period(ResumeDocument.ExperienceLine job) {
        String start = month(job.startMonth()) + job.startYear();
        if (job.currentJob()) {
            return start + " – atual";
        }
        if (job.endYear() == null) {
            return start;
        }
        return start + " – " + month(job.endMonth()) + job.endYear();
    }

    private static String month(Integer month) {
        return month == null ? "" : String.format("%02d/", month);
    }

    private static String place(ResumeDocument.Header header) {
        if (header.city() == null && header.state() == null) {
            return "";
        }
        if (header.state() == null) {
            return header.city();
        }
        if (header.city() == null) {
            return header.state();
        }
        return header.city() + "/" + header.state();
    }

    private static String workMode(String mode) {
        return switch (mode) {
            case "REMOTE" -> "Disponível para trabalho remoto";
            case "HYBRID" -> "Disponível para trabalho híbrido";
            case "ONSITE" -> "Disponível para trabalho presencial";
            default -> "Disponibilidade flexível";
        };
    }

    private static String status(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "COMPLETED" -> "Concluído";
            case "IN_PROGRESS" -> "Cursando";
            case "INTERRUPTED" -> "Interrompido";
            default -> status;
        };
    }

    private static String categoryLabel(String category) {
        return switch (category) {
            case "TECHNICAL" -> "Técnicas";
            case "TOOL" -> "Ferramentas";
            case "DOMAIN" -> "Conhecimentos";
            case "BEHAVIORAL" -> "Comportamentais";
            default -> category;
        };
    }

    private static String label(String level) {
        if (level == null) {
            return "nível não informado";
        }
        return switch (level) {
            case "BASIC" -> "básico";
            case "INTERMEDIATE" -> "intermediário";
            case "ADVANCED" -> "avançado";
            case "FLUENT" -> "fluente";
            case "NATIVE" -> "nativo";
            default -> level;
        };
    }

    private static String join(String course, String institution) {
        if (course == null || course.isBlank()) {
            return institution == null ? "" : institution;
        }
        if (institution == null || institution.isBlank()) {
            return course;
        }
        return course + " — " + institution;
    }

    private static String firstSentence(String text) {
        String trimmed = text.trim();
        int breakAt = trimmed.indexOf('\n');
        if (breakAt > 0) {
            return trimmed.substring(0, breakAt);
        }
        return trimmed.length() > 220 ? trimmed.substring(0, 220) : trimmed;
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
