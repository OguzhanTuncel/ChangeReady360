import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models/survey.model';
import { StakeholderService } from '../../services/stakeholder.service';
import { StakeholderGroup, StakeholderKpis } from '../../models/stakeholder.model';

@Component({
  selector: 'app-stakeholder',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    FormsModule
  ],
  templateUrl: './stakeholder.component.html',
  styleUrl: './stakeholder.component.css'
})
export class StakeholderComponent implements OnInit {
  companies = signal<Company[]>([]);
  selectedCompany = signal<Company | null>(null);
  isLoading = signal(true);
  newCompanyName = ''; // Normal property for ngModel
  showAddForm = signal(false);
  
  stakeholderGroups = signal<StakeholderGroup[]>([]);
  stakeholderKpis = signal<StakeholderKpis | null>(null);

  constructor(
    private companyService: CompanyService,
    private stakeholderService: StakeholderService
  ) {}

  ngOnInit() {
    this.loadCompanies();
    this.loadSelectedCompany();
    this.loadStakeholderData();
  }

  loadStakeholderData() {
    this.isLoading.set(true);
    
    this.stakeholderService.getGroups().subscribe({
      next: (groups) => {
        this.stakeholderGroups.set(groups);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });

    this.stakeholderService.getKpis().subscribe({
      next: (kpis) => {
        this.stakeholderKpis.set(kpis);
      }
    });
  }

  loadCompanies() {
    this.isLoading.set(true);
    this.companyService.getCompanies().subscribe({
      next: (companies) => {
        this.companies.set(companies);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  loadSelectedCompany() {
    this.companyService.getSelectedCompany().subscribe({
      next: (company) => {
        this.selectedCompany.set(company);
      }
    });
  }

  selectCompany(company: Company) {
    this.companyService.setSelectedCompany(company.id);
    this.selectedCompany.set(company);
  }

  toggleAddForm() {
    this.showAddForm.update(show => !show);
    if (!this.showAddForm()) {
      this.newCompanyName = '';
    }
  }

  createCompany() {
    const name = this.newCompanyName.trim();
    if (!name) {
      return;
    }

    this.isLoading.set(true);
    this.companyService.createCompany(name).subscribe({
      next: (company) => {
        this.companies.update(companies => [...companies, company]);
        this.selectCompany(company);
        this.newCompanyName = '';
        this.showAddForm.set(false);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  cancelAdd() {
    this.newCompanyName = '';
    this.showAddForm.set(false);
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

  getImpactText(impact: string): string {
    return impact;
  }

  navigateToGroupDetail(groupId: string) {
    // TODO: Navigate to group detail page
    console.log('Navigate to group:', groupId);
  }
}

