import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { SurveyService } from '../../services/survey.service';
import { SurveyTemplate, SurveyResult, DepartmentResult, DEPARTMENT_DISPLAY_NAMES } from '../../models/survey.model';
import { ReportingService } from '../../services/reporting.service';
import { ManagementSummary, DepartmentReadiness } from '../../models/reporting.model';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatTabsModule
  ],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent implements OnInit {
  templates = signal<SurveyTemplate[]>([]);
  selectedTemplateId = signal<string>('');
  results = signal<SurveyResult[]>([]);
  departmentResults = signal<DepartmentResult[]>([]);
  isLoading = signal(true);
  selectedTab = signal<number>(0);

  managementSummary = signal<ManagementSummary | null>(null);
  departmentReadiness = signal<DepartmentReadiness[]>([]);

  displayedColumns: string[] = ['category', 'subcategory', 'average', 'answered', 'reverse'];
  readonly departmentDisplayNames = DEPARTMENT_DISPLAY_NAMES;

  constructor(
    private surveyService: SurveyService,
    private reportingService: ReportingService
  ) {}

  ngOnInit() {
    this.loadTemplates();
    this.loadReportingData();
  }

  loadReportingData() {
    this.reportingService.getManagementSummary().subscribe({
      next: (summary) => {
        this.managementSummary.set(summary);
      }
    });

    this.reportingService.getDepartmentReadiness().subscribe({
      next: (departments) => {
        this.departmentReadiness.set(departments);
      }
    });
  }

  getCurrentDate(): string {
    return new Date().toLocaleDateString('de-DE', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  getDepartmentColor(readiness: number): string {
    if (readiness >= 75) return 'var(--change-ready-green)';
    if (readiness >= 50) return 'var(--gold-accent)';
    return 'var(--error-red)';
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
    
    // Load overall results
    this.surveyService.calculateResults(templateId).subscribe({
      next: (results) => {
        this.results.set(results);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });

    // Load department results
    this.surveyService.calculateResultsByDepartment(templateId).subscribe({
      next: (departmentResults) => {
        this.departmentResults.set(departmentResults);
      },
      error: () => {
        // Ignore errors for department results
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

  /**
   * Berechnet den Gesamt-Score als Prozent (0-100)
   * Durchschnitt aller Kategorien, skaliert auf 0-100%
   */
  getOverallScore(): number {
    const results = this.results();
    if (results.length === 0) return 0;

    // Durchschnitt aller Kategorien-Durchschnitte
    const totalAverage = results.reduce((sum, result) => sum + result.average, 0) / results.length;
    
    // Skaliere von 1-5 auf 0-100%
    // 1 = 0%, 3 = 50%, 5 = 100%
    const percentage = ((totalAverage - 1) / 4) * 100;
    return Math.max(0, Math.min(100, Math.round(percentage)));
  }

  /**
   * Berechnet den Umfang des Donut-Kreises
   */
  getDonutCircumference(): number {
    const radius = 75;
    return 2 * Math.PI * radius;
  }

  /**
   * Berechnet den Offset für den Score-Kreis
   */
  getDonutOffset(): number {
    const score = this.getOverallScore();
    const circumference = this.getDonutCircumference();
    // Offset = Umfang minus (Score * Umfang / 100)
    return circumference - (score / 100) * circumference;
  }

  /**
   * Gibt textuelle Bewertung basierend auf Score zurück
   */
  getEvaluationText(): { current: string; critical: string; positive: string } {
    const score = this.getOverallScore();
    const results = this.results();

    if (results.length === 0) {
      return {
        current: 'Noch keine Daten verfügbar',
        critical: 'Bitte füllen Sie zunächst einen Fragebogen aus',
        positive: 'Nach der Auswertung werden hier positive Aspekte angezeigt'
      };
    }

    // Finde niedrigste und höchste Kategorien
    const sortedResults = [...results].sort((a, b) => a.average - b.average);
    const lowestCategory = sortedResults[0];
    const highestCategory = sortedResults[sortedResults.length - 1];

    let currentText = '';
    let criticalText = '';
    let positiveText = '';

    if (score >= 75) {
      currentText = 'Sehr gute Change-Readiness. Ihr Unternehmen ist gut vorbereitet für Veränderungen.';
      criticalText = 'Keine kritischen Bereiche identifiziert.';
      positiveText = `Stärken in "${highestCategory.category}" (Score: ${highestCategory.average.toFixed(1)}/5).`;
    } else if (score >= 50) {
      currentText = 'Moderate Change-Readiness. Es gibt Potenzial für Verbesserungen.';
      criticalText = `Aufmerksamkeit erforderlich in "${lowestCategory.category}" (Score: ${lowestCategory.average.toFixed(1)}/5).`;
      positiveText = `Stärken in "${highestCategory.category}" (Score: ${highestCategory.average.toFixed(1)}/5).`;
    } else if (score >= 25) {
      currentText = 'Verbesserungspotenzial vorhanden. Change-Readiness sollte gestärkt werden.';
      criticalText = `Kritische Bereiche: "${lowestCategory.category}" (Score: ${lowestCategory.average.toFixed(1)}/5) benötigt Aufmerksamkeit.`;
      positiveText = `Ansatzpunkte für Verbesserung in "${highestCategory.category}" (Score: ${highestCategory.average.toFixed(1)}/5).`;
    } else {
      currentText = 'Change-Readiness benötigt erhebliche Unterstützung.';
      criticalText = `Mehrere kritische Bereiche identifiziert, insbesondere "${lowestCategory.category}" (Score: ${lowestCategory.average.toFixed(1)}/5).`;
      positiveText = 'Fokussierte Maßnahmen können die Change-Readiness deutlich verbessern.';
    }

    return {
      current: currentText,
      critical: criticalText,
      positive: positiveText
    };
  }

  /**
   * Calculate overall score for department results
   */
  getDepartmentScore(results: SurveyResult[]): number {
    if (results.length === 0) return 0;
    const totalAverage = results.reduce((sum, result) => sum + result.average, 0) / results.length;
    const percentage = ((totalAverage - 1) / 4) * 100;
    return Math.max(0, Math.min(100, Math.round(percentage)));
  }

  /**
   * Get donut offset for department score
   */
  getDepartmentDonutOffset(score: number): number {
    const circumference = this.getDonutCircumference();
    return circumference - (score / 100) * circumference;
  }
}

