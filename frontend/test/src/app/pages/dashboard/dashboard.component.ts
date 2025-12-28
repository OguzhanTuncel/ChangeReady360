import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance, SurveyResponse } from '../../models/survey.model';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardKpis } from '../../models/dashboard.model';

interface StatCard {
  title: string;
  value: string | number;
  icon: string;
  color: string;
  change?: string;
  route?: string;
}

interface Activity {
  id: string;
  title: string;
  description: string;
  date: string;
  type: 'survey' | 'result' | 'system';
  route?: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatChipsModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  instances = signal<SurveyInstance[]>([]);
  responses = signal<SurveyResponse[]>([]);
  isLoading = signal(true);
  kpis = signal<DashboardKpis | null>(null);

  constructor(
    private surveyService: SurveyService,
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading.set(true);
    
    // Load Dashboard KPIs
    this.dashboardService.getKpis().subscribe({
      next: (kpis) => {
        this.kpis.set(kpis);
      }
    });

    this.surveyService.getUserInstances().subscribe({
      next: (instances) => {
        this.instances.set(instances);
      }
    });

    this.surveyService.getUserResponses().subscribe({
      next: (responses) => {
        this.responses.set(responses);
        this.isLoading.set(false);
      }
    });
  }

  get kpiCards(): StatCard[] {
    const kpisData = this.kpis();
    if (!kpisData) {
      return [];
    }

    return [
      {
        title: 'Readiness Score',
        value: `${kpisData.readinessScore}%`,
        icon: 'trending_up',
        color: 'primary',
        route: '/app/results',
        change: kpisData.readinessTrend !== 0 
          ? `${kpisData.readinessTrend > 0 ? '+' : ''}${kpisData.readinessTrend}%`
          : undefined
      },
      {
        title: 'Stakeholder',
        value: kpisData.stakeholderCount,
        icon: 'people',
        color: 'primary',
        route: '/app/stakeholder',
        change: kpisData.stakeholderGroupsCount > 0 
          ? `in ${kpisData.stakeholderGroupsCount} Gruppen`
          : undefined
      },
      {
        title: 'Kritiker',
        value: kpisData.criticsCount,
        icon: 'info',
        color: 'warn',
        route: '/app/stakeholder',
        change: kpisData.criticsCount > 0 
          ? 'benötigen Aufmerksamkeit'
          : undefined
      },
      {
        title: 'Offene Maßnahmen',
        value: kpisData.openMeasuresCount,
        icon: 'visibility',
        color: 'primary',
        route: undefined, // TODO: Route zu Maßnahmen-Seite wenn vorhanden
        change: kpisData.overdueMeasuresCount > 0 
          ? `davon ${kpisData.overdueMeasuresCount} überfällig`
          : undefined
      }
    ];
  }

  get statCards(): StatCard[] {
    // Legacy method - wird später entfernt wenn alles auf kpiCards umgestellt ist
    return this.kpiCards;
  }

  get recentActivities(): Activity[] {
    const instances = this.instances();
    const activities: Activity[] = [];

    // Add completed surveys
    instances
      .filter(i => i.submittedAt)
      .sort((a, b) => (b.submittedAt?.getTime() || 0) - (a.submittedAt?.getTime() || 0))
      .slice(0, 3)
      .forEach(instance => {
        activities.push({
          id: instance.id,
          title: 'Fragebogen abgeschlossen',
          description: instance.template.name,
          date: this.formatDate(instance.submittedAt!),
          type: 'result',
          route: `/app/survey/${instance.id}/summary`
        });
      });

    // Add started surveys
    instances
      .filter(i => !i.submittedAt)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())
      .slice(0, 2)
      .forEach(instance => {
        activities.push({
          id: instance.id,
          title: 'Umfrage gestartet',
          description: instance.template.name,
          date: this.formatDate(instance.createdAt),
          type: 'survey',
          route: `/app/survey/${instance.id}/fill`
        });
      });

    return activities.sort((a, b) => {
      const dateA = this.parseActivityDate(a.date);
      const dateB = this.parseActivityDate(b.date);
      return dateB.getTime() - dateA.getTime();
    }).slice(0, 4);
  }

  get openSurveys() {
    return this.getOpenInstances().map(instance => ({
      id: instance.id,
      title: instance.template.name,
      participants: 1, // Single user instance
      status: 'in_progress' as const,
      route: `/app/survey/${instance.id}/fill`
    }));
  }

  getOpenInstances(): SurveyInstance[] {
    return this.instances().filter(i => !i.submittedAt);
  }

  getCompletedInstances(): SurveyInstance[] {
    return this.instances().filter(i => i.submittedAt);
  }

  calculateOverallScore(): number {
    const responses = this.responses();
    if (responses.length === 0) return 0;

    // Sammle alle Antwortwerte
    const allAnswers: number[] = [];
    responses.forEach(response => {
      response.answers.forEach(answer => {
        if (answer.value !== null) {
          allAnswers.push(answer.value);
        }
      });
    });

    if (allAnswers.length === 0) return 0;

    // Berechne den Durchschnitt aller Antworten (1-5 Skala)
    const totalAverage = allAnswers.reduce((sum, val) => sum + val, 0) / allAnswers.length;
    
    // Skaliere von 1-5 auf 0-100% (gleiche Formel wie Results)
    // 1 = 0%, 3 = 50%, 5 = 100%
    const percentage = ((totalAverage - 1) / 4) * 100;
    return Math.max(0, Math.min(100, Math.round(percentage)));
  }

  formatDate(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (days === 0) return 'Heute';
    if (days === 1) return 'Gestern';
    if (days < 7) return `Vor ${days} Tagen`;
    return date.toLocaleDateString('de-DE');
  }

  parseActivityDate(dateStr: string): Date {
    if (dateStr === 'Heute') return new Date();
    if (dateStr === 'Gestern') {
      const d = new Date();
      d.setDate(d.getDate() - 1);
      return d;
    }
    if (dateStr.startsWith('Vor ')) {
      const days = parseInt(dateStr.match(/\d+/)?.[0] || '0');
      const d = new Date();
      d.setDate(d.getDate() - days);
      return d;
    }
    return new Date(dateStr);
  }

  navigateToCard(route: string) {
    this.router.navigate([route]);
  }

  navigateToActivity(route: string) {
    this.router.navigate([route]);
  }

  navigateToSurvey(route: string) {
    this.router.navigate([route]);
  }

  getActivityIcon(type: string): string {
    switch (type) {
      case 'survey':
        return 'assignment';
      case 'result':
        return 'bar_chart';
      case 'system':
        return 'settings';
      default:
        return 'info';
    }
  }

  getActivityColor(type: string): string {
    switch (type) {
      case 'survey':
        return 'var(--innovera-blue)';
      case 'result':
        return 'var(--change-ready-green)';
      case 'system':
        return 'var(--gray-600)';
      default:
        return 'var(--gray-500)';
    }
  }

  getCardColor(color: string): string {
    switch (color) {
      case 'primary':
        return 'var(--innovera-blue)';
      case 'success':
        return 'var(--change-ready-green)';
      case 'accent':
        return 'var(--gold-accent)';
      default:
        return 'var(--gray-500)';
    }
  }

  getActivityCount(type: 'survey' | 'result'): number {
    const instances = this.instances();
    if (type === 'survey') {
      return instances.filter(i => !i.submittedAt).length;
    } else {
      return instances.filter(i => i.submittedAt).length;
    }
  }

  getCurrentDate(): string {
    const date = new Date();
    const options: Intl.DateTimeFormatOptions = { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    };
    return date.toLocaleDateString('de-DE', options);
  }
}

