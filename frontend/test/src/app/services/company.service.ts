import { Injectable, signal } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { Company } from '../models/survey.model';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  // Mock Storage (später durch Backend-API ersetzen)
  private companies = signal<Company[]>([]);
  private selectedCompanyId = signal<number | null>(null);

  constructor() {
    // Mock-Daten: Initiale Companies
    this.companies.set([
      {
        id: 1,
        name: 'Beispiel GmbH',
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ]);
    // Gespeicherte Company aus localStorage laden, sonst erste Company als Standard
    this.loadSelectedCompanyFromStorage();
    if (!this.selectedCompanyId()) {
      this.selectedCompanyId.set(1);
    }
  }

  /**
   * Get all companies
   */
  getCompanies(): Observable<Company[]> {
    return of(this.companies()).pipe(delay(200));
  }

  /**
   * Get company by ID
   */
  getCompany(id: number): Observable<Company | null> {
    const company = this.companies().find(c => c.id === id);
    return of(company || null).pipe(delay(200));
  }

  /**
   * Create new company
   */
  createCompany(name: string): Observable<Company> {
    const newCompany: Company = {
      id: Date.now(), // Mock ID
      name,
      active: true,
      createdAt: new Date(),
      updatedAt: new Date()
    };
    this.companies.update(companies => [...companies, newCompany]);
    return of(newCompany).pipe(delay(300));
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
    const company = this.companies().find(c => c.id === companyId);
    if (company) {
      this.selectedCompanyId.set(companyId);
      // Store in localStorage for persistence
      localStorage.setItem('selectedCompanyId', companyId.toString());
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
    return this.getCompany(id);
  }

  /**
   * Load selected company from localStorage
   */
  loadSelectedCompanyFromStorage(): void {
    const storedId = localStorage.getItem('selectedCompanyId');
    if (storedId) {
      const id = parseInt(storedId, 10);
      const company = this.companies().find(c => c.id === id);
      if (company) {
        this.selectedCompanyId.set(id);
      }
    }
  }
}

