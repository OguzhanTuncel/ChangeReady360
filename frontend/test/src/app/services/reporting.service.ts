import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ReportingData, ManagementSummary, DepartmentReadiness, TrendChartData } from '../models/reporting.model';
import { SurveyResult, DepartmentResult } from '../models/survey.model';

interface ReportingDataResponse {
  summary: ManagementSummaryResponse;
  departments: DepartmentReadinessResponse[];
  trend: TrendDataResponse;
}

interface ManagementSummaryResponse {
  overallReadiness: number;
  readinessTrend: number;
  stakeholderCount: number;
  activeMeasuresCount: number;
  date: string;
}

interface DepartmentReadinessResponse {
  id: string;
  name: string;
  readiness: number;
  color: string;
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
export class ReportingService {
  private reportingData = signal<ReportingData | null>(null);

  constructor(private http: HttpClient) {}

  /**
   * Lädt alle Reporting-Daten (Summary, Departments, Trend)
   */
  getReportingData(): Observable<ReportingData> {
    return this.http.get<ReportingDataResponse>(`${environment.apiBaseUrl}/reporting/data`).pipe(
      map((response: ReportingDataResponse) => {
        const data: ReportingData = {
          summary: {
            overallReadiness: response.summary.overallReadiness || 0,
            readinessTrend: response.summary.readinessTrend || 0,
            stakeholderCount: response.summary.stakeholderCount || 0,
            activeMeasuresCount: response.summary.activeMeasuresCount || 0,
            date: new Date(response.summary.date)
          },
          departments: response.departments.map(d => ({
            id: d.id,
            name: d.name,
            readiness: d.readiness || 0,
            color: d.color || '#56A080'
          })),
          trend: {
            dataPoints: response.trend.dataPoints.map(p => ({
              date: new Date(p.date),
              actualValue: p.actualValue || 0,
              targetValue: p.targetValue
            }))
          }
        };
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
    return this.http.get<ManagementSummaryResponse>(`${environment.apiBaseUrl}/reporting/summary`).pipe(
      map((response: ManagementSummaryResponse) => ({
        overallReadiness: response.overallReadiness || 0,
        readinessTrend: response.readinessTrend || 0,
        stakeholderCount: response.stakeholderCount || 0,
        activeMeasuresCount: response.activeMeasuresCount || 0,
        date: new Date(response.date)
      })),
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
    return this.http.get<DepartmentReadinessResponse[]>(`${environment.apiBaseUrl}/reporting/departments`).pipe(
      map((response: DepartmentReadinessResponse[]) => 
        response.map(d => ({
          id: d.id,
          name: d.name,
          readiness: d.readiness || 0,
          color: d.color || '#56A080'
        }))
      ),
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
    return this.http.get<TrendDataResponse>(`${environment.apiBaseUrl}/reporting/trends`).pipe(
      map((response: TrendDataResponse) => ({
        dataPoints: response.dataPoints.map(p => ({
          date: new Date(p.date),
          actualValue: p.actualValue || 0,
          targetValue: p.targetValue
        }))
      })),
      catchError(error => {
        console.error('Error loading trend data:', error);
        return of({
          dataPoints: []
        });
      })
    );
  }

  /**
   * Lädt Template-spezifische Results (kategorisiert nach Category/Subcategory)
   * Backend: GET /api/v1/reporting/templates/{id}/results
   */
  getTemplateResults(templateId: string): Observable<SurveyResult[]> {
    return this.http.get<SurveyResultResponse[]>(`${environment.apiBaseUrl}/reporting/templates/${templateId}/results`).pipe(
      map((response: SurveyResultResponse[]) =>
        response.map(r => ({
          category: r.category,
          subcategory: r.subcategory,
          average: r.average || 0,
          answeredCount: r.answeredCount || 0,
          totalCount: r.totalCount || 0,
          reverseItems: r.reverseItems || []
        }))
      ),
      catchError(error => {
        console.error(`Error loading template results for ${templateId}:`, error);
        return of([]);
      })
    );
  }

  /**
   * Lädt Template-spezifische Department-Results
   * Backend: GET /api/v1/reporting/templates/{id}/results/departments
   */
  getTemplateDepartmentResults(templateId: string): Observable<DepartmentResult[]> {
    return this.http.get<TemplateDepartmentResultResponse[]>(`${environment.apiBaseUrl}/reporting/templates/${templateId}/results/departments`).pipe(
      map((response: TemplateDepartmentResultResponse[]) =>
        response.map(d => ({
          department: d.department as any,
          participantCount: d.participantCount || 0,
          results: d.results.map(r => ({
            category: r.category,
            subcategory: r.subcategory,
            average: r.average || 0,
            answeredCount: r.answeredCount || 0,
            totalCount: r.totalCount || 0,
            reverseItems: r.reverseItems || []
          }))
        }))
      ),
      catchError(error => {
        console.error(`Error loading template department results for ${templateId}:`, error);
        return of([]);
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

interface SurveyResultResponse {
  category: string;
  subcategory: string;
  average: number;
  answeredCount: number;
  totalCount: number;
  reverseItems: string[];
}

interface TemplateDepartmentResultResponse {
  department: string;
  departmentName: string;
  participantCount: number;
  results: SurveyResultResponse[];
}


