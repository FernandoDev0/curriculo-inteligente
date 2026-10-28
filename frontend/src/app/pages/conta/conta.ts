import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError } from '../../core/api-error';
import { formatDateTime } from '../../core/format';
import { Profile } from '../../core/models';
import { SessionStore } from '../../core/session.store';

@Component({
  selector: 'app-conta',
  imports: [ReactiveFormsModule],
  templateUrl: './conta.html',
})
export class Conta {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  readonly sessions = inject(SessionStore);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly profile = signal<Profile | null>(null);
  readonly consenting = signal(false);
  readonly consentError = signal<string | null>(null);
  readonly uploading = signal(false);
  readonly photoError = signal<string | null>(null);
  readonly photoNote = signal<string | null>(null);
  readonly photoVersion = signal(0);
  readonly deleting = signal(false);
  readonly deleteError = signal<string | null>(null);
  readonly confirmation = new FormControl('', { nonNullable: true });

  constructor() {
    this.sessions.load().subscribe({ error: () => undefined });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.profile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível ler a conta.');
        this.loading.set(false);
      },
    });
  }

  consent(): void {
    this.consenting.set(true);
    this.consentError.set(null);
    this.api.consent().subscribe({
      next: (profile) => {
        if (profile?.aiConsentAt !== undefined) {
          this.profile.set(profile);
        } else {
          this.load();
        }
        this.consenting.set(false);
      },
      error: (err: unknown) => {
        this.consentError.set(readApiError(err).message ?? 'Não foi possível registrar o consentimento.');
        this.consenting.set(false);
      },
    });
  }

  onPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.photoError.set(null);
    this.photoNote.set(null);
    if (!file) {
      return;
    }
    const allowed = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowed.includes(file.type)) {
      this.photoError.set('Envie JPEG, PNG ou WebP.');
      input.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.photoError.set('A foto passa de 5 MB.');
      input.value = '';
      return;
    }
    this.uploading.set(true);
    this.api.uploadPhoto(file).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.photoVersion.update((value) => value + 1);
        this.photoNote.set('Foto enviada. Ela só entra no currículo se você aprovar e marcar a inclusão na edição.');
        this.uploading.set(false);
        input.value = '';
      },
      error: (err: unknown) => {
        this.photoError.set(readApiError(err).message ?? 'Não foi possível enviar a foto.');
        this.uploading.set(false);
        input.value = '';
      },
    });
  }

  approvePhoto(): void {
    this.uploading.set(true);
    this.photoError.set(null);
    this.api.approvePhoto().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.photoNote.set('Foto aprovada para uso no currículo.');
        this.uploading.set(false);
      },
      error: (err: unknown) => {
        this.photoError.set(readApiError(err).message ?? 'Não foi possível aprovar a foto.');
        this.uploading.set(false);
      },
    });
  }

  removePhoto(): void {
    this.uploading.set(true);
    this.photoError.set(null);
    this.api.deletePhoto().subscribe({
      next: () => {
        this.photoVersion.update((value) => value + 1);
        this.photoNote.set('Foto removida.');
        this.uploading.set(false);
        this.load();
      },
      error: (err: unknown) => {
        this.photoError.set(readApiError(err).message ?? 'Não foi possível remover a foto.');
        this.uploading.set(false);
      },
    });
  }

  deleteAccount(): void {
    if (this.confirmation.value !== 'APAGAR' || this.deleting()) {
      this.deleteError.set('Digite APAGAR para confirmar.');
      return;
    }
    this.deleting.set(true);
    this.deleteError.set(null);
    this.api.deleteAccount().subscribe({
      next: () => {
        this.sessions.load().subscribe({
          next: () => void this.router.navigateByUrl('/'),
          error: () => void this.router.navigateByUrl('/'),
        });
      },
      error: (err: unknown) => {
        this.deleteError.set(readApiError(err).message ?? 'Não foi possível apagar a conta.');
        this.deleting.set(false);
      },
    });
  }

  when(value: string | null | undefined): string {
    return formatDateTime(value);
  }

  photoSrc(): string {
    return `/api/v1/profile/photo?v=${this.photoVersion()}`;
  }
}
