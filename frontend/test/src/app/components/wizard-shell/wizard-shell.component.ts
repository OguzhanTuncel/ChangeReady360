import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProgressIndicatorComponent } from '../progress-indicator/progress-indicator.component';

@Component({
  selector: 'app-wizard-shell',
  standalone: true,
  imports: [CommonModule, RouterModule, ProgressIndicatorComponent],
  templateUrl: './wizard-shell.component.html',
  styleUrl: './wizard-shell.component.css'
})
export class WizardShellComponent {
  @Input() currentStep: number = 1;
  @Input() totalSteps: number = 1;
  @Input() title: string = '';
  @Input() showBack: boolean = true;
  @Input() showNext: boolean = true;
  @Input() nextDisabled: boolean = false;
  @Input() nextLabel: string = 'Weiter';
  @Output() back = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();

  onBack() {
    this.back.emit();
  }

  onNext() {
    if (!this.nextDisabled) {
      this.next.emit();
    }
  }
}

