import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';
import { SurveyService } from '../../services/survey.service';
import { SurveyTemplate, SurveyInstance } from '../../models/survey.model';

@Component({
  selector: 'app-surveys',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    DatePipe
  ],
  templateUrl: './surveys.component.html',
  styleUrl: './surveys.component.css'
})
export class SurveysComponent implements OnInit {
  activeTemplates = signal<SurveyTemplate[]>([]);
  userInstances = signal<SurveyInstance[]>([]);
  isLoading = signal(true);

  constructor(
    private surveyService: SurveyService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading.set(true);
    
    this.surveyService.getActiveTemplates().subscribe({
      next: (templates) => {
        this.activeTemplates.set(templates);
      },
      error: (error) => {
        console.error('Error loading active templates:', error);
        this.activeTemplates.set([]);
      }
    });

    this.surveyService.getUserInstances().subscribe({
      next: (instances) => {
        this.userInstances.set(instances);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading user instances:', error);
        this.userInstances.set([]);
        this.isLoading.set(false);
      }
    });
  }

  getOpenInstances(): SurveyInstance[] {
    return this.userInstances().filter(i => !i.submittedAt);
  }

  getCompletedInstances(): SurveyInstance[] {
    return this.userInstances().filter(i => i.submittedAt);
  }

  getInstanceStatus(instance: SurveyInstance): { label: string; color: string } {
    if (instance.submittedAt) {
      return { label: 'Abgeschlossen', color: 'primary' };
    }
    // Nutze totalQuestions/answeredQuestions vom Backend wenn verfügbar, sonst Fallback
    const total = (instance as any).totalQuestions || this.getTotalQuestionsForInstance(instance);
    const answered = (instance as any).answeredQuestions || instance.answers.length;
    return { label: `${answered}/${total} beantwortet`, color: 'accent' };
  }

  getTotalQuestionsForInstance(instance: SurveyInstance): number {
    if (!instance.participantType) return 0;
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

  getTotalQuestions(): number {
    // Calculate total questions from active templates
    const templates = this.activeTemplates();
    if (templates.length === 0) return 0;
    
    // Use first template as reference (assuming all templates have similar structure)
    const template = templates[0];
    let count = 0;
    template.categories.forEach(category => {
      category.subcategories.forEach(subcategory => {
        count += subcategory.questions.length;
      });
    });
    return count;
  }

  getTotalCategories(): number {
    const templates = this.activeTemplates();
    if (templates.length === 0) return 0;
    
    // Count unique categories across templates (assuming ADKAR structure)
    const template = templates[0];
    return template.categories.length;
  }

  getQuestionsForAdkarArea(area: 'KNOWLEDGE' | 'ABILITY' | 'DESIRE' | 'REINFORCEMENT'): number {
    const templates = this.activeTemplates();
    if (templates.length === 0) return 0;
    
    const template = templates[0];
    let count = 0;

    // Real backend template uses 12 categories (e.g. "Verständnis", "Unterstützung", ...)
    // We map them into the 4 cards shown on this screen (Wissen/Fähigkeit/Motivation/Kommunikation).
    const normalize = (input: string) => {
      return (input || '')
        .trim()
        .toLowerCase()
        .replace(/ä/g, 'ae')
        .replace(/ö/g, 'oe')
        .replace(/ü/g, 'ue')
        .replace(/ß/g, 'ss')
        .replace(/&/g, 'und')
        .replace(/[^a-z0-9]+/g, '');
    };

    const buckets: Record<typeof area, string[]> = {
      KNOWLEDGE: [
        normalize('Verständnis'),
        normalize('Transparenz, Entscheidungsfindung und Einbindung im Change-Prozess')
      ],
      ABILITY: [
        normalize('Ressourcen – Zeit'),
        normalize('Ressourcen – Equipment'),
        normalize('Prozesse'),
        normalize('Kompetenzen')
      ],
      DESIRE: [
        normalize('Persönliche Vorteile und Nachteile'),
        normalize('Einstellung und Haltung')
      ],
      REINFORCEMENT: [
        normalize('Unterstützung'),
        normalize('Menge und Frequenz der Kommunikation'),
        normalize('Effektivität der Kommunikation'),
        normalize('Offener Umgang')
      ]
    };

    const allowed = new Set(buckets[area]);
    template.categories.forEach(category => {
      if (!allowed.has(normalize(category.name))) return;
      category.subcategories.forEach(subcategory => {
        count += subcategory.questions.length;
      });
    });

    return count;
  }

  getEstimatedDuration(): number {
    // UX requirement: fixed 15 minutes (no mock-data; just a static UX hint).
    return 15;
  }

  startAssessment() {
    const templates = this.activeTemplates();
    if (templates.length > 0) {
      // Navigate to survey start page with first available template
      this.router.navigate(['/app/survey', templates[0].id, 'start']);
    }
  }
}

