import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance, SurveyQuestion, LikertValue } from '../../models/survey.model';

@Component({
  selector: 'app-survey-summary',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatChipsModule,
    DatePipe
  ],
  templateUrl: './survey-summary.component.html',
  styleUrl: './survey-summary.component.css'
})
export class SurveySummaryComponent implements OnInit {
  instance = signal<SurveyInstance | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  constructor(
    private surveyService: SurveyService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    const instanceId = this.route.snapshot.paramMap.get('id');
    if (!instanceId) {
      this.error.set('Keine Umfrage angegeben');
      this.isLoading.set(false);
      return;
    }

    this.surveyService.getInstance(instanceId).subscribe({
      next: (instance) => {
        if (!instance) {
          this.error.set('Umfrage nicht gefunden');
          this.isLoading.set(false);
          return;
        }

        if (!instance.submittedAt) {
          this.router.navigate(['/app/survey', instanceId, 'fill']);
          return;
        }

        this.instance.set(instance);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Fehler beim Laden der Umfrage');
        this.isLoading.set(false);
      }
    });
  }

  getAnswerText(value: LikertValue): string {
    if (value === null) return 'Keine Angabe';
    const labels: { [key: number]: string } = {
      1: 'Stimme gar nicht zu',
      2: 'Stimme eher nicht zu',
      3: 'Weder noch',
      4: 'Stimme eher zu',
      5: 'Stimme voll zu'
    };
    return labels[value] || 'Unbekannt';
  }

  getAllQuestions(): SurveyQuestion[] {
    const instance = this.instance();
    if (!instance || !instance.participantType) return [];

    const questions: SurveyQuestion[] = [];
    instance.template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        const visibleQuestions = instance.participantType === 'PMA'
          ? subcategory.questions
          : subcategory.questions.filter(q => !q.onlyPMA);
        questions.push(...visibleQuestions);
      });
    });
    return questions;
  }

  getAnswer(questionId: string): LikertValue {
    const instance = this.instance();
    if (!instance) return null;
    const answer = instance.answers.find(a => a.questionId === questionId);
    return answer?.value || null;
  }

  getAnsweredCount(): number {
    const instance = this.instance();
    if (!instance) return 0;
    return instance.answers.filter(a => a.value !== null).length;
  }

  getTotalCount(): number {
    return this.getAllQuestions().length;
  }

  getVisibleQuestions(subcategory: { name: string; questions: SurveyQuestion[] }, participantType: string): SurveyQuestion[] {
    if (participantType === 'PMA') {
      return subcategory.questions;
    }
    return subcategory.questions.filter(q => !q.onlyPMA);
  }

  getAnswerChipClass(questionId: string): string {
    const value = this.getAnswer(questionId);
    if (value === null) return 'no-answer';
    if (value >= 4) return 'positive';
    if (value >= 3) return 'neutral';
    return 'negative';
  }
}
