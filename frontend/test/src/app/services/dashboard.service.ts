import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { DashboardKpis, ReadinessData, StakeholderGroupSummary, DashboardData } from '../models/dashboard.model';
import { StakeholderService } from './stakeholder.service';

interface DashboardKpisResponse {
  totalSurveys: number;
  completedSurveys: number;
  openSurveys: number;
  totalStakeholders: number;
  promoters: number;
  neutrals: number;
  critics: number;
  overallReadiness: number;
  activeMeasures: number;
}

interface TrendDataResponse {
  dataPoints: TrendDataPointResponse[];
}

interface TrendDataPointResponse {
  date: string;
  actualValue: number;
  targetValue?: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private dashboardData = signal<DashboardData | null>(null);

  constructor(
    private http: HttpClient,
    private stakeholderService: StakeholderService
  ) {}

  /**
   * Lädt alle Dashboard-Daten vom Backend
   */
  getDashboardData(): Observable<DashboardData> {
    // Lade KPIs und Stakeholder-Gruppen parallel
    return forkJoin({
      kpis: this.http.get<DashboardKpisResponse>(`${environment.apiBaseUrl}/dashboard/kpis`),
      groups: this.stakeholderService.getGroups()
    }).pipe(
      map(({ kpis: kpisResponse, groups }) => {
        // Mappe Backend-Response auf Frontend-Model
        const data: DashboardData = {
          kpis: {
            readinessScore: kpisResponse.overallReadiness || 0,
            readinessTrend: 0, // Wird aus Trend-Daten berechnet
            stakeholderCount: kpisResponse.totalStakeholders || 0,
            stakeholderGroupsCount: groups.length,
            criticsCount: kpisResponse.critics || 0,
            openMeasuresCount: kpisResponse.activeMeasures || 0,
            overdueMeasuresCount: 0 // Nicht im Backend verfügbar
          },
          readiness: {
            overallReadiness: kpisResponse.overallReadiness || 0,
            promoters: kpisResponse.promoters || 0,
            neutrals: kpisResponse.neutrals || 0,
            critics: kpisResponse.critics || 0
          },
          stakeholderGroups: groups.map(g => ({
            id: g.id,
            name: g.name,
            icon: g.icon,
            readiness: g.readiness,
            trend: g.trend,
            participantCount: g.participantCount,
            status: g.status
          }))
        };
        this.dashboardData.set(data);
        return data;
      }),
      catchError(error => {
        console.error('Error loading dashboard data:', error);
        if (error.status === 0 || error.status === undefined) {
          console.error('Backend connection failed. Is the backend running on http://localhost:8080?');
        } else if (error.status === 401 || error.status === 403) {
          console.error('Authentication failed. Please log in again.');
        } else {
          console.error('API error:', error.status, error.message);
        }
        // Return empty data on error
        const emptyData: DashboardData = {
          kpis: {
            readinessScore: 0,
            readinessTrend: 0,
            stakeholderCount: 0,
            stakeholderGroupsCount: 0,
            criticsCount: 0,
            openMeasuresCount: 0,
            overdueMeasuresCount: 0
          },
          readiness: {
            overallReadiness: 0,
            promoters: 0,
            neutrals: 0,
            critics: 0
          },
          stakeholderGroups: []
        };
        this.dashboardData.set(emptyData);
        return of(emptyData);
      })
    );
  }

  /**
   * Lädt nur die KPIs (mit Trend-Daten)
   */
  getKpis(): Observable<DashboardKpis> {
    return forkJoin({
      kpis: this.http.get<DashboardKpisResponse>(`${environment.apiBaseUrl}/dashboard/kpis`),
      trends: this.http.get<TrendDataResponse>(`${environment.apiBaseUrl}/dashboard/trends`).pipe(
        catchError(() => of({ dataPoints: [] } as TrendDataResponse))
      ),
      groups: this.stakeholderService.getGroups().pipe(
        catchError(() => of([]))
      )
    }).pipe(
      map(({ kpis: kpisResponse, trends, groups }) => {
        // Berechne Trend: Vergleich aktueller Wert mit Wert von vor 30 Tagen
        let readinessTrend = 0;
        if (trends.dataPoints.length >= 2) {
          const sortedPoints = [...trends.dataPoints].sort((a, b) => 
            new Date(a.date).getTime() - new Date(b.date).getTime()
          );
          const current = sortedPoints[sortedPoints.length - 1]?.actualValue || 0;
          const previous = sortedPoints[sortedPoints.length - 2]?.actualValue || 0;
          readinessTrend = Math.round(current - previous);
        }

        return {
          readinessScore: kpisResponse.overallReadiness || 0,
          readinessTrend: readinessTrend,
          stakeholderCount: kpisResponse.totalStakeholders || 0,
          stakeholderGroupsCount: groups.length,
          criticsCount: kpisResponse.critics || 0,
          openMeasuresCount: kpisResponse.activeMeasures || 0,
          overdueMeasuresCount: 0 // Nicht im Backend verfügbar
        };
      }),
      catchError(error => {
        console.error('Error loading dashboard KPIs:', error);
        return of({
          readinessScore: 0,
          readinessTrend: 0,
          stakeholderCount: 0,
          stakeholderGroupsCount: 0,
          criticsCount: 0,
          openMeasuresCount: 0,
          overdueMeasuresCount: 0
        });
      })
    );
  }

  /**
   * Lädt nur die Readiness-Daten
   */
  getReadinessData(): Observable<ReadinessData> {
    return this.http.get<DashboardKpisResponse>(`${environment.apiBaseUrl}/dashboard/kpis`).pipe(
      map((kpisResponse: DashboardKpisResponse) => ({
        overallReadiness: kpisResponse.overallReadiness || 0,
        promoters: kpisResponse.promoters || 0,
        neutrals: kpisResponse.neutrals || 0,
        critics: kpisResponse.critics || 0
      })),
      catchError(error => {
        console.error('Error loading readiness data:', error);
        return of({
          overallReadiness: 0,
          promoters: 0,
          neutrals: 0,
          critics: 0
        });
      })
    );
  }

  /**
   * Lädt nur die Stakeholder-Gruppen
   */
  getStakeholderGroups(): Observable<StakeholderGroupSummary[]> {
    return this.stakeholderService.getGroups().pipe(
      map(groups => groups.map(g => ({
        id: g.id,
        name: g.name,
        icon: g.icon,
        readiness: g.readiness,
        trend: g.trend,
        participantCount: g.participantCount,
        status: g.status
      }))),
      catchError(error => {
        console.error('Error loading stakeholder groups:', error);
        return of([]);
      })
    );
  }

  /**
   * Aktueller Dashboard-Daten-State (Signal)
   */
  getDashboardDataSignal() {
    return this.dashboardData.asReadonly();
  }
}


