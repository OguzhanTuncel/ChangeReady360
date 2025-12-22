import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { SurveyService } from '../../services/survey.service';
import { SurveyTemplate, SurveyResult } from '../../models/survey.model';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule
  ],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent implements OnInit {
  templates = signal<SurveyTemplate[]>([]);
  selectedTemplateId = signal<string>('');
  results = signal<SurveyResult[]>([]);
  isLoading = signal(true);

  displayedColumns: string[] = ['category', 'subcategory', 'average', 'answered', 'reverse'];

  constructor(private surveyService: SurveyService) {}

  ngOnInit() {
    this.loadTemplates();
  }

  loadTemplates() {
    this.isLoading.set(true);
    this.surveyService.getTemplates().subscribe({
      next: (templates) => {
        this.templates.set(templates);
        if (templates.length > 0) {
          this.selectedTemplateId.set(templates[0].id);
          this.loadResults(templates[0].id);
        } else {
          this.isLoading.set(false);
        }
      }
    });
  }

  loadResults(templateId: string) {
    this.isLoading.set(true);
    this.surveyService.calculateResults(templateId).subscribe({
      next: (results) => {
        this.results.set(results);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  onTemplateChange(templateId: string) {
    this.selectedTemplateId.set(templateId);
    this.loadResults(templateId);
  }

  getAverageColor(average: number): string {
    if (average >= 4) return 'success';
    if (average >= 3) return 'warning';
    return 'error';
  }

  getPercentage(answered: number, total: number): number {
    if (total === 0) return 0;
    return Math.round((answered / total) * 100);
  }
}
