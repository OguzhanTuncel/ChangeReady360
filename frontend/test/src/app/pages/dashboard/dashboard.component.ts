import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance, SurveyResponse } from '../../models/survey.model';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardKpis, ReadinessData, StakeholderGroupSummary } from '../../models/dashboard.model';
import { DonutChartComponent } from '../../components/donut-chart/donut-chart.component';

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
    MatChipsModule,
    DonutChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  instances = signal<SurveyInstance[]>([]);
  responses = signal<SurveyResponse[]>([]);
  isLoading = signal(true);
  kpis = signal<DashboardKpis | null>(null);
  readinessData = signal<ReadinessData | null>(null);
  stakeholderGroups = signal<StakeholderGroupSummary[]>([]);
  errorMessage = signal<string | null>(null);

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
    this.errorMessage.set(null);
    
    // Load all data in parallel with proper error handling
    forkJoin({
      kpis: this.dashboardService.getKpis().pipe(
        catchError(error => {
          console.error('Error loading dashboard KPIs:', error);
          this.handleError(error, 'Dashboard-KPIs');
          return of(null);
        })
      ),
      readiness: this.dashboardService.getReadinessData().pipe(
        catchError(error => {
          console.error('Error loading readiness data:', error);
          this.handleError(error, 'Readiness-Daten');
          return of(null);
        })
      ),
      groups: this.dashboardService.getStakeholderGroups().pipe(
        catchError(error => {
          console.error('Error loading stakeholder groups:', error);
          this.handleError(error, 'Stakeholder-Gruppen');
          return of([]);
        })
      ),
      instances: this.surveyService.getUserInstances().pipe(
        catchError(error => {
          console.error('Error loading survey instances:', error);
          this.handleError(error, 'Umfragen');
          return of([]);
        })
      ),
      responses: this.surveyService.getUserResponses().pipe(
        catchError(error => {
          console.error('Error loading survey responses:', error);
          this.handleError(error, 'Umfrage-Antworten');
          return of([]);
        })
      )
    }).pipe(
      finalize(() => {
        // Always set isLoading to false when all requests complete (success or error)
        this.isLoading.set(false);
      })
    ).subscribe({
      next: (data) => {
        this.kpis.set(data.kpis);
        this.readinessData.set(data.readiness);
        this.stakeholderGroups.set(data.groups);
        this.instances.set(data.instances);
        this.responses.set(data.responses);
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.handleError(error, 'Dashboard-Daten');
        // Set default values on error
        this.kpis.set(null);
        this.readinessData.set(null);
        this.stakeholderGroups.set([]);
        this.instances.set([]);
        this.responses.set([]);
      }
    });
  }

  private handleError(error: any, context: string): void {
    if (error.status === 0 || error.status === undefined) {
      this.errorMessage.set(`Verbindung zum Backend fehlgeschlagen. Bitte stellen Sie sicher, dass der Backend-Server auf Port 8080 läuft.`);
    } else if (error.status === 401 || error.status === 403) {
      this.errorMessage.set(`Authentifizierung fehlgeschlagen. Bitte loggen Sie sich erneut ein.`);
    } else {
      this.errorMessage.set(`Fehler beim Laden von ${context}. Status: ${error.status}`);
    }
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
        // UX: on dashboard we show number of stakeholder groups (created groups),
        // because that is what users create/manage here.
        value: kpisData.stakeholderGroupsCount,
        icon: 'people',
        color: 'primary',
        route: '/app/stakeholder',
        change: kpisData.stakeholderCount > 0
          ? `${kpisData.stakeholderCount} Personen`
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
      // Participants: Single-user instances haben immer 1 Teilnehmer
      // Wenn Backend später participantCount liefert, nutze: instance.participantCount || 1
      participants: 1,
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

  getStatusBadgeClass(status: 'ready' | 'attention' | 'critical'): string {
    switch (status) {
      case 'ready':
        return 'badge-ready';
      case 'attention':
        return 'badge-attention';
      case 'critical':
        return 'badge-critical';
      default:
        return '';
    }
  }

  getStatusBadgeText(status: 'ready' | 'attention' | 'critical'): string {
    switch (status) {
      case 'ready':
        return 'Bereit';
      case 'attention':
        return 'Aufmerksamkeit';
      case 'critical':
        return 'Kritisch';
      default:
        return '';
    }
  }

  getTrendIcon(trend: number): string {
    if (trend > 0) return 'trending_up';
    if (trend < 0) return 'trending_down';
    return 'trending_flat';
  }

  getTrendClass(trend: number): string {
    if (trend > 0) return 'trend-positive';
    if (trend < 0) return 'trend-negative';
    return 'trend-neutral';
  }

  formatTrend(trend: number): string {
    if (trend === 0) return '0%';
    return `${trend > 0 ? '+' : ''}${trend}%`;
  }
}

