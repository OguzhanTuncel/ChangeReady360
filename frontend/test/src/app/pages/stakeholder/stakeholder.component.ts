import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { StakeholderService } from '../../services/stakeholder.service';
import { DashboardService } from '../../services/dashboard.service';
import { StakeholderGroup, StakeholderKpis, StakeholderGroupDetail, StakeholderPerson } from '../../models/stakeholder.model';
import { DonutChartComponent } from '../../components/donut-chart/donut-chart.component';
import { StakeholderCreateDialogComponent } from '../../components/stakeholder-create-dialog/stakeholder-create-dialog.component';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-stakeholder',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DonutChartComponent
  ],
  templateUrl: './stakeholder.component.html',
  styleUrl: './stakeholder.component.css'
})
export class StakeholderComponent implements OnInit {
  isLoading = signal(true);
  
  stakeholderGroups = signal<StakeholderGroup[]>([]);
  stakeholderKpis = signal<StakeholderKpis | null>(null);
  selectedGroup = signal<StakeholderGroupDetail | null>(null);
  groupPersons = signal<StakeholderPerson[]>([]);
  showDetailView = signal(false);

  constructor(
    private stakeholderService: StakeholderService,
    private dashboardService: DashboardService,
    private dialog: MatDialog
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
    const dialogRef = this.dialog.open(StakeholderCreateDialogComponent, {
      width: '500px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Reload stakeholder data after successful creation
        this.loadStakeholderData();
      }
    });
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

  deleteGroup(group: StakeholderGroup, event?: MouseEvent) {
    event?.stopPropagation();

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      disableClose: true,
      data: {
        title: 'Stakeholder-Gruppe löschen?',
        message: `Willst du Gruppe "${group.name}" wirklich löschen?`,
        confirmText: 'Löschen',
        cancelText: 'Abbrechen'
      }
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.isLoading.set(true);
      this.stakeholderService.deleteGroup(group.id).subscribe({
        next: () => {
          // Daten sofort neu laden (Stakeholder + Dashboard KPIs)
          this.loadStakeholderData();
          this.dashboardService.getDashboardData().subscribe();
        },
        error: () => {
          this.isLoading.set(false);
        }
      });
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

