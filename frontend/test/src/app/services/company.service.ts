import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Company } from '../models/survey.model';
import { environment } from '../../environments/environment';

interface CompanyResponse {
  id: number;
  name: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private companies = signal<Company[]>([]);
  private selectedCompanyId = signal<number | null>(null);

  constructor(private http: HttpClient) {
    // Gespeicherte Company aus localStorage laden
    this.loadSelectedCompanyFromStorage();
  }

  /**
   * Get all companies (for SYSTEM_ADMIN only)
   * Backend: GET /api/v1/admin/companies
   */
  getCompanies(): Observable<Company[]> {
    return this.http.get<CompanyResponse[]>(`${environment.apiBaseUrl}/admin/companies`).pipe(
      map((response: CompanyResponse[]) =>
        response.map(c => ({
          id: c.id,
          name: c.name,
          active: c.active,
          createdAt: new Date(c.createdAt),
          updatedAt: new Date(c.updatedAt)
        }))
      ),
      tap(companies => {
        this.companies.set(companies);
        // Wenn keine Company ausgewählt, setze erste als Standard
        if (!this.selectedCompanyId() && companies.length > 0) {
          this.selectedCompanyId.set(companies[0].id);
          localStorage.setItem('selectedCompanyId', companies[0].id.toString());
        }
      }),
      catchError(error => {
        console.error('Error loading companies:', error);
        // Bei Fehler: Leeres Array zurückgeben (Empty State)
        this.companies.set([]);
        return of([]);
      })
    );
  }

  /**
   * Get company by ID
   * Backend: GET /api/v1/admin/companies/{id}
   */
  getCompany(id: number): Observable<Company | null> {
    return this.http.get<CompanyResponse>(`${environment.apiBaseUrl}/admin/companies/${id}`).pipe(
      map((response: CompanyResponse) => ({
        id: response.id,
        name: response.name,
        active: response.active,
        createdAt: new Date(response.createdAt),
        updatedAt: new Date(response.updatedAt)
      })),
      catchError(error => {
        console.error(`Error loading company ${id}:`, error);
        return of(null);
      })
    );
  }

  /**
   * Create new company (SYSTEM_ADMIN only)
   * Backend: POST /api/v1/admin/companies
   */
  createCompany(name: string): Observable<Company> {
    return this.http.post<CompanyResponse>(`${environment.apiBaseUrl}/admin/companies`, { name }).pipe(
      map((response: CompanyResponse) => {
        const newCompany: Company = {
          id: response.id,
          name: response.name,
          active: response.active,
          createdAt: new Date(response.createdAt),
          updatedAt: new Date(response.updatedAt)
        };
        // Aktualisiere lokale Liste
        this.companies.update(companies => [...companies, newCompany]);
        return newCompany;
      }),
      catchError(error => {
        console.error('Error creating company:', error);
        throw error;
      })
    );
  }

  /**
   * Get selected company ID
   */
  getSelectedCompanyId(): number | null {
    return this.selectedCompanyId();
  }

  /**
   * Set selected company
   */
  setSelectedCompany(companyId: number): void {
    // Validiere dass Company existiert
    const company = this.companies().find(c => c.id === companyId);
    if (company) {
      this.selectedCompanyId.set(companyId);
      localStorage.setItem('selectedCompanyId', companyId.toString());
    } else {
      console.warn(`Company ${companyId} not found in local cache. Loading from backend...`);
      // Lade Company vom Backend falls nicht im Cache
      this.getCompany(companyId).subscribe(company => {
        if (company) {
          this.selectedCompanyId.set(companyId);
          localStorage.setItem('selectedCompanyId', companyId.toString());
        }
      });
    }
  }

  /**
   * Get selected company
   */
  getSelectedCompany(): Observable<Company | null> {
    const id = this.selectedCompanyId();
    if (!id) {
      return of(null);
    }
    // Prüfe zuerst lokalen Cache
    const cachedCompany = this.companies().find(c => c.id === id);
    if (cachedCompany) {
      return of(cachedCompany);
    }
    // Sonst lade vom Backend
    return this.getCompany(id);
  }

  /**
   * Load selected company from localStorage
   */
  loadSelectedCompanyFromStorage(): void {
    const storedId = localStorage.getItem('selectedCompanyId');
    if (storedId) {
      const id = parseInt(storedId, 10);
      if (!isNaN(id)) {
        this.selectedCompanyId.set(id);
      }
    }
  }
}

