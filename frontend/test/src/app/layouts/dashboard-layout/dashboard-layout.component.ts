import { Component, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models/survey.model';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatBadgeModule,
    MatMenuModule,
    MatInputModule
  ],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.css'
})
export class DashboardLayoutComponent {
  isMobile = signal(false);
  sidenavOpened = signal(true);
  currentPageTitle = signal('Dashboard');
  companies = signal<Company[]>([]);
  selectedCompany = signal<Company | null>(null);
  notificationCount = signal(2); // TODO: Aus Backend laden

  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/app/dashboard', icon: 'dashboard' },
    { label: 'Stakeholder', route: '/app/stakeholder', icon: 'people' },
    { label: 'Assessment', route: '/app/surveys', icon: 'assignment' },
    { label: 'Reporting', route: '/app/results', icon: 'bar_chart' }
  ];

  constructor(
    private breakpointObserver: BreakpointObserver,
    private router: Router,
    public authService: AuthService,
    private companyService: CompanyService
  ) {
    // Responsive Sidebar
    this.breakpointObserver.observe([Breakpoints.Handset])
      .subscribe(result => {
        this.isMobile.set(result.matches);
        this.sidenavOpened.set(!result.matches);
      });

    // Page Title basierend auf Route aktualisieren
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updatePageTitle(event.url);
      });

    // Initial title setzen
    this.updatePageTitle(this.router.url);

    // Load companies and selected company
    this.loadCompanies();
    this.loadSelectedCompany();
  }

  loadCompanies() {
    this.companyService.getCompanies().subscribe({
      next: (companies) => {
        this.companies.set(companies);
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

  onCompanyChange(companyId: number) {
    this.companyService.setSelectedCompany(companyId);
    this.loadSelectedCompany();
    // Reload companies to update list
    this.loadCompanies();
  }

  toggleSidenav(): void {
    this.sidenavOpened.update(opened => !opened);
  }

  logout(): void {
    this.authService.logout().subscribe();
  }

  getUserInitials(): string {
    const user = this.authService.currentUser();
    if (!user?.email) return 'U';
    const name = user.email.split('@')[0];
    const parts = name.split('.');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  getUserName(): string {
    const user = this.authService.currentUser();
    if (!user?.email) return 'Benutzer';
    const name = user.email.split('@')[0];
    const parts = name.split('.');
    if (parts.length >= 2) {
      return parts.map(p => p.charAt(0).toUpperCase() + p.slice(1)).join(' ');
    }
    return name.charAt(0).toUpperCase() + name.slice(1);
  }

  getUserRole(): string {
    const user = this.authService.currentUser();
    // TODO: Role aus User-Objekt holen wenn verfügbar
    return 'Projektleiter';
  }

  refreshData(): void {
    // TODO: Daten aktualisieren
    this.loadCompanies();
    this.loadSelectedCompany();
  }

  exportReport(): void {
    // TODO: Report exportieren
    console.log('Export report');
  }

  private updatePageTitle(url: string): void {
    const item = this.navItems.find(nav => url.startsWith(nav.route));
    if (item) {
      this.currentPageTitle.set(item.label);
    } else if (url.includes('/wizard')) {
      this.currentPageTitle.set('Umfrage');
    } else if (url.includes('/survey')) {
      this.currentPageTitle.set('Umfrage');
    } else {
      this.currentPageTitle.set('Dashboard');
    }
  }
}

