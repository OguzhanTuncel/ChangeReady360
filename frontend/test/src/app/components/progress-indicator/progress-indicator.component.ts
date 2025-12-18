import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-progress-indicator',
  standalone: true,
  templateUrl: './progress-indicator.component.html',
  styleUrl: './progress-indicator.component.css'
})
export class ProgressIndicatorComponent {
  @Input() current: number = 0;
  @Input() total: number = 0;

  get progressPercentage(): number {
    if (this.total === 0) return 0;
    return (this.current / this.total) * 100;
  }

  get steps(): number[] {
    return Array.from({ length: this.total }, (_, i) => i + 1);
  }
}

