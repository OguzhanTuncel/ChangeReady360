import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WizardShellComponent } from '../../components/wizard-shell/wizard-shell.component';
import { ScorePreviewComponent, CategoryScore } from '../../components/score-preview/score-preview.component';
import { WizardStateService } from '../../services/wizard-state.service';
import { MOCK_CATEGORIES, ANALYSIS_MODES, CONTEXT_TYPES } from '../../models/wizard-data.model';

@Component({
  selector: 'app-wizard-summary',
  standalone: true,
  imports: [CommonModule, WizardShellComponent, ScorePreviewComponent],
  templateUrl: './wizard-summary.component.html',
  styleUrl: './wizard-summary.component.css'
})
export class WizardSummaryComponent implements OnInit {
  analysisMode: string = '';
  contextType: string = '';
  overallScore: number = 0;
  maxScore: number = 100;
  categoryScores: CategoryScore[] = [];
  allCategories = MOCK_CATEGORIES;

  constructor(
    private router: Router,
    private wizardState: WizardStateService
  ) {}

  ngOnInit() {
    this.loadSummary();
  }

  loadSummary() {
    const state = this.wizardState.getState();
    const wizardState = state();
    
    // Get selected options
    const mode = ANALYSIS_MODES.find(m => m.id === wizardState.analysisMode);
    const context = CONTEXT_TYPES.find(c => c.id === wizardState.contextType);
    
    this.analysisMode = mode?.title || '';
    this.contextType = context?.title || '';
    
    // Calculate scores
    this.calculateScores();
  }

  calculateScores() {
    const state = this.wizardState.getState();
    const answers = state().answers;
    
    let totalScore = 0;
    let totalMaxScore = 0;
    
    this.categoryScores = this.allCategories.map(category => {
      const categoryAnswers = answers.filter(a => 
        category.questions.some(q => q.id === a.questionId)
      );
      
      const categoryScore = categoryAnswers.reduce((sum, a) => sum + a.value, 0);
      const categoryMaxScore = category.questions.length * 5; // 5 questions * max 5 points
      
      totalScore += categoryScore;
      totalMaxScore += categoryMaxScore;
      
      return {
        categoryId: category.id,
        categoryName: category.name,
        score: categoryScore,
        maxScore: categoryMaxScore
      };
    });
    
    // Calculate overall score (scale to 100)
    this.overallScore = totalMaxScore > 0 
      ? Math.round((totalScore / totalMaxScore) * 100)
      : 0;
  }

  getTotalSteps(): number {
    return 3 + this.allCategories.length + 1;
  }

  onBack() {
    const lastCategoryIndex = this.allCategories.length - 1;
    this.router.navigate(['/wizard/category', lastCategoryIndex]);
  }

  onRestart() {
    this.wizardState.reset();
    this.router.navigate(['/']);
  }
}

