import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { StakeholderGroup, StakeholderGroupDetail, StakeholderKpis, StakeholderPerson } from '../models/stakeholder.model';

interface StakeholderGroupResponse {
  id: number;
  name: string;
  icon: string;
  impact: string;
  readiness: number;
  trend: number;
  participantCount: number;
  promoters: number;
  neutrals: number;
  critics: number;
  status: string;
}

interface StakeholderGroupDetailResponse {
  id: number;
  name: string;
  icon: string;
  impact: string;
  description?: string;
  readiness: number;
  trend: number;
  participantCount: number;
  promoters: number;
  neutrals: number;
  critics: number;
  status: string;
  persons: StakeholderPersonResponse[];
  history: ReadinessHistoryPointResponse[];
}

interface StakeholderPersonResponse {
  id: number;
  name: string;
  role?: string;
  category: string;
}

interface ReadinessHistoryPointResponse {
  date: string;
  readiness: number;
}

interface StakeholderKpisResponse {
  total: number;
  promoters: number;
  neutrals: number;
  critics: number;
}

interface StakeholderGroupCreateRequest {
  name: string;
  icon?: string;
  impact: string;
  description?: string;
}

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
    return this.http.get<StakeholderGroupResponse[]>(`${environment.apiBaseUrl}/stakeholder/groups`).pipe(
      map((data: StakeholderGroupResponse[]) => {
        const groups: StakeholderGroup[] = data.map(item => ({
          id: item.id.toString(),
          name: item.name,
          icon: item.icon,
          impact: item.impact as any,
          readiness: item.readiness,
          trend: item.trend,
          participantCount: item.participantCount,
          promoters: item.promoters,
          neutrals: item.neutrals,
          critics: item.critics,
          status: item.status as any
        }));
        this.groups.set(groups);
        return groups;
      }),
      catchError(error => {
        console.error('Error loading stakeholder groups:', error);
        if (error.status === 0 || error.status === undefined) {
          console.error('Backend connection failed. Is the backend running on http://localhost:8080?');
        } else if (error.status === 401 || error.status === 403) {
          console.error('Authentication failed. Please log in again.');
        } else {
          console.error('API error:', error.status, error.message);
        }
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
    return this.http.get<StakeholderGroupDetailResponse>(`${environment.apiBaseUrl}/stakeholder/groups/${groupId}`).pipe(
      map((data: StakeholderGroupDetailResponse) => ({
        id: data.id.toString(),
        name: data.name,
        icon: data.icon,
        impact: data.impact as any,
        description: data.description,
        readiness: data.readiness,
        trend: data.trend,
        participantCount: data.participantCount,
        promoters: data.promoters,
        neutrals: data.neutrals,
        critics: data.critics,
        status: data.status as any,
        historicalReadiness: data.history?.map(h => ({
          date: new Date(h.date),
          readiness: h.readiness
        }))
      })),
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
    return this.http.get<StakeholderKpisResponse>(`${environment.apiBaseUrl}/stakeholder/kpis`).pipe(
      map((data: StakeholderKpisResponse) => {
        const kpis: StakeholderKpis = {
          total: data.total,
          promoters: data.promoters,
          neutrals: data.neutrals,
          critics: data.critics
        };
        this.kpis.set(kpis);
        return kpis;
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
    return this.http.get<StakeholderPersonResponse[]>(`${environment.apiBaseUrl}/stakeholder/groups/${groupId}/persons`).pipe(
      map((data: StakeholderPersonResponse[]) => 
        data.map(item => ({
          id: item.id.toString(),
          name: item.name,
          role: item.role,
          department: '', // Backend liefert kein department, daher leerer String
          category: item.category as any
        }))
      ),
      catchError(error => {
        console.error('Error loading group persons:', error);
        return of([]);
      })
    );
  }

  /**
   * Erstellt eine neue Stakeholder-Gruppe
   */
  createGroup(request: StakeholderGroupCreateRequest): Observable<StakeholderGroup> {
    return this.http.post<StakeholderGroupResponse>(`${environment.apiBaseUrl}/stakeholder/groups`, request).pipe(
      map((data: StakeholderGroupResponse) => {
        const group: StakeholderGroup = {
          id: data.id.toString(),
          name: data.name,
          icon: data.icon,
          impact: data.impact as any,
          readiness: data.readiness,
          trend: data.trend,
          participantCount: data.participantCount,
          promoters: data.promoters,
          neutrals: data.neutrals,
          critics: data.critics,
          status: data.status as any
        };
        // Aktualisiere lokalen State
        this.groups.update(groups => [...groups, group]);
        return group;
      }),
      catchError(error => {
        console.error('Error creating stakeholder group:', error);
        throw error;
      })
    );
  }

  /**
   * Aktualisiert eine Stakeholder-Gruppe
   */
  updateGroup(groupId: string, request: Partial<StakeholderGroupCreateRequest>): Observable<StakeholderGroup> {
    return this.http.put<StakeholderGroupResponse>(`${environment.apiBaseUrl}/stakeholder/groups/${groupId}`, request).pipe(
      map((data: StakeholderGroupResponse) => {
        const group: StakeholderGroup = {
          id: data.id.toString(),
          name: data.name,
          icon: data.icon,
          impact: data.impact as any,
          readiness: data.readiness,
          trend: data.trend,
          participantCount: data.participantCount,
          promoters: data.promoters,
          neutrals: data.neutrals,
          critics: data.critics,
          status: data.status as any
        };
        // Aktualisiere lokalen State
        this.groups.update(groups => groups.map(g => g.id === groupId ? group : g));
        return group;
      }),
      catchError(error => {
        console.error('Error updating stakeholder group:', error);
        throw error;
      })
    );
  }

  /**
   * Löscht eine Stakeholder-Gruppe
   * Backend: DELETE /api/v1/stakeholder/groups/{id}
   */
  deleteGroup(groupId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/stakeholder/groups/${groupId}`).pipe(
      catchError(error => {
        console.error('Error deleting stakeholder group:', error);
        throw error;
      })
    );
  }

  /**
   * Fügt eine Person zu einer Gruppe hinzu
   */
  addPersonToGroup(groupId: string, person: { name: string; role?: string; email?: string }): Observable<StakeholderPerson> {
    return this.http.post<StakeholderPersonResponse>(`${environment.apiBaseUrl}/stakeholder/groups/${groupId}/persons`, person).pipe(
      map((data: StakeholderPersonResponse) => ({
        id: data.id.toString(),
        name: data.name,
        role: data.role,
        department: '', // Backend liefert kein department, daher leerer String
        category: data.category as any
      })),
      catchError(error => {
        console.error('Error adding person to group:', error);
        throw error;
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

