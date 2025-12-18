import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface CategoryScore {
  categoryId: string;
  categoryName: string;
  score: number;
  maxScore: number;
}

@Component({
  selector: 'app-score-preview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './score-preview.component.html',
  styleUrl: './score-preview.component.css'
})
export class ScorePreviewComponent {
  @Input() overallScore: number = 0;
  @Input() maxScore: number = 100;
  @Input() categoryScores: CategoryScore[] = [];

  getScorePercentage(score: number, max: number): number {
    return (score / max) * 100;
  }

  getScoreBadge(score: number, max: number): { label: string; color: string } {
    const percentage = (score / max) * 100;
    if (percentage >= 75) {
      return { label: 'Sehr gut', color: 'success' };
    } else if (percentage >= 50) {
      return { label: 'Gut', color: 'warning' };
    } else {
      return { label: 'Verbesserung nötig', color: 'error' };
    }
  }
}

