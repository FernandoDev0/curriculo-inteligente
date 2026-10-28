import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Api } from './core/api.service';
import { SessionStore } from './core/session.store';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  readonly sessions = inject(SessionStore);
  readonly loggingOut = signal(false);

  constructor() {
    this.sessions.load().subscribe({ error: () => undefined });
  }

  logout(): void {
    this.loggingOut.set(true);
    this.api.logout().subscribe({
      next: () => {
        this.sessions.clearLocal();
        this.loggingOut.set(false);
        void this.router.navigateByUrl('/');
      },
      error: () => {
        this.loggingOut.set(false);
      },
    });
  }
}
