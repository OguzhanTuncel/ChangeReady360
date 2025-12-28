import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { DashboardKpis, ReadinessData, StakeholderGroupSummary, DashboardData } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private dashboardData = signal<DashboardData | null>(null);

  constructor(private http: HttpClient) {}

  /**
   * Lädt alle Dashboard-Daten vom Backend
   */
  getDashboardData(): Observable<DashboardData> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/dashboard/kpis
    // Aktuell: Mock-Daten zurückgeben, bis Backend-Endpoint verfügbar ist
    return of({
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
    }).pipe(
      map(data => {
        this.dashboardData.set(data);
        return data;
      }),
      catchError(error => {
        console.error('Error loading dashboard data:', error);
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
   * Lädt nur die KPIs
   */
  getKpis(): Observable<DashboardKpis> {
    return this.getDashboardData().pipe(
      map(data => data.kpis)
    );
  }

  /**
   * Lädt nur die Readiness-Daten
   */
  getReadinessData(): Observable<ReadinessData> {
    return this.getDashboardData().pipe(
      map(data => data.readiness)
    );
  }

  /**
   * Lädt nur die Stakeholder-Gruppen
   */
  getStakeholderGroups(): Observable<StakeholderGroupSummary[]> {
    return this.getDashboardData().pipe(
      map(data => data.stakeholderGroups)
    );
  }

  /**
   * Aktueller Dashboard-Daten-State (Signal)
   */
  getDashboardDataSignal() {
    return this.dashboardData.asReadonly();
  }
}

