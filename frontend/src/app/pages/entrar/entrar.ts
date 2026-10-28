import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { applyFieldErrors, readApiError } from '../../core/api-error';
import { SessionStore } from '../../core/session.store';

@Component({
  selector: 'app-entrar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './entrar.html',
})
export class Entrar {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  readonly sessions = inject(SessionStore);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly bootstrap = signal(false);

  readonly form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(254)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(10)],
    }),
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(160)] }),
  });

  constructor() {
    this.sessions.load().subscribe({
      next: (session) => {
        this.loading.set(false);
        if (session.authenticated) {
          void this.router.navigateByUrl('/');
          return;
        }
        this.setBootstrap(session.needsBootstrap);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível consultar a sessão.');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    this.form.markAllAsTouched();
    this.error.set(null);
    if (this.form.invalid || this.saving()) {
      return;
    }
    const { email, password, fullName } = this.form.getRawValue();
    this.saving.set(true);
    const request = this.bootstrap()
      ? this.api.bootstrap(email.trim(), password, fullName.trim())
      : this.api.login(email.trim(), password);
    request.subscribe({
      next: () => {
        this.form.controls.password.reset('');
        this.saving.set(false);
        void this.router.navigateByUrl('/entrevistas');
      },
      error: (err: unknown) => {
        const body = readApiError(err);
        applyFieldErrors(this.form, body.fields);
        this.error.set(body.message ?? 'Não foi possível entrar.');
        this.saving.set(false);
      },
    });
  }

  private setBootstrap(value: boolean): void {
    this.bootstrap.set(value);
    const name = this.form.controls.fullName;
    if (value) {
      name.setValidators([Validators.required, Validators.maxLength(160)]);
    } else {
      name.setValidators([Validators.maxLength(160)]);
    }
    name.updateValueAndValidity();
  }
}
