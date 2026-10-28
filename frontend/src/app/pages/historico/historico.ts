import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { questionLabel } from '../../core/format';
import { Interview, InterviewMessage } from '../../core/models';

@Component({
  selector: 'app-historico',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './historico.html',
})
export class Historico {
  private readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly note = signal<string | null>(null);
  readonly interview = signal<Interview | null>(null);
  readonly selectedId = signal<string | null>(null);
  readonly id = signal('');
  readonly form = new FormGroup({
    correction: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });
  readonly userMessages = computed(() =>
    (this.interview()?.messages ?? []).filter((message) => message.role === 'USER'),
  );

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
        this.interview.set({ ...interview, messages: interview.messages ?? [] });
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível ler o histórico.');
        this.loading.set(false);
      },
    });
  }

  edit(message: InterviewMessage): void {
    this.selectedId.set(message.id);
    this.form.controls.correction.setValue(message.skipped ? '' : message.content);
    this.note.set(null);
    this.error.set(null);
  }

  save(): void {
    const messageId = this.selectedId();
    this.form.controls.correction.markAsTouched();
    if (!messageId || this.form.controls.correction.invalid || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    this.note.set(null);
    this.api.correctMessage(this.id(), messageId, this.form.controls.correction.value.trim()).subscribe({
      next: (interview) => {
        if (interview?.messages) {
          this.interview.set({ ...interview, messages: interview.messages ?? [] });
        } else {
          this.load();
        }
        this.note.set('A resposta foi corrigida. O estado da entrevista foi recalculado a partir dela.');
        this.saving.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível corrigir a resposta.');
        this.saving.set(false);
      },
    });
  }

  label(key: string | null): string {
    return questionLabel(key);
  }
}
