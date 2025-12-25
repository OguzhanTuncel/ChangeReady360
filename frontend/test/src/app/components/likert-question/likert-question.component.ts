import { Component, Input, Output, EventEmitter, signal, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { LikertValue } from '../../models/survey.model';

@Component({
  selector: 'app-likert-question',
  standalone: true,
  imports: [CommonModule, FormsModule, MatRadioModule, MatFormFieldModule],
  templateUrl: './likert-question.component.html',
  styleUrl: './likert-question.component.css'
})
export class LikertQuestionComponent implements OnInit, OnChanges {
  @Input() questionId!: string;
  @Input() questionText!: string;
  @Input() value: LikertValue = null;
  @Input() reverse: boolean = false;
  @Input() disabled: boolean = false;
  
  @Output() valueChange = new EventEmitter<LikertValue>();

  selectedValue = signal<LikertValue>(null);

  ngOnInit() {
    this.selectedValue.set(this.value);
  }

  ngOnChanges() {
    this.selectedValue.set(this.value);
  }

  onValueChange(value: LikertValue) {
    this.selectedValue.set(value);
    this.valueChange.emit(value);
  }

  getLikertLabels(): { value: LikertValue; label: string }[] {
    return [
      { value: 1 as LikertValue, label: 'Stimme gar nicht zu' },
      { value: 2 as LikertValue, label: 'Stimme eher nicht zu' },
      { value: 3 as LikertValue, label: 'Weder noch' },
      { value: 4 as LikertValue, label: 'Stimme eher zu' },
      { value: 5 as LikertValue, label: 'Stimme voll zu' }
    ];
  }
}

