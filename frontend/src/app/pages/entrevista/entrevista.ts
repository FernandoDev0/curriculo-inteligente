import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { formatDateTime, interviewStatusLabel, questionLabel, stageLabel } from '../../core/format';
import { collectedFrom } from '../../core/interview-view';
import { Interview, InterviewImprovement } from '../../core/models';
import { AnswerForm } from './answer-form';

@Component({
  selector: 'app-entrevista',
  imports: [RouterLink, AnswerForm],
  templateUrl: './entrevista.html',
})
export class Entrevista {
  private readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly sending = signal(false);
  readonly pausing = signal(false);
  readonly drafting = signal(false);
  readonly improving = signal(false);
  readonly consented = signal(false);
  readonly improvement = signal<InterviewImprovement | null>(null);
  readonly confirmingDiscard = signal(false);
  readonly discarding = signal(false);
  readonly interview = signal<Interview | null>(null);
  readonly nonce = signal(0);
  readonly id = signal('');

  readonly collected = computed(() => collectedFrom(this.interview()?.messages));

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id') ?? '';
      this.id.set(id);
      if (id) {
        this.load(id);
      }
    });
  }

  load(id = this.id()): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.interview(id).subscribe({
      next: (interview) => {
        this.interview.set(this.normalize(interview));
        this.loading.set(false);
        if (interview.status === 'COMPLETED' || interview.status === 'PAUSED') {
          this.api.profile().subscribe({
            next: (profile) => this.consented.set(profile.aiConsentAt != null),
            error: () => this.consented.set(false),
          });
        }
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível abrir a entrevista.');
        this.loading.set(false);
      },
    });
  }

  onAnswer(event: { content: string; skip: boolean }): void {
    const current = this.interview();
    if (!current || this.sending()) {
      return;
    }
    if (event.skip && current.currentQuestion?.canSkip !== true) {
      return;
    }
    this.sending.set(true);
    this.error.set(null);
    this.api.sendMessage(current.id, event.content, event.skip).subscribe({
      next: (interview) => {
        this.interview.set(this.normalize(interview));
        this.nonce.update((value) => value + 1);
        this.sending.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível registrar a resposta.');
        this.sending.set(false);
      },
    });
  }

  pause(): void {
    this.pausing.set(true);
    this.error.set(null);
    this.api.pauseInterview(this.id()).subscribe({
      next: () => {
        this.pausing.set(false);
        this.load();
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível pausar.');
        this.pausing.set(false);
      },
    });
  }

  resume(): void {
    this.pausing.set(true);
    this.error.set(null);
    this.api.resumeInterview(this.id()).subscribe({
      next: () => {
        this.pausing.set(false);
        this.load();
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível retomar.');
        this.pausing.set(false);
      },
    });
  }

  improve(): void {
    if (this.improving()) {
      return;
    }
    this.improving.set(true);
    this.error.set(null);
    const run = () => {
      this.api.improveInterview(this.id()).subscribe({
        next: (result) => {
          this.improvement.set(result);
          this.improving.set(false);
        },
        error: (err: unknown) => {
          this.error.set(readApiError(err).message ?? 'A IA não conseguiu melhorar o currículo.');
          this.improving.set(false);
        },
      });
    };
    if (this.consented()) {
      run();
      return;
    }
    this.api.consent().subscribe({
      next: () => {
        this.consented.set(true);
        run();
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível registrar o consentimento.');
        this.improving.set(false);
      },
    });
  }

  draft(): void {
    this.drafting.set(true);
    this.error.set(null);
    this.api.resumeDraft(this.id()).subscribe({
      next: (resume) => {
        this.drafting.set(false);
        if (resume?.id) {
          void this.router.navigate(['/curriculo', resume.id, 'editar']);
          return;
        }
        this.error.set('A API não devolveu o currículo.');
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível gravar o rascunho.');
        this.drafting.set(false);
      },
    });
  }

  discard(): void {
    this.discarding.set(true);
    this.error.set(null);
    this.api.deleteInterview(this.id()).subscribe({
      next: () => {
        this.discarding.set(false);
        void this.router.navigateByUrl('/entrevistas');
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível descartar a entrevista.');
        this.discarding.set(false);
      },
    });
  }

  status(value: string | undefined): string {
    return interviewStatusLabel(value);
  }

  stage(value: string | undefined): string {
    return stageLabel(value);
  }

  label(key: string | null): string {
    return questionLabel(key);
  }

  when(value: string | undefined): string {
    return formatDateTime(value);
  }

  role(value: string): string {
    return value === 'USER' ? 'Você' : 'Pergunta';
  }

  private normalize(interview: Interview): Interview {
    return {
      ...interview,
      messages: interview.messages ?? [],
      currentQuestion: interview.currentQuestion ?? null,
      progressPercent: interview.progressPercent ?? 0,
    };
  }
}
