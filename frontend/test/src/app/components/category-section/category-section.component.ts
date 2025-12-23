import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule, MatCardHeader, MatCardTitle, MatCardContent } from '@angular/material/card';
import { SurveyCategory, SurveyQuestion, LikertValue, ParticipantType } from '../../models/survey.model';
import { LikertQuestionComponent } from '../likert-question/likert-question.component';

@Component({
  selector: 'app-category-section',
  standalone: true,
  imports: [CommonModule, MatCardModule, LikertQuestionComponent],
  templateUrl: './category-section.component.html',
  styleUrl: './category-section.component.css'
})
export class CategorySectionComponent {
  @Input() category!: SurveyCategory;
  @Input() participantType!: ParticipantType;
  @Input() answers: Map<string, LikertValue> = new Map();
  @Input() disabled: boolean = false;
  
  @Output() answerChange = new EventEmitter<{ questionId: string; value: LikertValue }>();

  getVisibleQuestions(subcategory: { name: string; questions: SurveyQuestion[] }): SurveyQuestion[] {
    if (this.participantType === 'PMA') {
      return subcategory.questions;
    }
    return subcategory.questions.filter(q => !q.onlyPMA);
  }

  onAnswerChange(questionId: string, value: LikertValue) {
    this.answerChange.emit({ questionId, value });
  }

  getAnswer(questionId: string): LikertValue {
    return this.answers.get(questionId) || null;
  }
}

