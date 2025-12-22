import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  template: `
    <div class="settings-page">
      <h1>Einstellungen</h1>
      <mat-card>
        <mat-card-content>
          <h3>Benutzerinformationen</h3>
          <p><strong>E-Mail:</strong> {{ authService.currentUser()?.email }}</p>
          <p><strong>Rolle:</strong> {{ authService.currentUser()?.role }}</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .settings-page {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-lg);
    }
  `]
})
export class SettingsComponent {
  constructor(public authService: AuthService) {}
}
