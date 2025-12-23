import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance, SurveyQuestion } from '../../models/survey.model';

@Component({
  selector: 'app-survey-review',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatListModule,
    MatIconModule
  ],
  templateUrl: './survey-review.component.html',
  styleUrl: './survey-review.component.css'
})
export class SurveyReviewComponent implements OnInit {
  instance = signal<SurveyInstance | null>(null);
  isLoading = signal(true);
  isSubmitting = signal(false);
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

        if (instance.submittedAt) {
          this.router.navigate(['/app/survey', instanceId, 'success']);
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

  getUnansweredQuestions(): SurveyQuestion[] {
    const instance = this.instance();
    if (!instance || !instance.participantType) return [];

    const unanswered: SurveyQuestion[] = [];
    const answersMap = new Map(instance.answers.map(a => [a.questionId, a.value]));

    instance.template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        const questions = instance.participantType === 'PMA'
          ? subcategory.questions
          : subcategory.questions.filter(q => !q.onlyPMA);
        
        questions.forEach(question => {
          if (!answersMap.has(question.id) || answersMap.get(question.id) === null) {
            unanswered.push(question);
          }
        });
      });
    });
    return unanswered;
  }

  scrollToQuestion(questionId: string) {
    const instance = this.instance();
    if (!instance) return;
    
    // Navigate to fill page with questionId as fragment
    this.router.navigate(['/app/survey', instance.id, 'fill'], { 
      fragment: `question-${questionId}` 
    }).then(() => {
      // Wait a bit for the page to load, then scroll
      setTimeout(() => {
        const element = document.getElementById(`question-${questionId}`);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 300);
    });
  }

  goBack() {
    const instance = this.instance();
    if (!instance) return;
    this.router.navigate(['/app/survey', instance.id, 'fill']);
  }

  submit() {
    const instance = this.instance();
    if (!instance) return;

    // Prüfe ob alle Fragen beantwortet sind
    const unansweredQuestions = this.getUnansweredQuestions();
    if (unansweredQuestions.length > 0) {
      this.error.set(`Offene Fragen vorhanden. Absenden nicht möglich.`);
      this.isSubmitting.set(false);
      return;
    }

    this.isSubmitting.set(true);
    this.error.set(null);

    this.surveyService.submitInstance(instance.id).subscribe({
      next: () => {
        this.router.navigate(['/app/survey', instance.id, 'success']);
      },
      error: () => {
        this.error.set('Fehler beim Absenden der Umfrage');
        this.isSubmitting.set(false);
      }
    });
  }
}

