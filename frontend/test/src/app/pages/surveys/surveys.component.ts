import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
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
      }
    });

    this.surveyService.getUserInstances().subscribe({
      next: (instances) => {
        this.userInstances.set(instances);
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
    const total = this.getTotalQuestions(instance);
    const answered = instance.answers.length;
    return { label: `${answered}/${total} beantwortet`, color: 'accent' };
  }

  getTotalQuestions(instance: SurveyInstance): number {
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
    
    // Map ADKAR areas to category names (assuming categories match ADKAR structure)
    const areaMap: Record<string, string[]> = {
      'KNOWLEDGE': ['Wissen', 'Knowledge', 'Wissen (Knowledge)'],
      'ABILITY': ['Fähigkeit', 'Ability', 'Fähigkeit (Ability)'],
      'DESIRE': ['Motivation', 'Desire', 'Motivation (Desire)'],
      'REINFORCEMENT': ['Kommunikation', 'Reinforcement', 'Kommunikation (Reinforcement)']
    };
    
    const template = templates[0];
    const categoryNames = areaMap[area] || [];
    let count = 0;
    
    template.categories.forEach(category => {
      if (categoryNames.some(name => category.name.includes(name))) {
        category.subcategories.forEach(subcategory => {
          count += subcategory.questions.length;
        });
      }
    });
    
    // If no exact match, distribute questions evenly across 4 areas
    if (count === 0) {
      return Math.ceil(this.getTotalQuestions() / 4);
    }
    
    return count;
  }

  getEstimatedDuration(): number {
    // Estimate: ~2 minutes per question
    const totalQuestions = this.getTotalQuestions();
    return Math.ceil(totalQuestions * 2);
  }

  startAssessment() {
    const templates = this.activeTemplates();
    if (templates.length > 0) {
      // Navigate to survey start page with first available template
      this.router.navigate(['/app/survey', templates[0].id, 'start']);
    }
  }
}

