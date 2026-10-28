import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ResumeDetail } from '../../core/models';
import { Aprovar } from './aprovar';

function resume(status: string): ResumeDetail {
  return {
    id: 'r1',
    title: 'Currículo',
    status,
    targetRole: 'Padeiro',
    summaryDraft: 'Preparo pães.',
    summaryApproved: null,
    templateCode: 'REFERENCIA_A4',
    includePhoto: false,
    approvedAt: null,
    snapshot: null,
  };
}

describe('Aprovar', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Aprovar],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: 'r1' })),
            snapshot: { paramMap: convertToParamMap({ id: 'r1' }) },
          },
        },
      ],
    }).compileComponents();
  });

  it('does not call export when the resume status is DRAFT', () => {
    const fixture = TestBed.createComponent(Aprovar);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/resumes/r1').flush(resume('DRAFT'));
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="export-pdf"]') as HTMLButtonElement;
    button.click();
    fixture.detectChanges();

    http.expectNone('/api/v1/resumes/r1/export/pdf');
    expect(fixture.nativeElement.textContent).toContain('A exportação definitiva exige o currículo aprovado.');
    http.verify();
  });

  it('shows the API error message when export fails', async () => {
    const fixture = TestBed.createComponent(Aprovar);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/resumes/r1').flush(resume('APPROVED'));
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="export-pdf"]') as HTMLButtonElement;
    button.click();
    const request = http.expectOne('/api/v1/resumes/r1/export/pdf');
    request.flush(
      new Blob(
        [
          JSON.stringify({
            code: 'PAGE_OVERFLOW',
            message: 'O currículo passou de uma página.',
            hints: ['Encurte o resumo.'],
            pageCount: 2,
          }),
        ],
        { type: 'application/json' },
      ),
      { status: 409, statusText: 'Conflict' },
    );
    await fixture.whenStable();
    let text = '';
    for (let attempt = 0; attempt < 25; attempt += 1) {
      await new Promise((resolve) => setTimeout(resolve, 20));
      fixture.detectChanges();
      text = fixture.nativeElement.textContent as string;
      if (text.includes('O currículo passou de uma página.')) {
        break;
      }
    }
    expect(text).toContain('O currículo passou de uma página.');
    expect(text).toContain('Encurte o resumo.');
    http.verify();
  });
});
