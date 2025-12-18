import { Injectable, signal } from '@angular/core';
import { WizardState, AnalysisMode, ContextType, Answer } from '../models/wizard-data.model';

@Injectable({
  providedIn: 'root'
})
export class WizardStateService {
  private state = signal<WizardState>({
    answers: [],
    currentCategoryIndex: 0
  });

  getState() {
    return this.state.asReadonly();
  }

  setAnalysisMode(mode: AnalysisMode) {
    this.state.update(s => ({ ...s, analysisMode: mode }));
  }

  setContextType(context: ContextType) {
    this.state.update(s => ({ ...s, contextType: context }));
  }

  setAnswer(questionId: string, value: number) {
    this.state.update(s => {
      const answers = [...s.answers];
      const existingIndex = answers.findIndex(a => a.questionId === questionId);
      
      if (existingIndex >= 0) {
        answers[existingIndex] = { questionId, value };
      } else {
        answers.push({ questionId, value });
      }
      
      return { ...s, answers };
    });
  }

  getAnswer(questionId: string): number | undefined {
    const answer = this.state().answers.find(a => a.questionId === questionId);
    return answer?.value;
  }

  setCurrentCategoryIndex(index: number) {
    this.state.update(s => ({ ...s, currentCategoryIndex: index }));
  }

  reset() {
    this.state.set({
      answers: [],
      currentCategoryIndex: 0
    });
  }

  getAllAnswers(): Answer[] {
    return this.state().answers;
  }
}

