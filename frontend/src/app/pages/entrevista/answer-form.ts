import { Component, effect, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-answer-form',
  imports: [ReactiveFormsModule],
  templateUrl: './answer-form.html',
})
export class AnswerForm {
  readonly canSkip = input(false);
  readonly disabled = input(false);
  readonly nonce = input(0);
  readonly answered = output<{ content: string; skip: boolean }>();

  readonly form = new FormGroup({
    content: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    effect(() => {
      if (this.nonce() > 0) {
        this.form.controls.content.reset('');
      }
    });
  }

  send(): void {
    const value = this.form.controls.content.value.trim();
    if (!value || this.disabled()) {
      this.form.controls.content.markAsTouched();
      return;
    }
    this.answered.emit({ content: value, skip: false });
  }

  skip(): void {
    if (!this.canSkip() || this.disabled()) {
      return;
    }
    this.answered.emit({ content: '', skip: true });
  }
}
