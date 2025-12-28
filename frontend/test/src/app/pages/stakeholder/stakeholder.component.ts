import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StakeholderService } from '../../services/stakeholder.service';
import { StakeholderGroup, StakeholderKpis, StakeholderGroupDetail, StakeholderPerson } from '../../models/stakeholder.model';
import { DonutChartComponent } from '../../components/donut-chart/donut-chart.component';

@Component({
  selector: 'app-stakeholder',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    DonutChartComponent
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
  selectedGroup = signal<StakeholderGroupDetail | null>(null);
  groupPersons = signal<StakeholderPerson[]>([]);
  showDetailView = signal(false);

  constructor(
    private companyService: CompanyService,
    private stakeholderService: StakeholderService
  ) {}

  ngOnInit() {
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

  addStakeholder() {
    // TODO: Implementieren - Dialog öffnen oder zu Formular navigieren
    console.log('Add stakeholder clicked');
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
    this.stakeholderService.getGroupDetail(groupId).subscribe({
      next: (detail) => {
        this.selectedGroup.set(detail);
        this.showDetailView.set(true);
        this.loadGroupPersons(groupId);
      }
    });
  }

  loadGroupPersons(groupId: string) {
    this.stakeholderService.getGroupPersons(groupId).subscribe({
      next: (persons) => {
        this.groupPersons.set(persons);
      }
    });
  }

  backToOverview() {
    this.showDetailView.set(false);
    this.selectedGroup.set(null);
    this.groupPersons.set([]);
  }
}

