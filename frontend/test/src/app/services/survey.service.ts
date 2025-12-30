import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map, tap, switchMap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SurveyTemplate, SurveyInstance, SurveyResponse, ParticipantType, SurveyAnswer, SurveyResult, Department, DepartmentResult, CompanyResults } from '../models/survey.model';

// Backend Response Interfaces
interface SurveyTemplateResponse {
  id: number;
  name: string;
  description?: string;
  version: string;
  active: boolean;
  categoriesJson?: string; // JSON string from backend
  categories?: SurveyCategoryResponse[]; // Parsed categories (if backend sends them)
  createdAt: string;
  updatedAt: string;
}

interface SurveyCategoryResponse {
  name: string;
  subcategories: SurveySubcategoryResponse[];
}

interface SurveySubcategoryResponse {
  name: string;
  questions: SurveyQuestionResponse[];
}

interface SurveyQuestionResponse {
  id: string;
  text: string;
  category: string;
  subcategory: string;
  onlyPMA?: boolean;
  reverse?: boolean;
  order?: number;
  adkarCategory?: string;
}

interface SurveyInstanceResponse {
  id: number;
  templateId: number;
  templateName: string;
  participantType: string;
  department: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  submittedAt?: string;
  totalQuestions?: number; // Additive Feld vom Backend
  answeredQuestions?: number; // Additive Feld vom Backend
}

interface SurveyInstanceDetailResponse {
  id: number;
  template: SurveyTemplateResponse;
  participantType: string;
  department: string;
  status: string;
  answers: SurveyAnswerResponse[];
  createdAt: string;
  updatedAt: string;
  submittedAt?: string;
}

interface SurveyAnswerResponse {
  questionId: string;
  value: number;
}

interface SurveyInstanceCreateRequest {
  templateId: number;
  participantType: string;
  department: string;
}

interface SurveyAnswerUpdateRequest {
  answers: Array<{
    questionId: string;
    value: number;
  }>;
}

@Injectable({
  providedIn: 'root'
})
export class SurveyService {
  private templates = signal<SurveyTemplate[]>([]);
  private instances = signal<SurveyInstance[]>([]);

  constructor(private http: HttpClient) {}

  // Computed
  activeTemplates = computed(() => 
    this.templates().filter(t => t.active)
  );

  /**
   * Get all survey templates from backend
   */
  getTemplates(): Observable<SurveyTemplate[]> {
    return this.http.get<SurveyTemplateResponse[]>(`${environment.apiBaseUrl}/surveys/templates`).pipe(
      map((responses: SurveyTemplateResponse[]) => {
        const templates: SurveyTemplate[] = responses.map(resp => this.mapTemplateResponse(resp));
        this.templates.set(templates);
        return templates;
      }),
      catchError(error => {
        console.error('Error loading survey templates:', error);
        return of([]);
      })
    );
  }

  /**
   * Get active survey templates
   */
  getActiveTemplates(): Observable<SurveyTemplate[]> {
    return this.getTemplates().pipe(
      map(() => this.activeTemplates())
    );
  }

  /**
   * Get template by ID
   */
  getTemplate(id: string): Observable<SurveyTemplate | null> {
    return this.getTemplates().pipe(
      map(templates => {
        const template = templates.find(t => t.id === id);
        return template || null;
      })
    );
  }

  /**
   * Create new survey instance (start survey)
   */
  createInstance(templateId: string, participantType: ParticipantType, department: Department): Observable<SurveyInstance> {
    const request: SurveyInstanceCreateRequest = {
      templateId: parseInt(templateId),
      participantType: participantType,
      department: department
    };

    return this.http.post<SurveyInstanceResponse>(`${environment.apiBaseUrl}/surveys/instances`, request).pipe(
      switchMap((response: SurveyInstanceResponse) => {
        // Lade Template-Details für die Instanz
        return this.getTemplate(templateId).pipe(
          map(template => {
            if (!template) {
              throw new Error('Template not found');
            }
            const instance: SurveyInstance = {
              id: response.id.toString(),
              templateId: templateId,
              template: template,
              participantType: participantType as ParticipantType,
              department: department as Department,
              answers: [],
              createdAt: new Date(response.createdAt)
            };
            this.instances.update(instances => [...instances, instance]);
            return instance;
          })
        );
      }),
      catchError(error => {
        console.error('Error creating survey instance:', error);
        throw error;
      })
    );
  }

  /**
   * Get instance by ID
   */
  getInstance(id: string): Observable<SurveyInstance | null> {
    return this.http.get<SurveyInstanceDetailResponse>(`${environment.apiBaseUrl}/surveys/instances/${id}`).pipe(
      map((response: SurveyInstanceDetailResponse) => {
        const template = this.mapTemplateResponse(response.template);
        const instance: SurveyInstance = {
          id: response.id.toString(),
          templateId: template.id,
          template: template,
          participantType: response.participantType as ParticipantType,
          department: response.department as Department,
          answers: response.answers.map(a => ({
            questionId: a.questionId,
            value: a.value as any
          })),
          submittedAt: response.submittedAt ? new Date(response.submittedAt) : undefined,
          createdAt: new Date(response.createdAt)
        };
        return instance;
      }),
      catchError(error => {
        console.error('Error loading survey instance:', error);
        return of(null);
      })
    );
  }

