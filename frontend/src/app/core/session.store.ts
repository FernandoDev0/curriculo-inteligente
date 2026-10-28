import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Session } from './models';

@Injectable({ providedIn: 'root' })
export class SessionStore {
  private readonly http = inject(HttpClient);
  readonly current = signal<Session | null>(null);
  readonly loaded = signal(false);
  readonly loadError = signal<string | null>(null);

  load(): Observable<Session> {
    return this.http.get<Session>('/api/v1/auth/session').pipe(
      tap({
        next: (session) => {
          this.current.set(session);
          this.loaded.set(true);
          this.loadError.set(null);
        },
        error: () => {
          this.loaded.set(true);
          this.loadError.set('Não foi possível consultar a sessão.');
        },
      }),
    );
  }

  clearLocal(): void {
    this.current.set({ authenticated: false, needsBootstrap: false, email: null });
    this.loaded.set(true);
  }
}
