import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WizardShellComponent } from '../../components/wizard-shell/wizard-shell.component';
import { QuestionItemComponent } from '../../components/question-item/question-item.component';
import { WizardStateService } from '../../services/wizard-state.service';
import { MOCK_CATEGORIES, Category, Question } from '../../models/wizard-data.model';

@Component({
  selector: 'app-wizard-category',
  standalone: true,
  imports: [CommonModule, WizardShellComponent, QuestionItemComponent],
  templateUrl: './wizard-category.component.html',
  styleUrl: './wizard-category.component.css'
})
export class WizardCategoryComponent implements OnInit {
  categoryIndex: number = 0;
  category: Category | null = null;
  allCategories = MOCK_CATEGORIES;
  answers: Map<string, number> = new Map();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private wizardState: WizardStateService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.categoryIndex = parseInt(params['index'], 10);
      this.loadCategory();
      this.loadAnswers();
    });
  }

  loadCategory() {
    if (this.categoryIndex >= 0 && this.categoryIndex < this.allCategories.length) {
      this.category = this.allCategories[this.categoryIndex];
      this.wizardState.setCurrentCategoryIndex(this.categoryIndex);
    }
  }

  loadAnswers() {
    const state = this.wizardState.getState();
    state().answers.forEach(answer => {
      this.answers.set(answer.questionId, answer.value);
    });
  }

  onAnswerChange(questionId: string, value: number) {
    this.answers.set(questionId, value);
    this.wizardState.setAnswer(questionId, value);
  }

  getAnswer(questionId: string): number | undefined {
    return this.answers.get(questionId);
  }

  isAllQuestionsAnswered(): boolean {
    if (!this.category) return false;
    return this.category.questions.every(q => this.answers.has(q.id));
  }

  onBack() {
    if (this.categoryIndex > 0) {
      this.router.navigate(['/wizard/category', this.categoryIndex - 1]);
    } else {
      this.router.navigate(['/wizard/context']);
    }
  }

  onNext() {
    if (this.isAllQuestionsAnswered()) {
      const nextIndex = this.categoryIndex + 1;
      if (nextIndex < this.allCategories.length) {
        this.router.navigate(['/wizard/category', nextIndex]);
      } else {
        this.router.navigate(['/wizard/summary']);
      }
    }
  }

  getCurrentStep(): number {
    return 3 + this.categoryIndex;
  }

  getTotalSteps(): number {
    return 3 + this.allCategories.length + 1; // mode + context + categories + summary
  }
}

