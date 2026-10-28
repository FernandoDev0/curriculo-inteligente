import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError, readHttpError } from '../../core/api-error';
import { downloadBlob } from '../../core/download';
import { blankToNull, resumeStatusLabel } from '../../core/format';
import { ApiErrorBody, Experience, JobMatch, ResumeDetail, TextRevision } from '../../core/models';

@Component({
  selector: 'app-aprovar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './aprovar.html',
})
export class Aprovar {
  private readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly resume = signal<ResumeDetail | null>(null);
  readonly approving = signal(false);
  readonly approveError = signal<string | null>(null);
  readonly approveNote = signal<string | null>(null);
  readonly exporting = signal(false);
  readonly exportError = signal<string | null>(null);
  readonly exportHints = signal<string[]>([]);
  readonly exportPageCount = signal<number | null>(null);
  readonly exported = signal(false);
  readonly reviewing = signal(false);
  readonly reviewError = signal<string | null>(null);
  readonly revision = signal<TextRevision | null>(null);
  readonly applying = signal(false);
  readonly experiences = signal<Experience[]>([]);
  readonly loadingExperiences = signal(false);
  readonly matching = signal(false);
  readonly matchError = signal<string | null>(null);
  readonly match = signal<JobMatch | null>(null);

  readonly reviewForm = new FormGroup({
    section: new FormControl<'SUMMARY' | 'EXPERIENCE'>('SUMMARY', { nonNullable: true }),
    experienceId: new FormControl('', { nonNullable: true }),
    text: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly jobForm = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    company: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.load(id);
      }
    });
  }

  load(id = this.resume()?.id ?? ''): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.resume(id).subscribe({
      next: (resume) => {
        this.resume.set(resume);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível abrir o currículo.');
        this.loading.set(false);
      },
    });
  }

  approve(): void {
    const resume = this.resume();
    if (!resume || this.approving()) {
      return;
    }
    this.approving.set(true);
    this.approveError.set(null);
    this.approveNote.set(null);
    this.api.approveResume(resume.id).subscribe({
      next: (updated) => {
        if (updated?.id) {
          this.resume.set(updated);
        } else {
          this.load(resume.id);
        }
        this.approveNote.set('Currículo aprovado. A exportação definitiva já pode ser pedida.');
        this.approving.set(false);
      },
      error: (err: unknown) => {
        this.approveError.set(readApiError(err).message ?? 'Não foi possível aprovar.');
        this.approving.set(false);
      },
    });
  }

  exportPdf(): void {
    const resume = this.resume();
    if (!resume || this.exporting()) {
      return;
    }
    if (resume.status !== 'APPROVED') {
      this.exportError.set('A exportação definitiva exige o currículo aprovado.');
      this.exportHints.set([]);
      this.exportPageCount.set(null);
      this.exported.set(false);
      return;
    }
    this.exporting.set(true);
    this.exportError.set(null);
    this.exportHints.set([]);
    this.exported.set(false);
    this.api.exportPdf(resume.id).subscribe({
      next: (response) => {
        const saved = downloadBlob(response.body, 'curriculo.pdf');
        this.exporting.set(false);
        if (!saved) {
          this.exportError.set('A API respondeu, mas o navegador não entregou o arquivo.');
          return;
        }
        this.exported.set(true);
      },
      error: (err: unknown) => {
        const payload = err instanceof HttpErrorResponse ? err.error : null;
        if (typeof Blob !== 'undefined' && payload instanceof Blob) {
          void this.readExportFailure(err);
          return;
        }
        this.applyExportFailure(readApiError(err));
      },
    });
  }

  onReviewSection(): void {
    if (this.reviewForm.controls.section.value === 'EXPERIENCE' && this.experiences().length === 0) {
      this.loadExperiences();
    }
  }

  loadExperiences(): void {
    this.loadingExperiences.set(true);
    this.reviewError.set(null);
    this.api.experiences().subscribe({
      next: (items) => {
        this.experiences.set(items);
        this.loadingExperiences.set(false);
      },
      error: (err: unknown) => {
        this.reviewError.set(readApiError(err).message ?? 'Não foi possível ler as experiências.');
        this.loadingExperiences.set(false);
      },
    });
  }

  useExperienceText(): void {
    const found = this.experiences().find((item) => item.id === this.reviewForm.controls.experienceId.value);
    if (found?.responsibilities) {
      this.reviewForm.controls.text.setValue(found.responsibilities);
    }
  }

  review(): void {
    const resume = this.resume();
    this.reviewForm.markAllAsTouched();
    if (!resume || this.reviewForm.invalid || this.reviewing()) {
      return;
    }
    const raw = this.reviewForm.getRawValue();
    if (raw.section === 'EXPERIENCE' && !raw.experienceId) {
      this.reviewError.set('Escolha a experiência que será revisada.');
      return;
    }
    this.reviewing.set(true);
    this.reviewError.set(null);
    this.revision.set(null);
    this.api
      .review(resume.id, {
        section: raw.section,
        text: raw.text,
        ...(raw.section === 'EXPERIENCE' ? { experienceId: raw.experienceId } : {}),
      })
      .subscribe({
        next: (pair) => {
          this.revision.set({ original: pair.original ?? '', revised: pair.revised ?? '' });
          this.reviewing.set(false);
        },
        error: (err: unknown) => {
          this.reviewError.set(readApiError(err).message ?? 'Não foi possível revisar o texto.');
          this.reviewing.set(false);
        },
      });
  }

  applyRevision(): void {
    const resume = this.resume();
    const pair = this.revision();
    if (!resume || !pair || this.applying()) {
      return;
    }
    this.applying.set(true);
    this.reviewError.set(null);
    if (this.reviewForm.controls.section.value === 'SUMMARY') {
      this.api
        .updateResume(resume.id, {
          title: resume.title,
          targetRole: resume.targetRole,
          summaryDraft: pair.revised,
          templateCode: resume.templateCode || 'REFERENCIA_A4',
          includePhoto: resume.includePhoto,
        })
        .subscribe({
          next: (updated) => {
            this.resume.set(updated?.id ? updated : { ...resume, summaryDraft: pair.revised });
            this.applying.set(false);
          },
          error: (err: unknown) => {
            this.reviewError.set(readApiError(err).message ?? 'Não foi possível guardar o resumo.');
            this.applying.set(false);
          },
        });
      return;
    }
    const experience = this.experiences().find((item) => item.id === this.reviewForm.controls.experienceId.value);
    if (!experience) {
      this.reviewError.set('Escolha a experiência antes de guardar a revisão.');
      this.applying.set(false);
      return;
    }
    const { id, ...write } = experience;
    this.api.updateExperience(id, { ...write, responsibilities: pair.revised }).subscribe({
      next: (saved) => {
        this.experiences.update((items) => items.map((item) => (item.id === id ? saved : item)));
        this.applying.set(false);
      },
      error: (err: unknown) => {
        this.reviewError.set(readApiError(err).message ?? 'Não foi possível guardar a experiência.');
        this.applying.set(false);
      },
    });
  }

  runMatch(): void {
    const resume = this.resume();
    this.jobForm.markAllAsTouched();
    if (!resume || this.jobForm.invalid || this.matching()) {
      return;
    }
    const raw = this.jobForm.getRawValue();
    this.matching.set(true);
    this.matchError.set(null);
    this.match.set(null);
    this.api
      .createJob({
        title: raw.title.trim(),
        company: blankToNull(raw.company),
        description: raw.description.trim(),
      })
      .subscribe({
        next: (job) => {
          if (!job?.id) {
            this.matchError.set('A vaga não foi gravada: a API não devolveu o identificador.');
            this.matching.set(false);
            return;
          }
          this.api.jobMatch(resume.id, job.id).subscribe({
            next: (match) => {
              this.match.set({
                demonstrated: match.demonstrated ?? [],
                related: match.related ?? [],
                missing: match.missing ?? [],
                keywords: match.keywords ?? [],
                suggestions: match.suggestions ?? '',
              });
              this.matching.set(false);
            },
            error: (err: unknown) => {
              this.matchError.set(readApiError(err).message ?? 'Não foi possível comparar com a vaga.');
              this.matching.set(false);
            },
          });
        },
        error: (err: unknown) => {
          this.matchError.set(readApiError(err).message ?? 'Não foi possível gravar a vaga.');
          this.matching.set(false);
        },
      });
  }

  status(value: string | undefined): string {
    return resumeStatusLabel(value);
  }

  private applyExportFailure(body: ApiErrorBody): void {
    this.exportError.set(body.message ?? 'Não foi possível exportar o PDF.');
    this.exportHints.set(body.hints ?? []);
    this.exportPageCount.set(typeof body.pageCount === 'number' ? body.pageCount : null);
    this.exporting.set(false);
    this.exported.set(false);
  }

  private async readExportFailure(err: unknown): Promise<void> {
    this.applyExportFailure(await readHttpError(err));
  }
}
