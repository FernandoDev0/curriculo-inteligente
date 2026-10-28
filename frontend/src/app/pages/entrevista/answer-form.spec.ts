import { TestBed } from '@angular/core/testing';
import { AnswerForm } from './answer-form';

describe('AnswerForm', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnswerForm],
    }).compileComponents();
  });

  it('does not show skip when canSkip is false', () => {
    const fixture = TestBed.createComponent(AnswerForm);
    fixture.componentRef.setInput('canSkip', false);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-testid="skip"]')).toBeNull();
  });

  it('emits the typed answer', () => {
    const fixture = TestBed.createComponent(AnswerForm);
    fixture.detectChanges();
    const answers: string[] = [];
    fixture.componentInstance.answered.subscribe((event) => answers.push(event.content));
    fixture.componentInstance.form.controls.content.setValue('Ana Souza');
    fixture.componentInstance.send();
    expect(answers).toEqual(['Ana Souza']);
  });

  it('shows skip when canSkip is true', () => {
    const fixture = TestBed.createComponent(AnswerForm);
    fixture.componentRef.setInput('canSkip', true);
    fixture.detectChanges();
    const skip = fixture.nativeElement.querySelector('[data-testid="skip"]') as HTMLElement | null;
    expect(skip).not.toBeNull();
    expect(skip?.textContent).toContain('Pular');
  });
});
