import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { formatDateTime, resumeStatusLabel } from '../../core/format';
import { ResumeSummary } from '../../core/models';

@Component({
  selector: 'app-curriculos',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './curriculos.html',
})
export class Curriculos {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly error = signal<string | null>(null);
  readonly items = signal<ResumeSummary[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly form = new FormGroup({
    title: new FormControl('Currículo', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(160)],
    }),
  });

  constructor() {
    this.load(0);
  }

  load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.resumes(page, 20).subscribe({
      next: (body) => {
        this.items.set(body.content);
        this.page.set(body.page);
        this.totalPages.set(body.totalPages);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível listar os currículos.');
        this.loading.set(false);
      },
    });
  }

  create(): void {
    this.form.controls.title.markAsTouched();
    if (this.form.controls.title.invalid || this.creating()) {
      return;
    }
    this.creating.set(true);
    this.error.set(null);
    this.api.createResume(this.form.controls.title.value.trim()).subscribe({
      next: (resume) => {
        this.creating.set(false);
        void this.router.navigate(['/curriculo', resume.id, 'editar']);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível criar o currículo.');
        this.creating.set(false);
      },
    });
  }

  status(value: string): string {
    return resumeStatusLabel(value);
  }

  when(value: string | undefined): string {
    return formatDateTime(value);
  }
}
