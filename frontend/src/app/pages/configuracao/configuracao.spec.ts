import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SetupStatus } from '../../core/models';
import { Configuracao } from './configuracao';

function status(state: SetupStatus['state'], message: string): SetupStatus {
  return {
    keyConfigured: state !== 'KEY_MISSING',
    keySource: state === 'KEY_MISSING' ? 'NONE' : 'FILE',
    model: 'gemini-3.5-flash',
    imageEditingAvailable: false,
    state,
    lastTestAt: null,
    lastTestMessage: message,
  };
}

describe('Configuracao', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Configuracao],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  function render(body: SetupStatus): HTMLElement {
    const fixture = TestBed.createComponent(Configuracao);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/setup/status').flush(body);
    fixture.detectChanges();
    http.verify();
    return fixture.nativeElement as HTMLElement;
  }

  it('renders the key-missing state from a mocked status object', () => {
    const element = render(status('KEY_MISSING', 'Nenhuma chave foi configurada.'));
    expect(element.querySelector('[data-state]')?.getAttribute('data-state')).toBe('KEY_MISSING');
    expect(element.textContent).toContain('Nenhuma chave foi configurada.');
    expect(element.textContent).toContain('Chave ausente');
  });

  it('renders the validated state from a mocked status object', () => {
    const element = render(status('VALIDATED', 'Conexão validada.'));
    expect(element.querySelector('[data-state]')?.getAttribute('data-state')).toBe('VALIDATED');
    expect(element.textContent).toContain('Conexão validada.');
    expect(element.textContent).not.toContain('Nenhuma chave foi configurada.');
  });
});