  /**
   * Save answer for instance (autosave)
   */
  saveAnswer(instanceId: string, questionId: string, value: number | null): Observable<void> {
    // Backend akzeptiert jetzt value=null als "Antwort entfernen" (Keine Angabe / unanswered).
    // Wir senden bewusst nur die eine Änderung, damit Autosave robust ist und keine leeren Answer-Listen erzeugt.
    const request: SurveyAnswerUpdateRequest = {
      answers: [{ questionId, value: value as any }]
    };

    return this.http.put<void>(`${environment.apiBaseUrl}/surveys/instances/${instanceId}/answers`, request).pipe(
      tap(() => {
        this.instances.update(instances =>
          instances.map(i => {
            if (i.id !== instanceId) return i;
            const remaining = i.answers.filter(a => a.questionId !== questionId);
            if (value === null) {
              return { ...i, answers: remaining };
            }
            return { ...i, answers: [...remaining, { questionId, value: value as any }] };
          })
        );
      }),
      catchError(error => {
        console.error('Error saving answer:', error);
        throw error;
      })
    );
  }

  /**
   * Submit survey instance
   */
  submitInstance(instanceId: string): Observable<SurveyResponse> {
    return this.http.post<void>(`${environment.apiBaseUrl}/surveys/instances/${instanceId}/submit`, {}).pipe(
      switchMap(() => {
        // Lade die submitted Instanz
        return this.getInstance(instanceId).pipe(
          map(instance => {
            if (!instance || !instance.participantType || !instance.department) {
              throw new Error('Instance not found or incomplete');
            }

            const response: SurveyResponse = {
              id: instanceId,
              templateId: instance.templateId,
              participantType: instance.participantType,
              department: instance.department,
              companyId: 0, // Wird vom Backend verwaltet
              answers: instance.answers,
              submittedAt: instance.submittedAt || new Date(),
              createdAt: instance.createdAt
            };

            // Aktualisiere lokalen State
            this.instances.update(instances =>
              instances.map(i =>
                i.id === instanceId
                  ? { ...i, submittedAt: response.submittedAt }
                  : i
              )
            );

            return response;
          })
        );
      }),
      catchError(error => {
        console.error('Error submitting survey instance:', error);
        throw error;
      })
    );
  }

  /**
   * Get user's survey instances
   * Optimiert: Nutzt direkt die Response-Daten statt für jede Instanz getInstance() aufzurufen
   */
  getUserInstances(): Observable<SurveyInstance[]> {
    return forkJoin({
      responses: this.http.get<SurveyInstanceResponse[]>(`${environment.apiBaseUrl}/surveys/instances`),
      templates: this.getTemplates()
    }).pipe(
      map(({ responses, templates }) => {
        // Mappe Responses zu Instances mit Template-Daten
        return responses.map(resp => {
          const template = templates.find(t => t.id === resp.templateId.toString());
          if (!template) {
            // Template nicht gefunden - könnte passieren wenn Template gelöscht wurde
            return null;
          }
          
          const instance: SurveyInstance = {
            id: resp.id.toString(),
            templateId: resp.templateId.toString(),
            template: template,
            participantType: resp.participantType as ParticipantType,
            department: resp.department as Department,
            answers: [], // Wird nur in Detail-View geladen (via getInstance)
            submittedAt: resp.submittedAt ? new Date(resp.submittedAt) : undefined,
            createdAt: new Date(resp.createdAt),
            // Additive Felder vom Backend für Progress-Anzeige
            ...(resp.totalQuestions !== undefined && { totalQuestions: resp.totalQuestions }),
            ...(resp.answeredQuestions !== undefined && { answeredQuestions: resp.answeredQuestions })
          } as SurveyInstance & { totalQuestions?: number; answeredQuestions?: number };
          
          return instance;
        }).filter((i): i is SurveyInstance => i !== null);
      }),
      tap(instances => {
        this.instances.set(instances);
      }),
      catchError(error => {
        console.error('Error loading user instances:', error);
        if (error.status === 0 || error.status === undefined) {
          console.error('Backend connection failed. Is the backend running on http://localhost:8080?');
        } else if (error.status === 401 || error.status === 403) {
          console.error('Authentication failed. Please log in again.');
        } else {
          console.error('API error:', error.status, error.message);
        }
        return of([]);
      })
    );
  }

