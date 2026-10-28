import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { SessionStore } from './session.store';

export const authGuard: CanActivateFn = () => {
  const sessions = inject(SessionStore);
  const router = inject(Router);
  return sessions.load().pipe(
    map((session) => (session.authenticated ? true : router.createUrlTree(['/entrar']))),
    catchError(() => of(router.createUrlTree(['/entrar']))),
  );
};
