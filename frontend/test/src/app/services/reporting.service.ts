import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ReportingData, ManagementSummary, DepartmentReadiness, TrendChartData } from '../models/reporting.model';

@Injectable({
  providedIn: 'root'
})
export class ReportingService {
  private reportingData = signal<ReportingData | null>(null);

  constructor(private http: HttpClient) {}

  /**
   * Lädt alle Reporting-Daten (Summary, Departments, Trend)
   */
  getReportingData(): Observable<ReportingData> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/reporting/data
    // Aktuell: Mock-Daten zurückgeben, bis Backend-Endpoint verfügbar ist
    return of({
      summary: {
        overallReadiness: 0,
        readinessTrend: 0,
        stakeholderCount: 0,
        activeMeasuresCount: 0,
        date: new Date()
      },
      departments: [],
      trend: {
        dataPoints: []
      }
    }).pipe(
      map(data => {
        this.reportingData.set(data);
        return data;
      }),
      catchError(error => {
        console.error('Error loading reporting data:', error);
        const emptyData: ReportingData = {
          summary: {
            overallReadiness: 0,
            readinessTrend: 0,
            stakeholderCount: 0,
            activeMeasuresCount: 0,
            date: new Date()
          },
          departments: [],
          trend: {
            dataPoints: []
          }
        };
        this.reportingData.set(emptyData);
        return of(emptyData);
      })
    );
  }

  /**
   * Lädt Management Summary
   */
  getManagementSummary(): Observable<ManagementSummary> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/reporting/summary
    return of({
      overallReadiness: 0,
      readinessTrend: 0,
      stakeholderCount: 0,
      activeMeasuresCount: 0,
      date: new Date()
    }).pipe(
      catchError(error => {
        console.error('Error loading management summary:', error);
        return of({
          overallReadiness: 0,
          readinessTrend: 0,
          stakeholderCount: 0,
          activeMeasuresCount: 0,
          date: new Date()
        });
      })
    );
  }

  /**
   * Lädt Abteilungs-Readiness-Daten
   */
  getDepartmentReadiness(): Observable<DepartmentReadiness[]> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/reporting/departments
    return of([]).pipe(
      catchError(error => {
        console.error('Error loading department readiness:', error);
        return of([]);
      })
    );
  }

  /**
   * Lädt Trend-Daten für Chart
   */
  getTrendData(): Observable<TrendChartData> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/reporting/trends
    return of({
      dataPoints: []
    }).pipe(
      catchError(error => {
        console.error('Error loading trend data:', error);
        return of({
          dataPoints: []
        });
      })
    );
  }

  /**
   * Aktueller Reporting-Data-State (Signal)
   */
  getReportingDataSignal() {
    return this.reportingData.asReadonly();
  }
}

