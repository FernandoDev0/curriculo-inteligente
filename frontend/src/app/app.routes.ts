import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { Aprovar } from './pages/aprovar/aprovar';
import { Configuracao } from './pages/configuracao/configuracao';
import { Conta } from './pages/conta/conta';
import { Curriculos } from './pages/curriculos/curriculos';
import { Diagnostico } from './pages/diagnostico/diagnostico';
import { Editar } from './pages/editar/editar';
import { Entrar } from './pages/entrar/entrar';
import { Entrevista } from './pages/entrevista/entrevista';
import { Entrevistas } from './pages/entrevistas/entrevistas';
import { Historico } from './pages/historico/historico';
import { Home } from './pages/home/home';
import { Previa } from './pages/previa/previa';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'entrar', component: Entrar },
  { path: 'configuracao', component: Configuracao },
  { path: 'diagnostico', component: Diagnostico },
  { path: 'entrevistas', component: Entrevistas, canActivate: [authGuard] },
  { path: 'entrevista/:id/historico', component: Historico, canActivate: [authGuard] },
  { path: 'entrevista/:id', component: Entrevista, canActivate: [authGuard] },
  { path: 'curriculos', component: Curriculos, canActivate: [authGuard] },
  { path: 'curriculo/:id/previa', component: Previa, canActivate: [authGuard] },
  { path: 'curriculo/:id/editar', component: Editar, canActivate: [authGuard] },
  { path: 'curriculo/:id/aprovar', component: Aprovar, canActivate: [authGuard] },
  { path: 'conta', component: Conta, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
