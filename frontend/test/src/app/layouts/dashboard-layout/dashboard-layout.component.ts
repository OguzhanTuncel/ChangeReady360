import { Component, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';

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
    MatCardModule
  ],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.css'
})
export class DashboardLayoutComponent {
  isMobile = signal(false);
  sidenavOpened = signal(true);
  currentPageTitle = signal('Dashboard');

  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/app/dashboard', icon: 'dashboard' },
    { label: 'Umfragen', route: '/app/surveys', icon: 'assignment' },
    { label: 'Ergebnisse', route: '/app/results', icon: 'bar_chart' },
    { label: 'Einstellungen', route: '/app/settings', icon: 'settings' }
  ];

  constructor(
    private breakpointObserver: BreakpointObserver,
    private router: Router,
    public authService: AuthService
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
  }

  toggleSidenav(): void {
    this.sidenavOpened.update(opened => !opened);
  }

  logout(): void {
    this.authService.logout().subscribe();
  }

  private updatePageTitle(url: string): void {
    const item = this.navItems.find(nav => url.startsWith(nav.route));
    if (item) {
      this.currentPageTitle.set(item.label);
    } else if (url.includes('/wizard')) {
      this.currentPageTitle.set('Umfrage');
    } else {
      this.currentPageTitle.set('Dashboard');
    }
  }
}
