import { Component, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { Subscription } from 'rxjs';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance, SurveyQuestion, LikertValue, ParticipantType } from '../../models/survey.model';
import { CategorySectionComponent } from '../../components/category-section/category-section.component';

@Component({
  selector: 'app-survey-fill',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
    CategorySectionComponent
  ],
  templateUrl: './survey-fill.component.html',
  styleUrl: './survey-fill.component.css'
})
export class SurveyFillComponent implements OnInit, OnDestroy {
  instance = signal<SurveyInstance | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);
  answers = signal<Map<string, LikertValue>>(new Map());
  
  private subscription?: Subscription;

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

        if (instance.submittedAt) {
          this.router.navigate(['/app/survey', instanceId, 'success']);
          return;
        }

        this.instance.set(instance);
        const answersMap = new Map<string, LikertValue>();
        instance.answers.forEach(a => answersMap.set(a.questionId, a.value));
        this.answers.set(answersMap);
        this.isLoading.set(false);

        // Scroll to question if fragment is present
        this.route.fragment.subscribe(fragment => {
          if (fragment) {
            setTimeout(() => {
              const element = document.getElementById(fragment);
              if (element) {
                element.scrollIntoView({ behavior: 'smooth', block: 'center' });
              }
            }, 500);
          }
        });
      },
      error: () => {
        this.error.set('Fehler beim Laden der Umfrage');
        this.isLoading.set(false);
      }
    });
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  onAnswerChange(event: { questionId: string; value: LikertValue }) {
    const instance = this.instance();
    if (!instance) return;

    this.answers.update(answers => {
      const newAnswers = new Map(answers);
      if (event.value === null) {
        newAnswers.delete(event.questionId);
      } else {
        newAnswers.set(event.questionId, event.value);
      }
      return newAnswers;
    });

    this.surveyService.saveAnswer(instance.id, event.questionId, event.value).subscribe();
  }

  getTotalQuestions(): number {
    const instance = this.instance();
    if (!instance || !instance.participantType) return 0;

    let count = 0;
    instance.template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        if (instance.participantType === 'PMA') {
          count += subcategory.questions.length;
        } else {
          count += subcategory.questions.filter(q => !q.onlyPMA).length;
        }
      });
    });
    return count;
  }

  getAnsweredCount(): number {
    return this.answers().size;
  }

  getProgress(): number {
    const total = this.getTotalQuestions();
    if (total === 0) return 0;
    return (this.getAnsweredCount() / total) * 100;
  }

  getUnansweredQuestions(): SurveyQuestion[] {
    const instance = this.instance();
    if (!instance || !instance.participantType) return [];

    const unanswered: SurveyQuestion[] = [];
    instance.template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        const questions = instance.participantType === 'PMA'
          ? subcategory.questions
          : subcategory.questions.filter(q => !q.onlyPMA);
        
        questions.forEach(question => {
          if (!this.answers().has(question.id)) {
            unanswered.push(question);
          }
        });
      });
    });
    return unanswered;
  }

  goToReview() {
    const instance = this.instance();
    if (!instance) return;
    this.router.navigate(['/app/survey', instance.id, 'review']);
  }
}
