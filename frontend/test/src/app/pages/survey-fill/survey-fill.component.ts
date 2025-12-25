import { Component, signal, OnInit, OnDestroy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { Subscription } from 'rxjs';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance, SurveyQuestion, LikertValue, ParticipantType } from '../../models/survey.model';
import { LikertQuestionComponent } from '../../components/likert-question/likert-question.component';

@Component({
  selector: 'app-survey-fill',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
    LikertQuestionComponent
  ],
  templateUrl: './survey-fill.component.html',
  styleUrl: './survey-fill.component.css'
})
export class SurveyFillComponent implements OnInit, OnDestroy {
  instance = signal<SurveyInstance | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);
  answers = signal<Map<string, LikertValue>>(new Map());
  currentQuestionIndex = signal<number>(0);
  isSubmitting = signal(false);
  
  // Liste aller Fragen (gefiltert nach participantType)
  allQuestions = signal<SurveyQuestion[]>([]);
  
  // Computed properties
  currentQuestion = computed(() => {
    const questions = this.allQuestions();
    const index = this.currentQuestionIndex();
    return questions[index] || null;
  });
  
  totalQuestions = computed(() => this.allQuestions().length);
  
  currentQuestionNumber = computed(() => this.currentQuestionIndex() + 1);
  
  progressPercentage = computed(() => {
    const total = this.totalQuestions();
    if (total === 0) return 0;
    const answered = this.getAnsweredCount();
    return Math.round((answered / total) * 100);
  });
  
  isFirstQuestion = computed(() => this.currentQuestionIndex() === 0);
  
  isLastQuestion = computed(() => {
    const index = this.currentQuestionIndex();
    const total = this.totalQuestions();
    return index === total - 1;
  });
  
  canGoNext = computed(() => {
    const question = this.currentQuestion();
    if (!question) return false;
    return this.answers().has(question.id);
  });
  
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
        
        // Erstelle Liste aller Fragen
        this.buildQuestionsList(instance);
        
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

  buildQuestionsList(instance: SurveyInstance) {
    if (!instance.participantType) return;
    
    const questions: SurveyQuestion[] = [];
    instance.template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        const visibleQuestions = instance.participantType === 'PMA'
          ? subcategory.questions
          : subcategory.questions.filter(q => !q.onlyPMA);
        questions.push(...visibleQuestions);
      });
    });
    
    // Sortiere nach order falls vorhanden
    questions.sort((a, b) => (a.order || 0) - (b.order || 0));
    
    this.allQuestions.set(questions);
    
    // Setze Index auf erste unbeantwortete Frage oder 0
    const firstUnansweredIndex = questions.findIndex(q => !this.answers().has(q.id));
    this.currentQuestionIndex.set(firstUnansweredIndex >= 0 ? firstUnansweredIndex : 0);
  }

  onAnswerChange(value: LikertValue) {
    const instance = this.instance();
    const question = this.currentQuestion();
    if (!instance || !question) return;

    this.answers.update(answers => {
      const newAnswers = new Map(answers);
      if (value === null) {
        newAnswers.delete(question.id);
      } else {
        newAnswers.set(question.id, value);
      }
      return newAnswers;
    });

    this.surveyService.saveAnswer(instance.id, question.id, value).subscribe();
  }
  
  getCurrentAnswer(): LikertValue {
    const question = this.currentQuestion();
    if (!question) return null;
    return this.answers().get(question.id) || null;
  }

  getAnsweredCount(): number {
    return this.answers().size;
  }
  
  goToNext() {
    if (!this.canGoNext()) return;
    
    const currentIndex = this.currentQuestionIndex();
    const total = this.totalQuestions();
    
    if (currentIndex < total - 1) {
      this.currentQuestionIndex.set(currentIndex + 1);
    }
  }
  
  goToPrevious() {
    const currentIndex = this.currentQuestionIndex();
    if (currentIndex > 0) {
      this.currentQuestionIndex.set(currentIndex - 1);
    }
  }
  
  submitSurvey() {
    const instance = this.instance();
    if (!instance || !this.canGoNext()) return;
    
    this.isSubmitting.set(true);
    
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

