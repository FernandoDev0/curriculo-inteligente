import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { readApiError } from '../../core/api-error';
import { SessionStore } from '../../core/session.store';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
})
export class Home {
  readonly sessions = inject(SessionStore);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.sessions.load().subscribe({
      next: () => this.loading.set(false),
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível consultar a sessão.');
        this.loading.set(false);
      },
    });
  }
}
