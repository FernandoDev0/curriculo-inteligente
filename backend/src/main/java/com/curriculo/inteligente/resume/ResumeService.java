package com.curriculo.inteligente.resume;

import com.curriculo.inteligente.ai.InventionGuard;
import com.curriculo.inteligente.ai.LanguageModel;
import com.curriculo.inteligente.audit.AuditEvent;
import com.curriculo.inteligente.audit.AuditRepository;
import com.curriculo.inteligente.candidate.CandidateProfile;
import com.curriculo.inteligente.candidate.ProfileService;
import com.curriculo.inteligente.common.ApiException;
import com.curriculo.inteligente.common.PageResponse;
import com.curriculo.inteligente.common.Text;
import com.curriculo.inteligente.education.Certification;
import com.curriculo.inteligente.education.CertificationRepository;
import com.curriculo.inteligente.education.Education;
import com.curriculo.inteligente.education.EducationRepository;
import com.curriculo.inteligente.experience.Experience;
import com.curriculo.inteligente.experience.ExperienceRepository;
import com.curriculo.inteligente.experience.ExperienceSelectionPolicy;
import com.curriculo.inteligente.export.PdfExportService;
import com.curriculo.inteligente.export.ResumeDocument;
import com.curriculo.inteligente.interview.Interview;
import com.curriculo.inteligente.interview.InterviewMessage;
import com.curriculo.inteligente.interview.InterviewService;
import com.curriculo.inteligente.interview.QuestionBank;
import com.curriculo.inteligente.job.JobMatch;
import com.curriculo.inteligente.job.JobMatchRepository;
import com.curriculo.inteligente.job.JobPosting;
import com.curriculo.inteligente.job.JobRepository;
import com.curriculo.inteligente.setup.ApiKeyStore;
import com.curriculo.inteligente.skill.LanguageRepository;
import com.curriculo.inteligente.skill.LanguageSkill;
import com.curriculo.inteligente.skill.ProjectItem;
import com.curriculo.inteligente.skill.ProjectRepository;
import com.curriculo.inteligente.skill.Skill;
import com.curriculo.inteligente.skill.SkillRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    private static final String EXPERIENCE_VOICE = """
            Escreva na voz da própria pessoa, no pretérito perfeito, sem o pronome "eu".
            Cada linha começa com o verbo no passado: "Contribuí", "Mantive", "Utilizei", "Organizei", "Atendi".
            Errado: "Eu melhoro o fluxo da loja."
            Errado: "Minha responsabilidade era manter sempre visualmente limpo."
            Errado: "Eu utilizava equipamento para ver os preços da loja para o cliente."
            Certo: "Contribuí para a melhoria da organização e do fluxo de clientes no setor."
            Certo: "Mantive a organização, a limpeza e a apresentação visual das mercadorias."
            Certo: "Utilizei equipamentos de consulta para verificar preços e disponibilidade de produtos em estoque."
            Não use presente ("reponho", "melhoro", "utilizo").
            Não use imperfeito ("utilizava", "era", "fazia", "minha responsabilidade era").
            Não use infinitivo ("realizar a reposição").
            Não fale em terceira pessoa ("o candidato", "ele", "ela").
            Não use o nome da pessoa nem repita datas, anos ou tempo de casa.
            Um tópico por linha, concreto, só com o que está no relato.
            Não invente números, cargos, empresas, datas ou resultados.
            Sem introdução. Responda só os tópicos.
            """;

    private static final String SUMMARY_VOICE = """
            Escreva um resumo profissional de 90 a 140 palavras, no pretérito perfeito, sem o pronome "eu".
            Comece por verbos como "Atuei", "Contribuí", "Mantive", "Utilizei".
            Não use presente ("atuo", "melhoro") nem imperfeito ("utilizava", "era", "minha responsabilidade era").
            Não use infinitivo de anúncio de vaga nem terceira pessoa.
            Não cite o nome: ele já está no título.
            Não informe anos, datas, duração nem tempo de experiência: isso já está na experiência.
            Elabore a área e as atividades concretas. Use só os fatos do JSON.
            Evite "dinâmico" e "proativo". Sem título de seção. Responda só o parágrafo.
            """;

    private static final String HEADER_VOICE = """
            Reescreva o topo do currículo para um recrutador. Responda só um JSON com as chaves targetRole, city, state, phone e jobs.
            jobs é uma lista, na mesma ordem recebida, com company e roleTitle.
            O cargo desejado e o cargo da experiência ficam com iniciais maiúsculas e a grafia profissional. "repositor" vira "Repositor". Não invente senioridade nem outro ofício.
            A empresa só muda em maiúsculas e acentuação. Não troque o estabelecimento.
            A cidade fica com iniciais maiúsculas e preposição minúscula, como "São José do Rio Preto". Não troque a cidade.
            O estado é a sigla em maiúsculas.
            O telefone fica no formato (DD) 90000-0000, com os mesmos dígitos.
            Não invente números.
            """;

    public static final String INTERVIEW_NOTE = "origem:entrevista";
    private static final Pattern MONTH_YEAR = Pattern.compile("(0?[1-9]|1[0-2])\\s*[/.\\-]\\s*((?:19|20)\\d{2})");
    private static final Pattern YEAR = Pattern.compile("((?:19|20)\\d{2})");

    private final ResumeRepository resumes;
    private final ResumeVersionRepository versions;
    private final ProfileService profiles;
    private final ExperienceRepository experiences;
    private final EducationRepository educations;
    private final CertificationRepository certifications;
    private final SkillRepository skills;
    private final LanguageRepository languages;
    private final ProjectRepository projects;
    private final InterviewService interviews;
    private final JobRepository jobs;
    private final JobMatchRepository matches;
    private final LanguageModel model;
    private final ApiKeyStore keys;
    private final PdfExportService pdf;
    private final AuditRepository audit;
    private final JsonMapper json;

    public ResumeService(ResumeRepository resumes, ResumeVersionRepository versions, ProfileService profiles,
                         ExperienceRepository experiences, EducationRepository educations, CertificationRepository certifications,
                         SkillRepository skills, LanguageRepository languages, ProjectRepository projects,
                         InterviewService interviews, JobRepository jobs, JobMatchRepository matches, LanguageModel model,
                         ApiKeyStore keys, PdfExportService pdf, AuditRepository audit, JsonMapper json) {
        this.resumes = resumes;
        this.versions = versions;
        this.profiles = profiles;
        this.experiences = experiences;
        this.educations = educations;
        this.certifications = certifications;
        this.skills = skills;
        this.languages = languages;
        this.projects = projects;
        this.interviews = interviews;
        this.jobs = jobs;
        this.matches = matches;
        this.model = model;
        this.keys = keys;
        this.pdf = pdf;
        this.audit = audit;
        this.json = json;
    }

    @Transactional
    public ResumeDetail create(UUID userId, String title) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(title == null || title.isBlank() ? "Currículo" : title.trim());
        CandidateProfile profile = profiles.load(userId);
        resume.setTargetRole(profile.getTargetRole());
        return detail(resumes.save(resume), userId);
    }

    public PageResponse<ResumeDetail> list(UUID userId, Pageable pageable) {
        return PageResponse.of(resumes.findByUserId(userId, pageable), resume -> detail(resume, userId));
    }

    public ResumeDetail get(UUID userId, UUID id) {
        return detail(load(userId, id), userId);
    }

    @Transactional
    public ResumeDetail update(UUID userId, UUID id, ResumeWrite write) {
        Resume resume = load(userId, id);
        if (write.title() != null && !write.title().isBlank()) {
            resume.setTitle(write.title().trim());
        }
        resume.setTargetRole(write.targetRole());
        resume.setSummaryDraft(write.summaryDraft());
        if (write.templateCode() != null && !write.templateCode().isBlank()) {
            resume.setTemplateCode(write.templateCode());
        }
        resume.setIncludePhoto(write.includePhoto());
        if ("APPROVED".equals(resume.getStatus())) {
            resume.setStatus("DRAFT");
            resume.setApprovedAt(null);
        }
        return detail(resumes.save(resume), userId);
    }

    @Transactional
    public ResumeDetail draftFromInterview(UUID userId, UUID interviewId) {
        Interview interview = interviews.load(userId, interviewId);
        Map<String, String> answers = new LinkedHashMap<>();
        for (InterviewMessage message : interviews.history(interviewId)) {
            if ("USER".equals(message.getRole()) && !message.isSkipped()) {
                answers.put(message.getQuestionKey(), message.getContent());
            }
        }
        CandidateProfile profile = profiles.load(userId);
        profile.setFullName(value(answers, "full_name", profile.getFullName()));
        profile.setTargetRole(value(answers, "target_role", profile.getTargetRole()));
        profile.setCurrentProfession(blankToNull(answers.get("current_profession")));
        splitCity(profile, answers.get("city_state"));
        profile.setProfessionalEmail(blankToNull(answers.get("email")));
        profile.setPhone(blankToNull(answers.get("phone")));
        profile.setLinkedinUrl(blankToNull(answers.get("linkedin")));
        profile.setOtherProfessionalLinks(blankToNull(answers.get("other_links")));
        applyLinks(profile, answers.get("other_links"));
        profile.setWorkMode(workMode(answers.get("work_mode")));
        profile.setTargetOpportunity(blankToNull(answers.get("target_opportunity")));
        applyAge(profile, answers.get("age_choice"));
        boolean career = QuestionBank.affirmative(answers.get("career_change"));
        profile.setCareerChange(career);
        if (career) {
            profile.setDesiredProfession(blankToNull(answers.get("desired_profession")));
            profile.setTargetRole(value(answers, "desired_profession", profile.getTargetRole()));
            profile.setCareerChangeReason(blankToNull(answers.get("career_change_reason")));
            profile.setPortfolioUrl(blankToNull(answers.get("portfolio")));
        }
        profile.touch();
        List<Experience> previous = experiences.findByUserIdOrderBySortOrderAsc(userId).stream()
                .filter(item -> INTERVIEW_NOTE.equals(item.getNotes()))
                .toList();
        if (!previous.isEmpty()) {
            experiences.deleteAll(previous);
            experiences.flush();
        }
        int order = 0;
        for (int round = 1; round <= 12; round++) {
            String company = answers.get("exp:" + round + ":company");
            String role = answers.get("exp:" + round + ":role_title");
            if (company == null || role == null) {
                continue;
            }
            Period period = period(answers.get("exp:" + round + ":period"));
            if (period == null) {
                continue;
            }
            Experience experience = new Experience();
            experience.setUserId(userId);
            experience.setCompany(company.trim());
            experience.setRoleTitle(role.trim());
            experience.setStartYear(period.startYear);
            experience.setStartMonth(period.startMonth);
            experience.setEndYear(period.endYear);
            experience.setEndMonth(period.endMonth);
            experience.setCurrentJob(period.current);
            experience.setResponsibilities(narrative(answers, round));
            experience.setTools(blankToNull(answers.get("exp:" + round + ":tools")));
            experience.setResults(null);
            experience.setNotes(INTERVIEW_NOTE);
            experience.setSortOrder(order++);
            experiences.save(experience);
            suggestTools(userId, experience.getTools());
        }
        importEducation(userId, answers);
        importLanguages(userId, answers.get("languages"));
        if (certifications.findByUserIdOrderBySortOrderAsc(userId).isEmpty() && answers.get("courses") != null && !answers.get("courses").isBlank()) {
            Certification certification = new Certification();
            certification.setUserId(userId);
            certification.setName(answers.get("courses").trim());
            certifications.save(certification);
        }
        if (career && answers.get("personal_projects") != null && !answers.get("personal_projects").isBlank()
                && projects.findByUserIdOrderBySortOrderAsc(userId).isEmpty()) {
            ProjectItem project = new ProjectItem();
            project.setUserId(userId);
            project.setName("Projeto pessoal");
            project.setDescription(answers.get("personal_projects").trim());
            project.setUrl(blankToNull(answers.get("portfolio")));
            project.setPersonal(true);
            projects.save(project);
        }
        Resume resume = interview.getResumeId() == null
                ? null
                : resumes.findByIdAndUserId(interview.getResumeId(), userId).orElse(null);
        if (resume == null) {
            resume = new Resume();
            resume.setUserId(userId);
            resume.setTitle(profile.getTargetRole() == null || profile.getTargetRole().isBlank() ? "Currículo" : profile.getTargetRole());
        }
        resume.setTargetRole(profile.getTargetRole());
        resume.setStatus("DRAFT");
        resumes.save(resume);
        interview.setResumeId(resume.getId());
        return detail(resume, userId);
    }

    @Transactional
    public Improvement improveFromInterview(UUID userId, UUID interviewId) {
        requireAi(userId);
        ResumeDetail draft = draftFromInterview(userId, interviewId);
        polishTop(userId, draft.id());
        List<ImprovedPiece> pieces = new ArrayList<>();
        for (Experience experience : experiences.findByUserIdOrderBySortOrderAsc(userId)) {
            if (!INTERVIEW_NOTE.equals(experience.getNotes())) {
                continue;
            }
            String original = join(experience.getResponsibilities(), experience.getResults());
            if (original == null || original.isBlank()) {
                continue;
            }
            String revised = original;
            boolean keptOriginal = false;
            try {
                revised = model.complete(userId, "REVIEW", EXPERIENCE_VOICE, original, false).trim();
                InventionGuard.assertFaithful(original, revised);
                experience.setResponsibilities(asBullets(revised));
                experience.setResults(null);
                experiences.save(experience);
            } catch (RuntimeException ex) {
                keptOriginal = true;
                revised = original;
            }
            pieces.add(new ImprovedPiece(experience.getCompany(), experience.getRoleTitle(), original, revised, keptOriginal));
        }
        confirmNamedTools(userId);
        String summaryRevised;
        String summaryFailure = null;
        try {
            summaryRevised = regenerateSummary(userId, draft.id()).revised();
        } catch (RuntimeException ex) {
            summaryRevised = null;
            summaryFailure = ex.getMessage();
        }
        String note = summaryRevised == null
                ? "As experiências foram tratadas. O resumo não entrou: " + (summaryFailure == null ? "falha sem detalhe." : summaryFailure)
                : "O currículo foi preenchido com o resumo, as experiências, as ferramentas, a formação e os idiomas que você informou.";
        return new Improvement(draft.id(), summaryRevised, pieces, note);
    }

    public TextRevision review(UUID userId, UUID id, String section, UUID experienceId, String text) {
        load(userId, id);
        requireAi(userId);
        String source = text == null ? "" : text;
        String revised = model.complete(userId, "REVIEW", EXPERIENCE_VOICE, source, false).trim();
        InventionGuard.assertFaithful(source, revised);
        return new TextRevision(source, revised);
    }

    @Transactional
    public TextRevision regenerateSummary(UUID userId, UUID id) {
        Resume resume = load(userId, id);
        requireAi(userId);
        ResumeDocument document = document(userId, resume, false);
        String facts = json.writeValueAsString(document);
        String revised = model.complete(userId, "SUMMARY", SUMMARY_VOICE, facts, false).trim();
        revised = withoutName(revised, document.header().fullName());
        revised = InventionGuard.keepFaithful(facts, revised);
        if (revised.isBlank()) {
            throw new ApiException(422, "AI_INVALID_OUTPUT", "O resumo veio vazio depois de tirar o que não estava no relato.");
        }
        resume.setSummaryDraft(revised);
        resumes.save(resume);
        return new TextRevision(document.summary(), revised);
    }

    @Transactional
    public ResumeDetail approve(UUID userId, UUID id) {
        Resume resume = load(userId, id);
        CandidateProfile profile = profiles.load(userId);
        if (profile.getFullName() == null || profile.getFullName().isBlank() || resume.getTargetRole() == null || resume.getTargetRole().isBlank()) {
            throw new ApiException(400, "VALIDATION", "Nome e cargo desejado são obrigatórios antes da aprovação.");
        }
        if (resume.getSummaryDraft() != null) {
            resume.setSummaryApproved(resume.getSummaryDraft());
        }
        ResumeDocument document = document(userId, resume, true);
        ResumeVersion version = new ResumeVersion();
        version.setResumeId(resume.getId());
        version.setVersionNumber(versions.countByResumeId(resume.getId()) + 1);
        version.setSnapshot(json.writeValueAsString(document));
        versions.save(version);
        resume.setStatus("APPROVED");
        resume.setApprovedAt(Instant.now());
        resumes.save(resume);
        AuditEvent event = new AuditEvent();
        event.setUserId(userId);
        event.setAction("APPROVE");
        event.setResourceType("resume");
        event.setResourceId(resume.getId());
        audit.save(event);
        return detail(resume, userId);
    }

    public ResponseEntity<byte[]> preview(UUID userId, UUID id) {
        Resume resume = load(userId, id);
        return pdfResponse(render(userId, document(userId, resume, false), resume.isIncludePhoto()));
    }

    public ResponseEntity<byte[]> export(UUID userId, UUID id) {
        Resume resume = load(userId, id);
        if (!"APPROVED".equals(resume.getStatus())) {
            throw new ApiException(409, "NOT_APPROVED", "Aprove a prévia antes de exportar o arquivo definitivo.");
        }
        ResumeVersion version = versions.findFirstByResumeIdOrderByVersionNumberDesc(resume.getId())
                .orElseThrow(() -> new ApiException(409, "NOT_APPROVED", "Não há versão aprovada."));
        ResumeDocument document = json.readValue(version.getSnapshot(), ResumeDocument.class);
        PdfExportService.PdfFile file = render(userId, document, resume.isIncludePhoto() && document.header().hasPhoto());
        if (file.pages() != 1) {
            throw new ApiException(409, "PAGE_OVERFLOW", "O currículo aprovado não cabe em uma página.", file.pages(),
                    List.of("Encurte o resumo.", "Deixe experiências antigas como resumo.", "Remova itens menos ligados à vaga."));
        }
        return pdfResponse(file);
    }

    @Transactional
    public JobMatchResponse match(UUID userId, UUID resumeId, UUID jobId) {
        Resume resume = load(userId, resumeId);
        JobPosting job = jobs.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Vaga não encontrada."));
        String corpus = Text.plain(json.writeValueAsString(document(userId, resume, false)));
        List<String> demonstrated = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String term : terms(job.getDescription())) {
            if (corpus.contains(Text.plain(term))) {
                if (demonstrated.size() < 12) {
                    demonstrated.add(term);
                }
            } else if (missing.size() < 12) {
                missing.add(term);
            }
        }
        String suggestions = "A comparação só marca o que já está escrito no currículo. Nada foi inventado para cobrir um requisito ausente.";
        if (keys.resolve() != null && profiles.load(userId).getAiConsentAt() != null) {
            try {
                String phrase = model.complete(userId, "JOB_MATCH",
                        "Escreva uma orientação curta em português. Não invente experiência. Não diga que o currículo será aprovado por um ATS. Não inclua números novos.",
                        "Requisitos já citados: " + demonstrated + ". Requisitos sem comprovação: " + missing,
                        false);
                InventionGuard.assertFaithful(demonstrated + " " + missing, phrase);
                suggestions = phrase.trim();
            } catch (RuntimeException ignored) {
                // A lista lexical continua válida se a redação da IA falhar.
            }
        }
        JobMatch row = new JobMatch();
        row.setUserId(userId);
        row.setResumeId(resumeId);
        row.setJobId(jobId);
        row.setDemonstrated(json.writeValueAsString(demonstrated));
        row.setRelated(json.writeValueAsString(List.of()));
        row.setMissing(json.writeValueAsString(missing));
        row.setKeywords(json.writeValueAsString(demonstrated));
        row.setSuggestions(suggestions);
        matches.save(row);
        return new JobMatchResponse(demonstrated, List.of(), missing, demonstrated, suggestions);
    }

    @Transactional
    public JobPosting createJob(UUID userId, String title, String company, String description) {
        JobPosting job = new JobPosting();
        job.setUserId(userId);
        job.setTitle(title.trim());
        job.setCompany(company);
        job.setDescription(description.trim());
        return jobs.save(job);
    }

    public JobPosting job(UUID userId, UUID id) {
        return jobs.findByIdAndUserId(id, userId).orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Vaga não encontrada."));
    }

    private PdfExportService.PdfFile render(UUID userId, ResumeDocument document, boolean includePhoto) {
        byte[] photo = null;
        if (includePhoto && document.header().hasPhoto()) {
            try {
                photo = profiles.readPhoto(userId).bytes();
            } catch (ApiException ignored) {
                photo = null;
            }
        }
        PdfExportService.PdfFile file = pdf.render(document, photo);
        if (file.pages() > 1) {
            // A prévia ainda é entregue. A exportação definitiva recusa.
        }
        return file;
    }

    private ResponseEntity<byte[]> pdfResponse(PdfExportService.PdfFile file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"curriculo.pdf\"")
                .header("X-Page-Count", Integer.toString(file.pages()))
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.bytes());
    }

    private ResumeDetail detail(Resume resume, UUID userId) {
        ResumeDocument document = document(userId, resume, false);
        return new ResumeDetail(resume.getId(), resume.getTitle(), resume.getStatus(), resume.getTargetRole(),
                resume.getSummaryDraft(), resume.getSummaryApproved(), resume.getTemplateCode(), resume.isIncludePhoto(),
                resume.getApprovedAt(), document, resume.getCreatedAt(), resume.getUpdatedAt());
    }

    private ResumeDocument document(UUID userId, Resume resume, boolean approvedSummary) {
        CandidateProfile profile = profiles.load(userId);
        String summary = approvedSummary && resume.getSummaryApproved() != null ? resume.getSummaryApproved() : resume.getSummaryDraft();
        Map<UUID, String> levels = new LinkedHashMap<>();
        for (ExperienceSelectionPolicy.Selection selection : ExperienceSelectionPolicy.select(experiences.findByUserIdOrderBySortOrderAsc(userId))) {
            levels.put(selection.id(), selection.detailLevel());
        }
        List<ResumeDocument.ExperienceLine> lines = experiences.findByUserIdOrderBySortOrderAsc(userId).stream()
                .filter(item -> levels.containsKey(item.getId()))
                .map(item -> new ResumeDocument.ExperienceLine(item.getId().toString(), item.getCompany(), item.getRoleTitle(),
                        item.getStartYear(), item.getStartMonth(), item.getEndYear(), item.getEndMonth(), item.isCurrentJob(),
                        item.getProjectLabel(), item.getResponsibilities(), item.getTools(), item.getResults(), levels.get(item.getId())))
                .toList();
        List<ResumeDocument.SkillLine> skillLines = skills.findByUserIdOrderBySortOrderAsc(userId).stream()
                .filter(Skill::isConfirmed)
                .map(skill -> new ResumeDocument.SkillLine(skill.getName(), skill.getCategory()))
                .toList();
        List<ResumeDocument.EducationLine> educationLines = educations.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(item -> new ResumeDocument.EducationLine(item.getLevel(), item.getCourse(), item.getInstitution(), item.getStatus(), item.getStartYear(), item.getEndYear()))
                .toList();
        List<ResumeDocument.LanguageLine> languageLines = languages.findByUserId(userId).stream()
                .map(item -> new ResumeDocument.LanguageLine(item.getName(), item.getComprehension(), item.getReading(), item.getWriting(), item.getConversation()))
                .toList();
        List<ResumeDocument.ProjectLine> projectLines = projects.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(item -> new ResumeDocument.ProjectLine(item.getName(), item.getDescription(), item.getUrl(), item.isPersonal()))
                .toList();
        List<ResumeDocument.CertificationLine> certificationLines = certifications.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(item -> new ResumeDocument.CertificationLine(item.getName(), item.getIssuer(), item.getYear()))
                .toList();
        ResumeDocument.Header header = new ResumeDocument.Header(
                ResumeHeaderStyle.personName(profile.getFullName()),
                resume.getTargetRole(),
                ResumeHeaderStyle.placeName(profile.getCity()),
                ResumeHeaderStyle.state(profile.getState()),
                profile.getWorkMode(),
                profile.getProfessionalEmail(),
                ResumeHeaderStyle.phone(profile.getPhone()),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(), profile.getPortfolioUrl(), profile.getOtherProfessionalLinks(), profile.getAge(),
                profile.isIncludeAge(), profile.isPhotoApproved() && profile.getPhotoPath() != null);
        return new ResumeDocument(header, summary, lines, skillLines, educationLines, languageLines, projectLines, certificationLines);
    }

    private void polishTop(UUID userId, UUID resumeId) {
        Resume resume = load(userId, resumeId);
        CandidateProfile profile = profiles.load(userId);
        profile.setFullName(ResumeHeaderStyle.personName(profile.getFullName()));
        profile.setCity(ResumeHeaderStyle.placeName(profile.getCity()));
        profile.setState(ResumeHeaderStyle.state(profile.getState()));
        profile.setPhone(ResumeHeaderStyle.phone(profile.getPhone()));
        List<Experience> jobs = experiences.findByUserIdOrderBySortOrderAsc(userId).stream()
                .filter(item -> INTERVIEW_NOTE.equals(item.getNotes()))
                .toList();
        String source = json.writeValueAsString(java.util.Map.of(
                "targetRole", resume.getTargetRole() == null ? "" : resume.getTargetRole(),
                "city", profile.getCity() == null ? "" : profile.getCity(),
                "state", profile.getState() == null ? "" : profile.getState(),
                "phone", profile.getPhone() == null ? "" : profile.getPhone(),
                "jobs", jobs.stream().map(item -> java.util.Map.of(
                        "company", item.getCompany() == null ? "" : item.getCompany(),
                        "roleTitle", item.getRoleTitle() == null ? "" : item.getRoleTitle())).toList()));
        try {
            JsonNode node = json.readTree(stripFence(model.complete(userId, "HEADER", HEADER_VOICE, source, true)));
            String role = node.path("targetRole").asText("").trim();
            if (!role.isBlank()) {
                InventionGuard.assertFaithful(source, role);
                resume.setTargetRole(role);
                profile.setTargetRole(role);
            }
            String city = node.path("city").asText("").trim();
            if (ResumeHeaderStyle.sameWords(profile.getCity(), city)) {
                profile.setCity(ResumeHeaderStyle.placeName(city));
            }
            String state = node.path("state").asText("").trim();
            if (state.length() == 2) {
                profile.setState(state.toUpperCase(java.util.Locale.ROOT));
            }
            String phone = node.path("phone").asText("").trim();
            if (ResumeHeaderStyle.sameDigits(profile.getPhone(), phone)) {
                profile.setPhone(ResumeHeaderStyle.phone(phone));
            }
            JsonNode jobNodes = node.path("jobs");
            if (jobNodes.isArray()) {
                int index = 0;
                for (JsonNode jobNode : jobNodes) {
                    if (index >= jobs.size()) {
                        break;
                    }
                    Experience experience = jobs.get(index);
                    String company = jobNode.path("company").asText("").trim();
                    String roleTitle = jobNode.path("roleTitle").asText("").trim();
                    if (ResumeHeaderStyle.sameWords(experience.getCompany(), company)) {
                        experience.setCompany(company);
                    } else {
                        experience.setCompany(ResumeHeaderStyle.personName(experience.getCompany()));
                    }
                    if (!roleTitle.isBlank()) {
                        InventionGuard.assertFaithful(source + " " + experience.getRoleTitle(), roleTitle);
                        experience.setRoleTitle(roleTitle);
                    }
                    experiences.save(experience);
                    index++;
                }
            }
        } catch (RuntimeException ex) {
            for (Experience experience : jobs) {
                experience.setCompany(ResumeHeaderStyle.personName(experience.getCompany()));
                experiences.save(experience);
            }
        }
        profile.touch();
        resumes.save(resume);
    }

    private static String stripFence(String raw) {
        String text = raw == null ? "" : raw.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private void requireAi(UUID userId) {
        if (keys.resolve() == null) {
            throw new ApiException(409, "KEY_MISSING", "Configure a chave do Gemini para usar este recurso.");
        }
        if (profiles.load(userId).getAiConsentAt() == null) {
            throw new ApiException(409, "CONSENT_REQUIRED", "Antes de enviar o texto ao provedor de IA, registre o consentimento.");
        }
    }

    private Resume load(UUID userId, UUID id) {
        return resumes.findByIdAndUserId(id, userId).orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Currículo não encontrado."));
    }

    private void suggestTools(UUID userId, String tools) {
        if (tools == null) {
            return;
        }
        int order = skills.findByUserIdOrderBySortOrderAsc(userId).size();
        for (String part : tools.split(",")) {
            String name = part.trim();
            if (name.isBlank() || name.length() > 80) {
                continue;
            }
            var existing = skills.findByUserIdAndCategoryAndNameIgnoreCase(userId, "TOOL", name);
            if (existing.isPresent()) {
                Skill skill = existing.get();
                skill.setConfirmed(true);
                skill.setOrigin("USER");
                skills.save(skill);
                continue;
            }
            Skill skill = new Skill();
            skill.setUserId(userId);
            skill.setName(name);
            skill.setCategory("TOOL");
            skill.setConfirmed(true);
            skill.setOrigin("USER");
            skill.setSortOrder(order++);
            skills.save(skill);
        }
    }

    private void confirmNamedTools(UUID userId) {
        for (Skill skill : skills.findByUserIdOrderBySortOrderAsc(userId)) {
            if ("TOOL".equals(skill.getCategory()) && !skill.isConfirmed()) {
                skill.setConfirmed(true);
                skill.setOrigin("USER");
                skills.save(skill);
            }
        }
    }

    private void importEducation(UUID userId, Map<String, String> answers) {
        String highSchool = answers.get("high_school");
        String schooling = answers.get("schooling");
        if (highSchool != null || schooling != null) {
            String plainHigh = Text.plain(highSchool);
            String plainSchool = Text.plain(schooling);
            boolean mentioned = QuestionBank.affirmative(highSchool)
                    || plainSchool.contains("medio")
                    || plainSchool.contains("fundamental")
                    || plainHigh.contains("medio");
            if (mentioned) {
                String status = plainHigh.startsWith("nao") || plainSchool.contains("nao conclu") ? "INTERRUPTED" : "COMPLETED";
                String level = plainSchool.contains("fundamental") ? "Ensino fundamental" : "Ensino médio";
                addEducation(userId, level, level, null, status, null);
            }
        }
        String course = blankToNull(answers.get("course"));
        String institution = blankToNull(answers.get("institution"));
        if (course != null || institution != null) {
            String level = value(answers, "schooling", "Formação");
            String plainLevel = Text.plain(level);
            if (plainLevel.contains("medio") || plainLevel.contains("fundamental") || plainLevel.isBlank()) {
                level = course == null ? "Formação" : course;
            }
            addEducation(userId, level, course, institution,
                    educationStatus(answers.get("edu_status")), firstYear(answers.get("edu_year")));
        }
    }

    private void addEducation(UUID userId, String level, String course, String institution, String status, Integer endYear) {
        String marker = ((course == null ? "" : course) + "|" + (institution == null ? "" : institution)).toLowerCase(Locale.ROOT);
        boolean exists = educations.findByUserIdOrderBySortOrderAsc(userId).stream().anyMatch(item -> {
            String current = ((item.getCourse() == null ? "" : item.getCourse()) + "|" + (item.getInstitution() == null ? "" : item.getInstitution())).toLowerCase(Locale.ROOT);
            return current.equals(marker);
        });
        if (exists) {
            return;
        }
        Education education = new Education();
        education.setUserId(userId);
        education.setLevel(level == null || level.isBlank() ? "Formação" : level);
        education.setCourse(course);
        education.setInstitution(institution);
        education.setStatus(status == null ? "COMPLETED" : status);
        education.setEndYear(endYear);
        education.setSortOrder(educations.findByUserIdOrderBySortOrderAsc(userId).size());
        educations.save(education);
    }

    private void importLanguages(UUID userId, String answer) {
        if (answer == null || answer.isBlank()) {
            return;
        }
        for (String chunk : answer.split("[,;\\n]")) {
            String piece = chunk.trim();
            if (piece.length() < 3 || piece.length() > 40) {
                continue;
            }
            String plain = Text.plain(piece);
            String level = null;
            if (plain.contains("nativ")) {
                level = "NATIVE";
            } else if (plain.contains("fluent")) {
                level = "FLUENT";
            } else if (plain.contains("avanc")) {
                level = "ADVANCED";
            } else if (plain.contains("intermed")) {
                level = "INTERMEDIATE";
            } else if (plain.contains("basic")) {
                level = "BASIC";
            }
            String name = piece.replaceAll("(?i)(nativo|fluente|avançado|avancado|intermediário|intermediario|básico|basico)", "")
                    .replace(":", "")
                    .trim();
            if (name.length() < 3 || name.length() > 40) {
                continue;
            }
            boolean exists = languages.findByUserId(userId).stream()
                    .anyMatch(item -> item.getName() != null && item.getName().equalsIgnoreCase(name));
            if (exists) {
                continue;
            }
            LanguageSkill language = new LanguageSkill();
            language.setUserId(userId);
            language.setName(name);
            language.setComprehension(level);
            language.setReading(level);
            language.setWriting(level);
            language.setConversation(level);
            languages.save(language);
        }
    }

    private static void applyLinks(CandidateProfile profile, String links) {
        if (links == null || links.isBlank()) {
            return;
        }
        Matcher github = Pattern.compile("((?:https?://)?(?:www\\.)?github\\.com/[^\\s,;]+)").matcher(links);
        if (github.find() && (profile.getGithubUrl() == null || profile.getGithubUrl().isBlank())) {
            String url = github.group(1);
            profile.setGithubUrl(url.startsWith("http") ? url : "https://" + url);
        }
        Matcher site = Pattern.compile("(https?://[^\\s,;]+)").matcher(links);
        while (site.find()) {
            String url = site.group(1);
            if (!url.contains("github.com") && (profile.getPortfolioUrl() == null || profile.getPortfolioUrl().isBlank())) {
                profile.setPortfolioUrl(url);
            }
        }
    }

    private static String narrative(Map<String, String> answers, int round) {
        List<String> lines = new ArrayList<>();
        for (String key : List.of("day_in_the_life", "activities", "problems", "people", "responsibility", "autonomy", "results", "tools")) {
            String value = answers.get("exp:" + round + ":" + key);
            if (value != null && !value.isBlank()) {
                lines.add(value.trim());
            }
        }
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

    private static String withoutName(String text, String fullName) {
        if (text == null || fullName == null || fullName.isBlank()) {
            return text;
        }
        String cleaned = text.replace(fullName, " ");
        cleaned = cleaned.replaceAll("\\s{2,}", " ").replaceAll("^[\\s,.;:–-]+", "").trim();
        return cleaned;
    }

    private static String asBullets(String text) {
        String normalized = text.replace("\r", "").replace("•", "\n").replace("·", "\n");
        List<String> lines = new ArrayList<>();
        for (String line : normalized.split("\n")) {
            String cleaned = line.trim().replaceFirst("^[-*]\\s*", "");
            if (!cleaned.isBlank()) {
                lines.add(cleaned);
            }
        }
        return String.join("\n", lines);
    }

    private static List<String> terms(String description) {
        List<String> terms = new ArrayList<>();
        if (description == null) {
            return terms;
        }
        for (String raw : description.split("[\\n,;•]")) {
            String term = raw.trim();
            if (term.length() >= 4 && term.length() <= 80 && terms.stream().noneMatch(existing -> existing.equalsIgnoreCase(term))) {
                terms.add(term);
            }
        }
        return terms;
    }

    private static void splitCity(CandidateProfile profile, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String[] parts = value.split("[/,\\-]");
        profile.setCity(parts[0].trim());
        if (parts.length > 1) {
            profile.setState(parts[parts.length - 1].trim());
        }
    }

    private static void applyAge(CandidateProfile profile, String answer) {
        if (answer == null || !QuestionBank.affirmative(answer)) {
            profile.setIncludeAge(false);
            profile.setAge(null);
            return;
        }
        Matcher matcher = Pattern.compile("\\b(\\d{2})\\b").matcher(answer);
        if (matcher.find()) {
            int age = Integer.parseInt(matcher.group(1));
            if (age >= 14 && age <= 100) {
                profile.setIncludeAge(true);
                profile.setAge(age);
                return;
            }
        }
        profile.setIncludeAge(false);
    }

    private static String workMode(String answer) {
        String plain = Text.plain(answer);
        if (plain.contains("remot")) {
            return "REMOTE";
        }
        if (plain.contains("hibrid")) {
            return "HYBRID";
        }
        if (plain.contains("presencial")) {
            return "ONSITE";
        }
        if (plain.isBlank()) {
            return null;
        }
        return "FLEXIBLE";
    }

    private static String educationStatus(String answer) {
        String plain = Text.plain(answer);
        if (plain.contains("andamento") || plain.contains("cursando")) {
            return "IN_PROGRESS";
        }
        if (plain.contains("interromp")) {
            return "INTERRUPTED";
        }
        if (plain.contains("conclu")) {
            return "COMPLETED";
        }
        return "IN_PROGRESS";
    }

    private static Integer firstYear(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = YEAR.matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private static Period period(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = MONTH_YEAR.matcher(text);
        List<int[]> found = new ArrayList<>();
        while (matcher.find()) {
            found.add(new int[] {Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))});
        }
        boolean current = Text.plain(text).contains("atual") || Text.plain(text).contains("hoje") || Text.plain(text).contains("presente") || Text.plain(text).contains("momento");
        if (!found.isEmpty()) {
            int[] start = found.get(0);
            Integer endYear = null;
            Integer endMonth = null;
            if (!current && found.size() > 1) {
                endMonth = found.get(1)[0];
                endYear = found.get(1)[1];
            }
            return new Period(start[1], start[0], endYear, endMonth, current || found.size() == 1 && current);
        }
        Matcher year = YEAR.matcher(text);
        if (!year.find()) {
            return null;
        }
        int startYear = Integer.parseInt(year.group(1));
        Integer endYear = null;
        if (!current && year.find()) {
            endYear = Integer.parseInt(year.group(1));
        }
        return new Period(startYear, null, endYear, null, current);
    }

    private static String value(Map<String, String> answers, String key, String fallback) {
        String value = answers.get(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String join(String first, String second) {
        if (first == null || first.isBlank()) {
            return blankToNull(second);
        }
        if (second == null || second.isBlank()) {
            return first.trim();
        }
        return first.trim() + "\n" + second.trim();
    }

    private record Period(int startYear, Integer startMonth, Integer endYear, Integer endMonth, boolean current) {
    }

    public record ResumeDetail(UUID id, String title, String status, String targetRole, String summaryDraft, String summaryApproved,
                               String templateCode, boolean includePhoto, Instant approvedAt, ResumeDocument snapshot,
                               Instant createdAt, Instant updatedAt) {
    }

    public record ResumeWrite(String title, String targetRole, String summaryDraft, String templateCode, boolean includePhoto) {
    }

    public record TextRevision(String original, String revised) {
    }

    public record ImprovedPiece(String company, String roleTitle, String original, String revised, boolean keptOriginal) {
    }

    public record Improvement(UUID resumeId, String summary, List<ImprovedPiece> experiences, String note) {
    }

    public record JobMatchResponse(List<String> demonstrated, List<String> related, List<String> missing, List<String> keywords, String suggestions) {
    }
}
