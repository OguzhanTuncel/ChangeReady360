import { Injectable, signal, computed } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { SurveyTemplate, SurveyInstance, SurveyResponse, ParticipantType, SurveyAnswer, SurveyResult } from '../models/survey.model';
import { STANDARD_SURVEY_TEMPLATE } from '../data/survey-template.data';

@Injectable({
  providedIn: 'root'
})
export class SurveyService {
  // Mock Storage (später durch Backend-API ersetzen)
  private templates = signal<SurveyTemplate[]>([STANDARD_SURVEY_TEMPLATE]);
  private responses = signal<SurveyResponse[]>([]);
  private instances = signal<SurveyInstance[]>([]);

  // Computed
  activeTemplates = computed(() => 
    this.templates().filter(t => t.active)
  );

  /**
   * Get all survey templates
   */
  getTemplates(): Observable<SurveyTemplate[]> {
    return of(this.templates()).pipe(delay(300));
  }

  /**
   * Get active survey templates
   */
  getActiveTemplates(): Observable<SurveyTemplate[]> {
    return of(this.activeTemplates()).pipe(delay(300));
  }

  /**
   * Get template by ID
   */
  getTemplate(id: string): Observable<SurveyTemplate | null> {
    const template = this.templates().find(t => t.id === id);
    return of(template || null).pipe(delay(200));
  }

  /**
   * Create new survey instance (start survey)
   */
  createInstance(templateId: string, participantType: ParticipantType): Observable<SurveyInstance> {
    const template = this.templates().find(t => t.id === templateId);
    if (!template) {
      throw new Error('Template not found');
    }

    const instance: SurveyInstance = {
      id: `instance-${Date.now()}`,
      templateId,
      template,
      participantType,
      answers: [],
      createdAt: new Date()
    };

    this.instances.update(instances => [...instances, instance]);
    return of(instance).pipe(delay(200));
  }

  /**
   * Get instance by ID
   */
  getInstance(id: string): Observable<SurveyInstance | null> {
    const instance = this.instances().find(i => i.id === id);
    return of(instance || null).pipe(delay(200));
  }

  /**
   * Save answer for instance
   */
  saveAnswer(instanceId: string, questionId: string, value: number | null): Observable<void> {
    this.instances.update(instances => 
      instances.map(instance => {
        if (instance.id === instanceId) {
          const answers = [...instance.answers];
          const existingIndex = answers.findIndex(a => a.questionId === questionId);
          
          if (existingIndex >= 0) {
            if (value === null) {
              answers.splice(existingIndex, 1);
            } else {
              answers[existingIndex] = { questionId, value: value as any };
            }
          } else if (value !== null) {
            answers.push({ questionId, value: value as any });
          }
          
          return { ...instance, answers };
        }
        return instance;
      })
    );
    return of(void 0).pipe(delay(100));
  }

  /**
   * Submit survey instance
   */
  submitInstance(instanceId: string): Observable<SurveyResponse> {
    const instance = this.instances().find(i => i.id === instanceId);
    if (!instance || !instance.participantType) {
      throw new Error('Instance not found or incomplete');
    }

    const response: SurveyResponse = {
      id: `response-${Date.now()}`,
      templateId: instance.templateId,
      participantType: instance.participantType,
      answers: instance.answers,
      submittedAt: new Date(),
      createdAt: instance.createdAt
    };

    this.responses.update(responses => [...responses, response]);
    this.instances.update(instances => 
      instances.map(i => 
        i.id === instanceId 
          ? { ...i, submittedAt: new Date() }
          : i
      )
    );

    return of(response).pipe(delay(300));
  }

  /**
   * Get user's survey instances
   */
  getUserInstances(): Observable<SurveyInstance[]> {
    return of(this.instances()).pipe(delay(200));
  }

  /**
   * Get user's submitted responses
   */
  getUserResponses(): Observable<SurveyResponse[]> {
    return of(this.responses()).pipe(delay(200));
  }

  /**
   * Get all responses for a template (Admin)
   */
  getTemplateResponses(templateId: string): Observable<SurveyResponse[]> {
    const templateResponses = this.responses().filter(r => r.templateId === templateId);
    return of(templateResponses).pipe(delay(200));
  }

  /**
   * Calculate results for a template
   */
  calculateResults(templateId: string): Observable<SurveyResult[]> {
    const responses = this.responses().filter(r => r.templateId === templateId);
    const template = this.templates().find(t => t.id === templateId);
    
    if (!template) {
      return of([]);
    }

    const results: SurveyResult[] = [];

    template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        const questions = subcategory.questions;
        const answers: number[] = [];
        const reverseItems: string[] = [];

        questions.forEach(question => {
          if (question.reverse) {
            reverseItems.push(question.id);
          }

          const questionAnswers = responses
            .flatMap(r => r.answers)
            .filter(a => a.questionId === question.id && a.value !== null)
            .map(a => a.value!);

          answers.push(...questionAnswers);
        });

        if (answers.length > 0) {
          const average = answers.reduce((sum, val) => sum + val, 0) / answers.length;
          results.push({
            category: category.name,
            subcategory: subcategory.name || category.name,
            average: Math.round(average * 100) / 100,
            answeredCount: answers.length,
            totalCount: questions.length * responses.length,
            reverseItems
          });
        }
      });
    });

    return of(results).pipe(delay(200));
  }
}
