import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';
import { ApiErrorBody } from './models';

export function readApiError(error: unknown): ApiErrorBody {
  if (!(error instanceof HttpErrorResponse)) {
    return { message: 'Não foi possível concluir a operação.' };
  }
  return bodyFromPayload(error.error, error.status);
}

export async function readHttpError(error: unknown): Promise<ApiErrorBody> {
  if (!(error instanceof HttpErrorResponse)) {
    return { message: 'Não foi possível concluir a operação.' };
  }
  const payload: unknown = error.error;
  if (typeof Blob !== 'undefined' && payload instanceof Blob) {
    try {
      const text = await payload.text();
      if (!text.trim()) {
        return { message: statusFallback(error.status) };
      }
      return bodyFromPayload(JSON.parse(text) as unknown, error.status);
    } catch {
      return { message: statusFallback(error.status) };
    }
  }
  return bodyFromPayload(payload, error.status);
}

export function applyFieldErrors(form: FormGroup, fields: Record<string, string> | undefined): void {
  if (!fields) {
    return;
  }
  for (const key of Object.keys(fields)) {
    const control = form.get(key);
    const message = fields[key];
    if (!control || !message) {
      continue;
    }
    control.setErrors({ ...(control.errors ?? {}), server: message });
    control.markAsTouched();
  }
}

function bodyFromPayload(payload: unknown, status: number): ApiErrorBody {
  if (typeof payload === 'string' && payload.trim()) {
    return { message: payload };
  }
  if (payload && typeof payload === 'object' && !(typeof Blob !== 'undefined' && payload instanceof Blob)) {
    const body = payload as ApiErrorBody & { error?: string; detail?: string };
    if (!body.message) {
      body.message = body.detail || body.error || statusFallback(status);
    }
    if (status === 404) {
      body.message = 'Essa função ainda não está no backend que está no ar. Feche a janela do backend e rode rodar.cmd de novo.';
    }
    return body;
  }
  return { message: statusFallback(status) };
}

function statusFallback(status: number): string {
  if (status === 0) {
    return 'A API local não respondeu. Confira se ela está em 127.0.0.1:8080.';
  }
  return 'Não foi possível concluir a operação.';
}
