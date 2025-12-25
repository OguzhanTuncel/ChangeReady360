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

  constructor(private companyService: CompanyService) {}

  ngOnInit() {
    this.loadCompanies();
    this.loadSelectedCompany();
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
}

