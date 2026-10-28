import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import {
  blankToNull,
  detailPreferenceLabel,
  educationStatusLabel,
  formatPeriod,
  languageLevelLabel,
  numberOrNull,
  skillCategoryLabel,
} from '../../core/format';
import {
  Certification,
  CertificationWrite,
  Education,
  EducationWrite,
  Experience,
  ExperienceWrite,
  LanguageSkill,
  LanguageWrite,
  Profile,
  ProjectItem,
  ProjectWrite,
  ResumeDetail,
  Skill,
  SkillWrite,
  TextRevision,
} from '../../core/models';

type Kind = 'experience' | 'education' | 'certification' | 'skill' | 'language' | 'project';

@Component({
  selector: 'app-editar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './editar.html',
})
export class Editar {
  private readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly profile = signal<Profile | null>(null);
  readonly resume = signal<ResumeDetail | null>(null);
  readonly experiences = signal<Experience[]>([]);
  readonly educationItems = signal<Education[]>([]);
  readonly certifications = signal<Certification[]>([]);
  readonly skills = signal<Skill[]>([]);
  readonly languages = signal<LanguageSkill[]>([]);
  readonly projects = signal<ProjectItem[]>([]);
  readonly revision = signal<TextRevision | null>(null);

  readonly profileError = signal<string | null>(null);
  readonly profileNote = signal<string | null>(null);
  readonly resumeError = signal<string | null>(null);
  readonly resumeNote = signal<string | null>(null);
  readonly experienceError = signal<string | null>(null);
  readonly experienceNote = signal<string | null>(null);
  readonly educationError = signal<string | null>(null);
  readonly certificationError = signal<string | null>(null);
  readonly skillError = signal<string | null>(null);
  readonly skillNote = signal<string | null>(null);
  readonly languageError = signal<string | null>(null);
  readonly projectError = signal<string | null>(null);
  readonly sectionError = signal<string | null>(null);

  readonly savingProfile = signal(false);
  readonly savingResume = signal(false);
  readonly generating = signal(false);
  readonly savingExperience = signal(false);
  readonly savingEducation = signal(false);
  readonly savingCertification = signal(false);
  readonly savingSkill = signal(false);
  readonly savingLanguage = signal(false);
  readonly savingProject = signal(false);
  readonly suggesting = signal(false);
  readonly deleting = signal(false);
  readonly savingSkillId = signal<string | null>(null);

  readonly experienceOpen = signal(false);
  readonly experienceId = signal<string | null>(null);
  readonly educationOpen = signal(false);
  readonly educationId = signal<string | null>(null);
  readonly certificationOpen = signal(false);
  readonly certificationId = signal<string | null>(null);
  readonly languageOpen = signal(false);
  readonly languageId = signal<string | null>(null);
  readonly projectOpen = signal(false);
  readonly projectId = signal<string | null>(null);
  readonly pending = signal<{ kind: Kind; id: string; label: string } | null>(null);
  readonly resumeId = signal('');