  /**
   * Delete survey instance (Hard Delete)
   * Backend: DELETE /api/v1/surveys/instances/{instanceId}
   */
  deleteInstance(instanceId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/surveys/instances/${instanceId}`).pipe(
      tap(() => {
        // Optimistic local update: remove from cached instances list
        this.instances.update(instances => instances.filter(i => i.id !== instanceId));
      }),
      catchError(error => {
        console.error('Error deleting survey instance:', error);
        throw error;
      })
    );
  }

  /**
   * Get user's submitted responses (filtered from instances)
   */
  getUserResponses(): Observable<SurveyResponse[]> {
    return this.getUserInstances().pipe(
      map(instances => {
        return instances
          .filter(i => i.submittedAt !== undefined && i.participantType && i.department)
          .map(i => ({
            id: i.id,
            templateId: i.templateId,
            participantType: i.participantType!,
            department: i.department!,
            companyId: i.companyId || 0,
            answers: i.answers,
            submittedAt: i.submittedAt,
            createdAt: i.createdAt
          }));
      })
    );
  }

  /**
   * Get all responses for a template (Admin) - Not implemented in backend yet
   */
  getTemplateResponses(templateId: string): Observable<SurveyResponse[]> {
    // Backend hat noch keinen Endpoint dafür
    // Verwende getUserResponses und filtere nach templateId
    return this.getUserResponses().pipe(
      map(responses => responses.filter(r => r.templateId === templateId))
    );
  }

  /**
   * Calculate results for a template - Frontend-only calculation
   * @deprecated Diese Methode ist deprecated. Nutze stattdessen reportingService.getTemplateResults(templateId)
   * Diese Methode wird entfernt in zukünftiger Version.
   */
  calculateResults(templateId: string, companyId?: number): Observable<SurveyResult[]> {
    return this.getTemplateResponses(templateId).pipe(
      map(responses => {
        const template = this.templates().find(t => t.id === templateId);
        if (!template) {
          return [];
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

        return results;
      })
    );
  }

  /**
   * Calculate results by department - Frontend-only calculation
   * @deprecated Diese Methode berechnet Department-Results lokal. Nutze stattdessen ReportingService.getDepartmentReadiness()
   * Diese Methode wird entfernt sobald Backend-Endpoint verfügbar ist.
   */
  calculateResultsByDepartment(templateId: string, companyId?: number): Observable<DepartmentResult[]> {
    return this.getTemplateResponses(templateId).pipe(
      map(responses => {
        const template = this.templates().find(t => t.id === templateId);
        if (!template) {
          return [];
        }

        const departmentResults: DepartmentResult[] = [];
        const departments: Department[] = ['EINKAUF', 'VERTRIEB', 'LAGER_LOGISTIK', 'IT', 'GESCHAEFTSFUEHRUNG'];

        departments.forEach(department => {
          const departmentResponses = responses.filter(r => r.department === department);
          const participantCount = departmentResponses.length;

          if (participantCount === 0) {
            return;
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

                const questionAnswers = departmentResponses
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
                  totalCount: questions.length * departmentResponses.length,
                  reverseItems
                });
              }
            });
          });

          departmentResults.push({
            department,
            participantCount,
            results
          });
        });

        return departmentResults;
      })
    );
  }

  /**
   * Get company results (overall + by department)
   */
  getCompanyResults(templateId: string, companyId?: number): Observable<CompanyResults> {
    return forkJoin({
      overall: this.calculateResults(templateId, companyId),
      byDepartment: this.calculateResultsByDepartment(templateId, companyId)
    }).pipe(
      map(({ overall, byDepartment }) => ({
        companyId: companyId || 0,
        companyName: '', // Wird nicht vom Backend bereitgestellt
        overallResults: overall,
        departmentResults: byDepartment
      }))
    );
  }

  /**
   * Helper: Map Backend Template Response to Frontend Model
   */
  private mapTemplateResponse(response: SurveyTemplateResponse): SurveyTemplate {
    let categories: SurveyCategoryResponse[] = [];
    
    // Wenn Backend bereits geparste categories sendet, verwende diese
    if (response.categories && Array.isArray(response.categories)) {
      categories = response.categories;
    } 
    // Ansonsten parse categoriesJson (Backend sendet JSON als String)
    else if (response.categoriesJson) {
      try {
        categories = JSON.parse(response.categoriesJson);
      } catch (e) {
        console.error('Error parsing categoriesJson:', e, response.categoriesJson);
        categories = [];
      }
    }

    return {
      id: response.id.toString(),
      name: response.name,
      description: response.description,
      version: response.version,
      active: response.active,
      categories: categories.map(cat => ({
        name: cat.name,
        subcategories: cat.subcategories.map(sub => ({
          name: sub.name,
          questions: sub.questions.map(q => ({
            id: q.id,
            text: q.text,
            category: q.category,
            subcategory: q.subcategory,
            onlyPMA: q.onlyPMA || false,
            reverse: q.reverse || false,
            order: q.order || 0
          }))
        }))
      })),
      createdAt: new Date(response.createdAt),
      updatedAt: new Date(response.updatedAt)
    };
  }
}
