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

  constructor(private surveyService: SurveyService) {}

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
}
