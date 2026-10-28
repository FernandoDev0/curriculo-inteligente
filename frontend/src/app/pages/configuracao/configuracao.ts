import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { formatDateTime, safeKeySource, setupStateView } from '../../core/format';
import { SetupStatus } from '../../core/models';

@Component({
  selector: 'app-configuracao',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './configuracao.html',
})
export class Configuracao {
  private readonly api = inject(Api);

  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly status = signal<SetupStatus | null>(null);
  readonly testing = signal(false);
  readonly savingKey = signal(false);
  readonly removingKey = signal(false);
  readonly actionError = signal<string | null>(null);
  readonly actionNote = signal<string | null>(null);

  readonly apiKey = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.minLength(10)],
  });

  readonly displayState = computed(() => (this.testing() ? 'TESTING' : (this.status()?.state ?? '')));
  readonly view = computed(() => setupStateView(this.displayState()));

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.api.setupStatus().subscribe({
      next: (status) => {
        this.remember(status);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.loadError.set(readApiError(err).message ?? 'Não foi possível ler a configuração.');
        this.loading.set(false);
      },
    });
  }

  saveKey(): void {
    this.actionError.set(null);
    this.actionNote.set(null);
    this.apiKey.markAsTouched();
    if (this.apiKey.invalid || this.savingKey()) {
      return;
    }
    const apiKey = this.apiKey.value.trim();
    this.apiKey.reset('');
    this.savingKey.set(true);
    this.api.saveApiKey(apiKey).subscribe({
      next: (status) => {
        this.remember(status);
        this.actionNote.set('A chave foi enviada e o campo foi limpo. A resposta não traz a chave de volta.');
        this.savingKey.set(false);
      },
      error: (err: unknown) => {
        this.actionError.set(readApiError(err).message ?? 'Não foi possível gravar a chave.');
        this.savingKey.set(false);
      },
    });
  }

  removeKey(): void {
    this.actionError.set(null);
    this.actionNote.set(null);
    this.removingKey.set(true);
    this.api.deleteApiKey().subscribe({
      next: (status) => {
        this.remember(status);
        this.actionNote.set('A chave gravada pelo formulário foi removida. Uma variável de ambiente, se existir, permanece.');
        this.removingKey.set(false);
      },
      error: (err: unknown) => {
        this.actionError.set(readApiError(err).message ?? 'Não foi possível remover a chave.');
        this.removingKey.set(false);
      },
    });
  }

  test(): void {
    this.actionError.set(null);
    this.actionNote.set(null);
    this.testing.set(true);
    this.api.testConnection().subscribe({
      next: (status) => {
        this.remember(status);
        this.testing.set(false);
      },
      error: (err: unknown) => {
        this.actionError.set(readApiError(err).message ?? 'O teste de conexão falhou.');
        this.testing.set(false);
        this.reloadQuiet();
      },
    });
  }

  sourceLabel(): string {
    return safeKeySource(this.status()?.keySource);
  }

  when(value: string | null | undefined): string {
    return formatDateTime(value);
  }

  private remember(status: SetupStatus): void {
    const safe: SetupStatus = {
      keyConfigured: status.keyConfigured,
      keySource: status.keySource,
      model: status.model,
      imageEditingAvailable: status.imageEditingAvailable,
      state: status.state,
      lastTestAt: status.lastTestAt,
      lastTestMessage: status.lastTestMessage,
    };
    this.status.set(safe);
  }

  private reloadQuiet(): void {
    this.api.setupStatus().subscribe({
      next: (status) => this.remember(status),
      error: () => undefined,
    });
  }
}
