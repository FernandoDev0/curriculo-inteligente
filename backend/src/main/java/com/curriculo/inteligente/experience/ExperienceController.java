package com.curriculo.inteligente.experience;

import com.curriculo.inteligente.common.ApiException;
import com.curriculo.inteligente.education.Certification;
import com.curriculo.inteligente.education.CertificationRepository;
import com.curriculo.inteligente.education.Education;
import com.curriculo.inteligente.education.EducationRepository;
import com.curriculo.inteligente.security.CurrentUser;
import com.curriculo.inteligente.skill.LanguageRepository;
import com.curriculo.inteligente.skill.LanguageSkill;
import com.curriculo.inteligente.skill.ProjectItem;
import com.curriculo.inteligente.skill.ProjectRepository;
import com.curriculo.inteligente.skill.Skill;
import com.curriculo.inteligente.skill.SkillRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ExperienceController {

    private final ExperienceRepository experiences;
    private final EducationRepository educations;
    private final CertificationRepository certifications;
    private final SkillRepository skills;
    private final LanguageRepository languages;
    private final ProjectRepository projects;
    private final CurrentUser currentUser;

    public ExperienceController(ExperienceRepository experiences, EducationRepository educations, CertificationRepository certifications,
                                SkillRepository skills, LanguageRepository languages, ProjectRepository projects, CurrentUser currentUser) {
        this.experiences = experiences;
        this.educations = educations;
        this.certifications = certifications;
        this.skills = skills;
        this.languages = languages;
        this.projects = projects;
        this.currentUser = currentUser;
    }

    @GetMapping("/experiences")
    public List<Experience> experiences() {
        return experiences.findByUserIdOrderBySortOrderAsc(currentUser.requireId());
    }

    @PostMapping("/experiences")
    public Experience createExperience(@RequestBody Experience body) {
        body.setUserId(currentUser.requireId());
        validateExperience(body);
        return experiences.save(body);
    }

    @PutMapping("/experiences/{id}")
    public Experience updateExperience(@PathVariable UUID id, @RequestBody Experience body) {
        Experience current = experiences.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Experiência não encontrada."));
        body.setUserId(current.getUserId());
        validateExperience(body);
        return experiences.save(copyExperience(current, body));
    }

    @DeleteMapping("/experiences/{id}")
    public void deleteExperience(@PathVariable UUID id) {
        Experience current = experiences.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Experiência não encontrada."));
        experiences.delete(current);
    }

    @PutMapping("/experiences/order")
    @Transactional
    public List<Experience> orderExperiences(@RequestBody OrderRequest request) {
        return reorder(request.ids(), experiences.findByUserIdOrderBySortOrderAsc(currentUser.requireId()));
    }

    @GetMapping("/education")
    public List<Education> education() { return educations.findByUserIdOrderBySortOrderAsc(currentUser.requireId()); }

    @PostMapping("/education")
    public Education createEducation(@RequestBody Education body) {
        body.setUserId(currentUser.requireId());
        return educations.save(body);
    }

    @PutMapping("/education/{id}")
    public Education updateEducation(@PathVariable UUID id, @RequestBody Education body) {
        Education current = educations.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Formação não encontrada."));
        current.setLevel(body.getLevel());
        current.setCourse(body.getCourse());
        current.setInstitution(body.getInstitution());
        current.setStatus(body.getStatus());
        current.setStartYear(body.getStartYear());
        current.setEndYear(body.getEndYear());
        current.setSortOrder(body.getSortOrder());
        return educations.save(current);
    }

    @DeleteMapping("/education/{id}")
    public void deleteEducation(@PathVariable UUID id) {
        educations.delete(educations.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Formação não encontrada.")));
    }

    @GetMapping("/certifications")
    public List<Certification> certifications() { return certifications.findByUserIdOrderBySortOrderAsc(currentUser.requireId()); }

    @PostMapping("/certifications")
    public Certification createCertification(@RequestBody Certification body) {
        body.setUserId(currentUser.requireId());
        return certifications.save(body);
    }

    @PutMapping("/certifications/{id}")
    public Certification updateCertification(@PathVariable UUID id, @RequestBody Certification body) {
        Certification current = certifications.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Certificação não encontrada."));
        current.setName(body.getName());
        current.setIssuer(body.getIssuer());
        current.setYear(body.getYear());
        current.setSortOrder(body.getSortOrder());
        return certifications.save(current);
    }

    @DeleteMapping("/certifications/{id}")
    public void deleteCertification(@PathVariable UUID id) {
        certifications.delete(certifications.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Certificação não encontrada.")));
    }

    @GetMapping("/skills")
    public List<Skill> skills() { return skills.findByUserIdOrderBySortOrderAsc(currentUser.requireId()); }

    @PostMapping("/skills")
    public Skill createSkill(@RequestBody Skill body) {
        body.setUserId(currentUser.requireId());
        return skills.save(body);
    }

    @PutMapping("/skills/{id}")
    public Skill updateSkill(@PathVariable UUID id, @RequestBody Skill body) {
        Skill current = skills.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Competência não encontrada."));
        current.setName(body.getName());
        current.setCategory(body.getCategory());
        current.setConfirmed(body.isConfirmed());
        current.setOrigin(body.getOrigin());
        current.setSortOrder(body.getSortOrder());
        return skills.save(current);
    }

    @DeleteMapping("/skills/{id}")
    public void deleteSkill(@PathVariable UUID id) {
        skills.delete(skills.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Competência não encontrada.")));
    }

    @PostMapping("/skills/suggest")
    @Transactional
    public List<Skill> suggest() {
        UUID userId = currentUser.requireId();
        int order = skills.findByUserIdOrderBySortOrderAsc(userId).size();
        for (Experience experience : experiences.findByUserIdOrderBySortOrderAsc(userId)) {
            if (experience.getTools() == null) {
                continue;
            }
            for (String part : experience.getTools().split(",")) {
                String name = part.trim();
                if (name.isBlank() || skills.findByUserIdAndCategoryAndNameIgnoreCase(userId, "TOOL", name).isPresent()) {
                    continue;
                }
                Skill skill = new Skill();
                skill.setUserId(userId);
                skill.setName(name);
                skill.setCategory("TOOL");
                skill.setConfirmed(false);
                skill.setOrigin("SUGGESTED");
                skill.setSortOrder(order++);
                skills.save(skill);
            }
        }
        return skills.findByUserIdOrderBySortOrderAsc(userId);
    }

    @GetMapping("/languages")
    public List<LanguageSkill> languages() { return languages.findByUserId(currentUser.requireId()); }

    @PostMapping("/languages")
    public LanguageSkill createLanguage(@RequestBody LanguageSkill body) {
        body.setUserId(currentUser.requireId());
        return languages.save(body);
    }

    @PutMapping("/languages/{id}")
    public LanguageSkill updateLanguage(@PathVariable UUID id, @RequestBody LanguageSkill body) {
        LanguageSkill current = languages.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Idioma não encontrado."));
        current.setName(body.getName());
        current.setComprehension(body.getComprehension());
        current.setReading(body.getReading());
        current.setWriting(body.getWriting());
        current.setConversation(body.getConversation());
        return languages.save(current);
    }

    @DeleteMapping("/languages/{id}")
    public void deleteLanguage(@PathVariable UUID id) {
        languages.delete(languages.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Idioma não encontrado.")));
    }

    @GetMapping("/projects")
    public List<ProjectItem> projects() { return projects.findByUserIdOrderBySortOrderAsc(currentUser.requireId()); }

    @PostMapping("/projects")
    public ProjectItem createProject(@RequestBody ProjectItem body) {
        body.setUserId(currentUser.requireId());
        return projects.save(body);
    }

    @PutMapping("/projects/{id}")
    public ProjectItem updateProject(@PathVariable UUID id, @RequestBody ProjectItem body) {
        ProjectItem current = projects.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Projeto não encontrado."));
        current.setName(body.getName());
        current.setDescription(body.getDescription());
        current.setUrl(body.getUrl());
        current.setPersonal(body.isPersonal());
        current.setSortOrder(body.getSortOrder());
        return projects.save(current);
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable UUID id) {
        projects.delete(projects.findByIdAndUserId(id, currentUser.requireId())
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Projeto não encontrado.")));
    }

    private List<Experience> reorder(List<UUID> ids, List<Experience> owned) {
        List<Experience> ordered = new ArrayList<>();
        int index = 0;
        for (UUID id : ids) {
            Experience experience = owned.stream().filter(item -> item.getId().equals(id)).findFirst()
                    .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Experiência não encontrada."));
            experience.setSortOrder(index++);
            ordered.add(experiences.save(experience));
        }
        return ordered;
    }

    private Experience copyExperience(Experience current, Experience body) {
        current.setCompany(body.getCompany());
        current.setRoleTitle(body.getRoleTitle());
        current.setStartYear(body.getStartYear());
        current.setStartMonth(body.getStartMonth());
        current.setEndYear(body.getEndYear());
        current.setEndMonth(body.getEndMonth());
        current.setCurrentJob(body.isCurrentJob());
        current.setProjectLabel(body.getProjectLabel());
        current.setResponsibilities(body.getResponsibilities());
        current.setTools(body.getTools());
        current.setResults(body.getResults());
        current.setNotes(body.getNotes());
        current.setSortOrder(body.getSortOrder());
        current.setDetailPreference(body.getDetailPreference() == null ? "AUTO" : body.getDetailPreference());
        return experiences.save(current);
    }

    private void validateExperience(Experience body) {
        if (body.getCompany() == null || body.getCompany().isBlank() || body.getRoleTitle() == null || body.getRoleTitle().isBlank()) {
            throw new ApiException(400, "VALIDATION", "Empresa e cargo são obrigatórios.");
        }
        if (body.getStartYear() < 1950 || body.getStartYear() > 2100) {
            throw new ApiException(400, "VALIDATION", "Ano de início inválido.");
        }
        if (body.getDetailPreference() == null) {
            body.setDetailPreference("AUTO");
        }
    }

    public record OrderRequest(List<UUID> ids) {
    }
}