  readonly profileForm = new FormGroup({
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    targetRole: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    currentProfession: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    city: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(80)] }),
    state: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(40)] }),
    professionalEmail: new FormControl('', { nonNullable: true, validators: [Validators.email, Validators.maxLength(254)] }),
    phone: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(40)] }),
    linkedinUrl: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
    githubUrl: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
    portfolioUrl: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
    otherProfessionalLinks: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    workMode: new FormControl('', { nonNullable: true }),
    targetOpportunity: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    includeAge: new FormControl(false, { nonNullable: true }),
    age: new FormControl<number | null>(null),
    careerChange: new FormControl(false, { nonNullable: true }),
    desiredProfession: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    careerChangeReason: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(1000)] }),
  });

  readonly resumeForm = new FormGroup({
    title: new FormControl('Currículo', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    targetRole: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    summaryDraft: new FormControl('', { nonNullable: true }),
    templateCode: new FormControl('REFERENCIA_A4', { nonNullable: true }),
    includePhoto: new FormControl(false, { nonNullable: true }),
  });

  readonly experienceForm = new FormGroup({
    company: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    roleTitle: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    startYear: new FormControl<number | null>(null, [Validators.required, Validators.min(1950), Validators.max(2100)]),
    startMonth: new FormControl<number | null>(null, [Validators.min(1), Validators.max(12)]),
    endYear: new FormControl<number | null>(null, [Validators.min(1950), Validators.max(2100)]),
    endMonth: new FormControl<number | null>(null, [Validators.min(1), Validators.max(12)]),
    currentJob: new FormControl(false, { nonNullable: true }),
    projectLabel: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(240)] }),
    responsibilities: new FormControl('', { nonNullable: true }),
    tools: new FormControl('', { nonNullable: true }),
    results: new FormControl('', { nonNullable: true }),
    notes: new FormControl('', { nonNullable: true }),
    detailPreference: new FormControl('AUTO', { nonNullable: true }),
  });

  readonly educationForm = new FormGroup({
    level: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(40)] }),
    course: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    institution: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    status: new FormControl('COMPLETED', { nonNullable: true, validators: [Validators.required] }),
    startYear: new FormControl<number | null>(null),
    endYear: new FormControl<number | null>(null),
  });

  readonly certificationForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(200)] }),
    issuer: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    year: new FormControl<number | null>(null),
  });

  readonly skillForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    category: new FormControl('TOOL', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly languageForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(40)] }),
    comprehension: new FormControl('', { nonNullable: true }),
    reading: new FormControl('', { nonNullable: true }),
    writing: new FormControl('', { nonNullable: true }),
    conversation: new FormControl('', { nonNullable: true }),
  });

  readonly projectForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    description: new FormControl('', { nonNullable: true }),
    url: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(300)] }),
    personal: new FormControl(true, { nonNullable: true }),
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id') ?? '';
      this.resumeId.set(id);
      if (id) {
        this.load(id);
      }
    });
  }

  load(id = this.resumeId()): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      profile: this.api.profile(),
      resume: this.api.resume(id),
      experiences: this.api.experiences(),
      education: this.api.education(),
      certifications: this.api.certifications(),
      skills: this.api.skills(),
      languages: this.api.languages(),
      projects: this.api.projects(),
    }).subscribe({
      next: (data) => {
        this.profile.set(data.profile);
        this.resume.set(data.resume);
        this.experiences.set(data.experiences);
        this.educationItems.set(data.education);
        this.certifications.set(data.certifications);
        this.skills.set(data.skills);
        this.languages.set(data.languages);
        this.projects.set(data.projects);
        this.patchProfile(data.profile);
        this.patchResume(data.resume);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível abrir a edição.');
        this.loading.set(false);
      },
    });
  }

  saveProfile(): void {
    const current = this.profile();
    this.profileForm.markAllAsTouched();
    this.profileError.set(null);
    this.profileNote.set(null);
    if (!current || this.profileForm.invalid || this.savingProfile()) {
      return;
    }
    const raw = this.profileForm.getRawValue();
    const age = numberOrNull(raw.age);
    if (raw.includeAge && (age == null || age < 14 || age > 100)) {
      this.profileForm.controls.age.setErrors({ server: 'A idade, se informada, fica entre 14 e 100.' });
      return;
    }
    const email = raw.professionalEmail.trim();
    if (email && this.profileForm.controls.professionalEmail.invalid) {
      return;
    }
    const body: Profile = {
      ...current,
      fullName: raw.fullName.trim(),
      targetRole: raw.targetRole.trim(),
      currentProfession: blankToNull(raw.currentProfession),
      city: blankToNull(raw.city),
      state: blankToNull(raw.state),
      professionalEmail: blankToNull(email),
      phone: blankToNull(raw.phone),
      linkedinUrl: blankToNull(raw.linkedinUrl),
      githubUrl: blankToNull(raw.githubUrl),
      portfolioUrl: blankToNull(raw.portfolioUrl),
      otherProfessionalLinks: blankToNull(raw.otherProfessionalLinks),
      workMode: blankToNull(raw.workMode),
      targetOpportunity: blankToNull(raw.targetOpportunity),
      includeAge: raw.includeAge,
      age: raw.includeAge ? age : null,
      careerChange: raw.careerChange,
      desiredProfession: raw.careerChange ? blankToNull(raw.desiredProfession) : null,
      careerChangeReason: raw.careerChange ? blankToNull(raw.careerChangeReason) : null,
    };
    this.savingProfile.set(true);
    this.api.saveProfile(body).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.patchProfile(profile);
        this.profileNote.set('Dados pessoais salvos.');
        this.savingProfile.set(false);
      },
      error: (err: unknown) => {
        this.profileError.set(readApiError(err).message ?? 'Não foi possível salvar os dados pessoais.');
        this.savingProfile.set(false);
      },
    });
  }

  saveResume(): void {
    const resume = this.resume();
    this.resumeForm.markAllAsTouched();
    this.resumeError.set(null);
    this.resumeNote.set(null);
    if (!resume || this.resumeForm.invalid || this.savingResume()) {
      return;
    }
    this.savingResume.set(true);
    this.api.updateResume(resume.id, this.resumeWrite()).subscribe({
      next: (updated) => {
        this.resume.set(updated);
        this.patchResume(updated);
        this.resumeNote.set('Resumo e dados do currículo salvos.');
        this.savingResume.set(false);
      },
      error: (err: unknown) => {
        this.resumeError.set(readApiError(err).message ?? 'Não foi possível salvar o currículo.');
        this.savingResume.set(false);
      },
    });
  }

  generateSummary(): void {
    const resume = this.resume();
    if (!resume || this.generating()) {
      return;
    }
    this.generating.set(true);
    this.resumeError.set(null);
    this.revision.set(null);
    this.api.generateSummary(resume.id).subscribe({
      next: (pair) => {
        this.revision.set({ original: pair?.original ?? '', revised: pair?.revised ?? '' });
        this.api.resume(resume.id).subscribe({
          next: (fresh) => {
            this.resume.set(fresh);
            this.patchResume(fresh);
            this.generating.set(false);
          },
          error: () => this.generating.set(false),
        });
      },
      error: (err: unknown) => {
        this.resumeError.set(readApiError(err).message ?? 'Não foi possível gerar o resumo.');
        this.generating.set(false);
      },
    });
  }

  openExperience(): void {
    this.experienceId.set(null);
    this.experienceForm.reset({
      company: '',
      roleTitle: '',
      startYear: null,
      startMonth: null,
      endYear: null,
      endMonth: null,
      currentJob: false,
      projectLabel: '',
      responsibilities: '',
      tools: '',
      results: '',
      notes: '',
      detailPreference: 'AUTO',
    });
    this.experienceOpen.set(true);
    this.experienceError.set(null);
  }

  editExperience(item: Experience): void {
    this.experienceId.set(item.id);
    this.experienceForm.reset({
      company: item.company,
      roleTitle: item.roleTitle,
      startYear: item.startYear,
      startMonth: item.startMonth,
      endYear: item.endYear,
      endMonth: item.endMonth,
      currentJob: item.currentJob,
      projectLabel: item.projectLabel ?? '',
      responsibilities: item.responsibilities ?? '',
      tools: item.tools ?? '',
      results: item.results ?? '',
      notes: item.notes ?? '',
      detailPreference: item.detailPreference || 'AUTO',
    });
    this.experienceOpen.set(true);
  }

  saveExperience(): void {
    this.experienceForm.markAllAsTouched();
    if (this.experienceForm.invalid || this.savingExperience()) {
      return;
    }
    const raw = this.experienceForm.getRawValue();
    const existing = this.experiences().find((item) => item.id === this.experienceId());
    const body: ExperienceWrite = {
      company: raw.company.trim(),
      roleTitle: raw.roleTitle.trim(),
      startYear: numberOrNull(raw.startYear) ?? 0,
      startMonth: numberOrNull(raw.startMonth),
      endYear: raw.currentJob ? null : numberOrNull(raw.endYear),
      endMonth: raw.currentJob ? null : numberOrNull(raw.endMonth),
      currentJob: raw.currentJob,
      projectLabel: blankToNull(raw.projectLabel),
      responsibilities: blankToNull(raw.responsibilities),
      tools: blankToNull(raw.tools),
      results: blankToNull(raw.results),
      notes: blankToNull(raw.notes),
      sortOrder: existing?.sortOrder ?? this.experiences().length,
      detailPreference: raw.detailPreference || 'AUTO',
    };
    this.savingExperience.set(true);
    this.experienceError.set(null);
    const id = this.experienceId();
    const request = id ? this.api.updateExperience(id, body) : this.api.createExperience(body);
    request.subscribe({
      next: (saved) => this.afterExperienceSave(saved),
      error: (err: unknown) => {
        this.experienceError.set(readApiError(err).message ?? 'Não foi possível salvar a experiência.');
        this.savingExperience.set(false);
      },
    });
  }

  moveExperience(index: number, delta: number): void {
    const next = this.shift(this.experiences(), index, delta);
    if (!next) {
      return;
    }
    this.experiences.set(next);
    this.api.orderExperiences(next.map((item) => item.id)).subscribe({
      error: (err: unknown) => {
        this.experienceError.set(readApiError(err).message ?? 'Não foi possível reordenar.');
        this.reloadExperiences();
      },
    });
  }

  openEducation(): void {
    this.educationId.set(null);
    this.educationForm.reset({ level: '', course: '', institution: '', status: 'COMPLETED', startYear: null, endYear: null });
    this.educationOpen.set(true);
  }

  editEducation(item: Education): void {
    this.educationId.set(item.id);
    this.educationForm.reset({
      level: item.level,
      course: item.course ?? '',
      institution: item.institution ?? '',
      status: item.status,
      startYear: item.startYear,
      endYear: item.endYear,
    });
    this.educationOpen.set(true);
  }

  saveEducation(): void {
    this.educationForm.markAllAsTouched();
    if (this.educationForm.invalid || this.savingEducation()) {
      return;
    }
    const raw = this.educationForm.getRawValue();
    const existing = this.educationItems().find((item) => item.id === this.educationId());
    const body: EducationWrite = {
      level: raw.level.trim(),
      course: blankToNull(raw.course),
      institution: blankToNull(raw.institution),
      status: raw.status,
      startYear: numberOrNull(raw.startYear),
      endYear: numberOrNull(raw.endYear),
      sortOrder: existing?.sortOrder ?? this.educationItems().length,
    };
    this.savingEducation.set(true);
    this.educationError.set(null);
    const id = this.educationId();
    const request = id ? this.api.updateEducation(id, body) : this.api.createEducation(body);
    request.subscribe({
      next: (saved) => {
        this.upsert(this.educationItems, saved);
        this.educationOpen.set(false);
        this.savingEducation.set(false);
      },
      error: (err: unknown) => {
        this.educationError.set(readApiError(err).message ?? 'Não foi possível salvar a formação.');
        this.savingEducation.set(false);
      },
    });
  }

  moveEducation(index: number, delta: number): void {
    const next = this.shift(this.educationItems(), index, delta);
    if (!next) {
      return;
    }
    this.educationItems.set(next);
    this.api.orderEducation(next.map((item) => item.id)).subscribe({
      error: (err: unknown) => {
        this.educationError.set(readApiError(err).message ?? 'Não foi possível reordenar.');
        this.reloadEducation();
      },
    });
  }

  openCertification(): void {
    this.certificationId.set(null);
    this.certificationForm.reset({ name: '', issuer: '', year: null });
    this.certificationOpen.set(true);
  }

  editCertification(item: Certification): void {
    this.certificationId.set(item.id);
    this.certificationForm.reset({ name: item.name, issuer: item.issuer ?? '', year: item.year });
    this.certificationOpen.set(true);
  }

  saveCertification(): void {
    this.certificationForm.markAllAsTouched();
    if (this.certificationForm.invalid || this.savingCertification()) {
      return;
    }
    const raw = this.certificationForm.getRawValue();
    const existing = this.certifications().find((item) => item.id === this.certificationId());
    const body: CertificationWrite = {
      name: raw.name.trim(),
      issuer: blankToNull(raw.issuer),
      year: numberOrNull(raw.year),
      sortOrder: existing?.sortOrder ?? this.certifications().length,
    };
    this.savingCertification.set(true);
    this.certificationError.set(null);
    const id = this.certificationId();
    const request = id ? this.api.updateCertification(id, body) : this.api.createCertification(body);
    request.subscribe({
      next: (saved) => {
        this.upsert(this.certifications, saved);
        this.certificationOpen.set(false);
        this.savingCertification.set(false);
      },
      error: (err: unknown) => {
        this.certificationError.set(readApiError(err).message ?? 'Não foi possível salvar a certificação.');
        this.savingCertification.set(false);
      },
    });
  }

  saveSkill(): void {
    this.skillForm.markAllAsTouched();
    if (this.skillForm.invalid || this.savingSkill()) {
      return;
    }
    const raw = this.skillForm.getRawValue();
    const body: SkillWrite = {
      name: raw.name.trim(),
      category: raw.category,
      confirmed: true,
      origin: 'USER',
      sortOrder: this.skills().length,
    };
    this.savingSkill.set(true);
    this.skillError.set(null);
    this.api.createSkill(body).subscribe({
      next: (saved) => {
        this.skills.update((items) => [...items, saved]);
        this.skillForm.reset({ name: '', category: 'TOOL' });
        this.savingSkill.set(false);
      },
      error: (err: unknown) => {
        this.skillError.set(readApiError(err).message ?? 'Não foi possível adicionar a competência.');
        this.savingSkill.set(false);
      },
    });
  }

  confirmSkill(skill: Skill, event: Event): void {
    const input = event.target as HTMLInputElement;
    const confirmed = input.checked;
    this.savingSkillId.set(skill.id);
    this.skillError.set(null);
    const { id, ...write } = skill;
    this.api.updateSkill(id, { ...write, confirmed }).subscribe({
      next: (saved) => {
        const next = saved?.id ? saved : { ...skill, confirmed };
        this.skills.update((items) => items.map((item) => (item.id === id ? next : item)));
        this.savingSkillId.set(null);
      },
      error: (err: unknown) => {
        input.checked = skill.confirmed;
        this.skillError.set(readApiError(err).message ?? 'Não foi possível atualizar a competência.');
        this.savingSkillId.set(null);
      },
    });
  }

  suggestSkills(): void {
    this.suggesting.set(true);
    this.skillError.set(null);
    this.skillNote.set(null);
    this.api.suggestSkills().subscribe({
      next: () => {
        this.api.skills().subscribe({
          next: (items) => {
            this.skills.set(items);
            this.skillNote.set('Sugestões atualizadas. Elas ficam desmarcadas até você confirmar.');
            this.suggesting.set(false);
          },
          error: (err: unknown) => {
            this.skillError.set(readApiError(err).message ?? 'Não foi possível reler as competências.');
            this.suggesting.set(false);
          },
        });
      },
      error: (err: unknown) => {
        this.skillError.set(readApiError(err).message ?? 'Não foi possível sugerir competências.');
        this.suggesting.set(false);
      },
    });
  }

  openLanguage(): void {
    this.languageId.set(null);
    this.languageForm.reset({ name: '', comprehension: '', reading: '', writing: '', conversation: '' });
    this.languageOpen.set(true);
  }

  editLanguage(item: LanguageSkill): void {
    this.languageId.set(item.id);
    this.languageForm.reset({
      name: item.name,
      comprehension: item.comprehension ?? '',
      reading: item.reading ?? '',
      writing: item.writing ?? '',
      conversation: item.conversation ?? '',
    });
    this.languageOpen.set(true);
  }

  saveLanguage(): void {
    this.languageForm.markAllAsTouched();
    if (this.languageForm.invalid || this.savingLanguage()) {
      return;
    }
    const raw = this.languageForm.getRawValue();
    const body: LanguageWrite = {
      name: raw.name.trim(),
      comprehension: blankToNull(raw.comprehension),
      reading: blankToNull(raw.reading),
      writing: blankToNull(raw.writing),
      conversation: blankToNull(raw.conversation),
    };
    this.savingLanguage.set(true);
    this.languageError.set(null);
    const id = this.languageId();
    const request = id ? this.api.updateLanguage(id, body) : this.api.createLanguage(body);
    request.subscribe({
      next: (saved) => {
        this.upsert(this.languages, saved);
        this.languageOpen.set(false);
        this.savingLanguage.set(false);
      },
      error: (err: unknown) => {
        this.languageError.set(readApiError(err).message ?? 'Não foi possível salvar o idioma.');
        this.savingLanguage.set(false);
      },
    });
  }

  openProject(): void {
    this.projectId.set(null);
    this.projectForm.reset({ name: '', description: '', url: '', personal: true });
    this.projectOpen.set(true);
  }

  editProject(item: ProjectItem): void {
    this.projectId.set(item.id);
    this.projectForm.reset({
      name: item.name,
      description: item.description ?? '',
      url: item.url ?? '',
      personal: item.personal,
    });
    this.projectOpen.set(true);
  }

  saveProject(): void {
    this.projectForm.markAllAsTouched();
    if (this.projectForm.invalid || this.savingProject()) {
      return;
    }
    const raw = this.projectForm.getRawValue();
    const existing = this.projects().find((item) => item.id === this.projectId());
    const body: ProjectWrite = {
      name: raw.name.trim(),
      description: blankToNull(raw.description),
      url: blankToNull(raw.url),
      personal: raw.personal,
      sortOrder: existing?.sortOrder ?? this.projects().length,
    };
    this.savingProject.set(true);
    this.projectError.set(null);
    const id = this.projectId();
    const request = id ? this.api.updateProject(id, body) : this.api.createProject(body);
    request.subscribe({
      next: (saved) => {
        this.upsert(this.projects, saved);
        this.projectOpen.set(false);
        this.savingProject.set(false);
      },
      error: (err: unknown) => {
        this.projectError.set(readApiError(err).message ?? 'Não foi possível salvar o projeto.');
        this.savingProject.set(false);
      },
    });
  }

  moveProjects(index: number, delta: number): void {
    const next = this.shift(this.projects(), index, delta);
    if (!next) {
      return;
    }
    this.projects.set(next);
    this.api.orderProjects(next.map((item) => item.id)).subscribe({
      error: (err: unknown) => {
        this.projectError.set(readApiError(err).message ?? 'Não foi possível reordenar.');
        this.reloadProjects();
      },
    });
  }

  askDelete(kind: Kind, id: string, label: string): void {
    this.pending.set({ kind, id, label });
    this.sectionError.set(null);
  }

  confirmDelete(): void {
    const pending = this.pending();
    if (!pending || this.deleting()) {
      return;
    }
    this.deleting.set(true);
    this.callDelete(pending).subscribe({
      next: () => {
        this.removeLocal(pending.kind, pending.id);
        this.pending.set(null);
        this.deleting.set(false);
      },
      error: (err: unknown) => {
        this.sectionError.set(readApiError(err).message ?? 'Não foi possível apagar.');
        this.deleting.set(false);
      },
    });
  }

  period(item: Experience): string {
    return formatPeriod(item);
  }

  category(value: string): string {
    return skillCategoryLabel(value);
  }

  educationStatus(value: string): string {
    return educationStatusLabel(value);
  }

  preference(value: string): string {
    return detailPreferenceLabel(value);
  }

  level(value: string | null): string {
    return languageLevelLabel(value);
  }

  private resumeWrite() {
    const raw = this.resumeForm.getRawValue();
    return {
      title: raw.title.trim(),
      targetRole: blankToNull(raw.targetRole),
      summaryDraft: blankToNull(raw.summaryDraft),
      templateCode: raw.templateCode || 'REFERENCIA_A4',
      includePhoto: raw.includePhoto,
    };
  }

  private patchProfile(profile: Profile): void {
    this.profileForm.reset({
      fullName: profile.fullName ?? '',
      targetRole: profile.targetRole ?? '',
      currentProfession: profile.currentProfession ?? '',
      city: profile.city ?? '',
      state: profile.state ?? '',
      professionalEmail: profile.professionalEmail ?? '',
      phone: profile.phone ?? '',
      linkedinUrl: profile.linkedinUrl ?? '',
      githubUrl: profile.githubUrl ?? '',
      portfolioUrl: profile.portfolioUrl ?? '',
      otherProfessionalLinks: profile.otherProfessionalLinks ?? '',
      workMode: profile.workMode ?? '',
      targetOpportunity: profile.targetOpportunity ?? '',
      includeAge: profile.includeAge ?? false,
      age: profile.age,
      careerChange: profile.careerChange ?? false,
      desiredProfession: profile.desiredProfession ?? '',
      careerChangeReason: profile.careerChangeReason ?? '',
    });
  }

  private patchResume(resume: ResumeDetail): void {
    this.resumeForm.reset({
      title: resume.title ?? 'Currículo',
      targetRole: resume.targetRole ?? '',
      summaryDraft: resume.summaryDraft ?? '',
      templateCode: resume.templateCode || 'REFERENCIA_A4',
      includePhoto: resume.includePhoto ?? false,
    });
  }

  private afterExperienceSave(saved: Experience): void {
    if (!saved?.id) {
      this.reloadExperiences();
      this.experienceOpen.set(false);
      this.savingExperience.set(false);
      return;
    }
    this.upsert(this.experiences, saved);
    this.experienceOpen.set(false);
    this.experienceNote.set('Experiência salva.');
    this.savingExperience.set(false);
  }

  private reloadExperiences(): void {
    this.api.experiences().subscribe({
      next: (items) => this.experiences.set(items),
      error: () => undefined,
    });
  }

  private reloadEducation(): void {
    this.api.education().subscribe({
      next: (items) => this.educationItems.set(items),
      error: () => undefined,
    });
  }

  private reloadProjects(): void {
    this.api.projects().subscribe({
      next: (items) => this.projects.set(items),
      error: () => undefined,
    });
  }

  private callDelete(pending: { kind: Kind; id: string }): Observable<unknown> {
    switch (pending.kind) {
      case 'experience':
        return this.api.deleteExperience(pending.id);
      case 'education':
        return this.api.deleteEducation(pending.id);
      case 'certification':
        return this.api.deleteCertification(pending.id);
      case 'skill':
        return this.api.deleteSkill(pending.id);
      case 'language':
        return this.api.deleteLanguage(pending.id);
      case 'project':
        return this.api.deleteProject(pending.id);
    }
  }

  private removeLocal(kind: Kind, id: string): void {
    const drop = <T extends { id: string }>(items: T[]) => items.filter((item) => item.id !== id);
    switch (kind) {
      case 'experience':
        this.experiences.update(drop);
        return;
      case 'education':
        this.educationItems.update(drop);
        return;
      case 'certification':
        this.certifications.update(drop);
        return;
      case 'skill':
        this.skills.update(drop);
        return;
      case 'language':
        this.languages.update(drop);
        return;
      case 'project':
        this.projects.update(drop);
        return;
    }
  }

  private upsert<T extends { id: string }>(
    list: { update: (fn: (items: T[]) => T[]) => void },
    saved: T,
  ): void {
    if (!saved?.id) {
      return;
    }
    list.update((items) => {
      const index = items.findIndex((item) => item.id === saved.id);
      if (index === -1) {
        return [...items, saved];
      }
      const copy = items.slice();
      copy[index] = saved;
      return copy;
    });
  }

  private shift<T>(items: readonly T[], index: number, delta: number): T[] | null {
    const target = index + delta;
    if (target < 0 || target >= items.length) {
      return null;
    }
    const copy = [...items];
    const [item] = copy.splice(index, 1);
    if (!item) {
      return null;
    }
    copy.splice(target, 0, item);
    return copy;
  }
}
