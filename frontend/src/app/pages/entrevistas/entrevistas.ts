import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { formatDateTime, interviewStatusLabel, stageLabel } from '../../core/format';
import { InterviewSummary } from '../../core/models';

@Component({
  selector: 'app-entrevistas',
  imports: [RouterLink],
  templateUrl: './entrevistas.html',
})
export class Entrevistas {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly error = signal<string | null>(null);
  readonly items = signal<InterviewSummary[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);

  constructor() {
    this.load(0);
  }

  load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.interviews(page, 20).subscribe({
      next: (body) => {
        this.items.set(body.content);
        this.page.set(body.page);
        this.totalPages.set(body.totalPages);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível listar as entrevistas.');
        this.loading.set(false);
      },
    });
  }

  create(): void {
    this.creating.set(true);
    this.error.set(null);
    this.api.createInterview().subscribe({
      next: (interview) => {
        this.creating.set(false);
        void this.router.navigate(['/entrevista', interview.id]);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível abrir a entrevista.');
        this.creating.set(false);
      },
    });
  }

  status(value: string): string {
    return interviewStatusLabel(value);
  }

  stage(value: string): string {
    return stageLabel(value);
  }

  when(value: string | undefined): string {
    return formatDateTime(value);
  }
}
