import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SCALE_LABELS } from '../../models/wizard-data.model';

@Component({
  selector: 'app-question-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './question-item.component.html',
  styleUrl: './question-item.component.css'
})
export class QuestionItemComponent {
  @Input() questionId: string = '';
  @Input() questionText: string = '';
  @Input() value: number | undefined;
  @Output() valueChange = new EventEmitter<number>();

  scaleLabels = SCALE_LABELS;
  scaleValues = [1, 2, 3, 4, 5];

  onValueChange(value: number) {
    this.valueChange.emit(value);
  }
}

