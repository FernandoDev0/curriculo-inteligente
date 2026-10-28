import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Entrar } from './entrar';

describe('Entrar', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Entrar],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('marks an empty required name as invalid', () => {
    const fixture = TestBed.createComponent(Entrar);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/auth/session').flush({
      authenticated: false,
      needsBootstrap: true,
      email: null,
    });
    fixture.detectChanges();

    const name = fixture.componentInstance.form.controls.fullName;
    name.setValue('');
    name.markAsTouched();
    name.updateValueAndValidity();

    expect(name.invalid).toBe(true);
    expect(name.hasError('required')).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Nome completo');
    http.verify();
  });
});
