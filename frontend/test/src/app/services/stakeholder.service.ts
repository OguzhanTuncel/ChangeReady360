import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { StakeholderGroup, StakeholderGroupDetail, StakeholderKpis, StakeholderPerson } from '../models/stakeholder.model';

@Injectable({
  providedIn: 'root'
})
export class StakeholderService {
  private groups = signal<StakeholderGroup[]>([]);
  private kpis = signal<StakeholderKpis | null>(null);

  constructor(private http: HttpClient) {}

  /**
   * Lädt alle Stakeholder-Gruppen vom Backend
   */
  getGroups(): Observable<StakeholderGroup[]> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/stakeholder/groups
    // Aktuell: Mock-Daten zurückgeben, bis Backend-Endpoint verfügbar ist
    return of([]).pipe(
      map(data => {
        this.groups.set(data);
        return data;
      }),
      catchError(error => {
        console.error('Error loading stakeholder groups:', error);
        const emptyData: StakeholderGroup[] = [];
        this.groups.set(emptyData);
        return of(emptyData);
      })
    );
  }

  /**
   * Lädt Details einer Stakeholder-Gruppe
   */
  getGroupDetail(groupId: string): Observable<StakeholderGroupDetail> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/stakeholder/groups/{id}
    return of({
      id: groupId,
      name: '',
      icon: '',
      readiness: 0,
      trend: 0,
      participantCount: 0,
      promoters: 0,
      neutrals: 0,
      critics: 0,
      status: 'ready',
      impact: 'Hoch'
    }).pipe(
      catchError(error => {
        console.error('Error loading group detail:', error);
        throw error;
      })
    );
  }

  /**
   * Lädt Stakeholder-KPIs
   */
  getKpis(): Observable<StakeholderKpis> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/stakeholder/kpis
    return of({
      total: 0,
      promoters: 0,
      neutrals: 0,
      critics: 0
    }).pipe(
      map(data => {
        this.kpis.set(data);
        return data;
      }),
      catchError(error => {
        console.error('Error loading stakeholder KPIs:', error);
        const emptyData: StakeholderKpis = {
          total: 0,
          promoters: 0,
          neutrals: 0,
          critics: 0
        };
        this.kpis.set(emptyData);
        return of(emptyData);
      })
    );
  }

  /**
   * Lädt Stakeholder-Personen einer Gruppe
   */
  getGroupPersons(groupId: string): Observable<StakeholderPerson[]> {
    // TODO: Backend-Endpoint implementieren: GET /api/v1/stakeholder/groups/{id}/persons
    return of([]).pipe(
      catchError(error => {
        console.error('Error loading group persons:', error);
        return of([]);
      })
    );
  }

  /**
   * Aktueller Groups-State (Signal)
   */
  getGroupsSignal() {
    return this.groups.asReadonly();
  }

  /**
   * Aktueller KPIs-State (Signal)
   */
  getKpisSignal() {
    return this.kpis.asReadonly();
  }
}

