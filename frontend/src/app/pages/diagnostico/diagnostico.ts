import { Component, computed, inject, signal } from '@angular/core';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { formatDateTime, safeKeySource, setupStateView } from '../../core/format';
import { SetupStatus } from '../../core/models';

@Component({
  selector: 'app-diagnostico',
  templateUrl: './diagnostico.html',
})
export class Diagnostico {
  private readonly api = inject(Api);
  readonly loading = signal(true);
  readonly testing = signal(false);
  readonly error = signal<string | null>(null);
  readonly status = signal<SetupStatus | null>(null);
  readonly displayState = computed(() => (this.testing() ? 'TESTING' : (this.status()?.state ?? '')));
  readonly view = computed(() => setupStateView(this.displayState()));

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.setupStatus().subscribe({
      next: (status) => {
        this.remember(status);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível ler o diagnóstico.');
        this.loading.set(false);
      },
    });
  }

  test(): void {
    this.error.set(null);
    this.testing.set(true);
    this.api.testConnection().subscribe({
      next: (status) => {
        this.remember(status);
        this.testing.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'O teste de conexão falhou.');
        this.testing.set(false);
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
    this.status.set({
      keyConfigured: status.keyConfigured,
      keySource: status.keySource,
      model: status.model,
      imageEditingAvailable: status.imageEditingAvailable,
      state: status.state,
      lastTestAt: status.lastTestAt,
      lastTestMessage: status.lastTestMessage,
    });
  }
}
