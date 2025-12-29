import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { StakeholderService } from '../../services/stakeholder.service';
import { DashboardService } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-stakeholder-create-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>Neue Stakeholder-Gruppe erstellen</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="stakeholder-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" placeholder="z.B. IT-Abteilung" required>
          @if (form.get('name')?.hasError('required') && form.get('name')?.touched) {
            <mat-error>Name ist erforderlich</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Betroffenheit</mat-label>
          <mat-select formControlName="impact" required>
            <mat-option value="NIEDRIG">Niedrig</mat-option>
            <mat-option value="MITTEL">Mittel</mat-option>
            <mat-option value="HOCH">Hoch</mat-option>
            <mat-option value="SEHR_HOCH">Sehr hoch</mat-option>
            <mat-option value="STRATEGISCH">Strategisch</mat-option>
          </mat-select>
          @if (form.get('impact')?.hasError('required') && form.get('impact')?.touched) {
            <mat-error>Betroffenheit ist erforderlich</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Beschreibung (optional)</mat-label>
          <textarea matInput formControlName="description" rows="3" placeholder="Beschreibung der Stakeholder-Gruppe"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-raised-button class="btn-cancel" (click)="onCancel()">Abbrechen</button>
      <button mat-raised-button class="btn-create" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting()">
        @if (isSubmitting()) {
          <mat-icon>hourglass_empty</mat-icon>
          <span>Wird erstellt...</span>
        } @else {
          <mat-icon>add</mat-icon>
          <span>Erstellen</span>
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .stakeholder-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 400px;
      padding: 16px 0;
    }
    .full-width {
      width: 100%;
    }
    mat-dialog-content {
      max-height: 600px;
      background: #ffffff;
    }
    /* Fix: Inputs sollen nicht "durchsichtig" sein */
    :host ::ng-deep .mat-mdc-text-field-wrapper {
      background: #ffffff !important;
      opacity: 1 !important;
    }
    :host ::ng-deep .mdc-text-field--outlined {
      background: #ffffff !important;
      opacity: 1 !important;
    }
    :host ::ng-deep input.mat-mdc-input-element,
    :host ::ng-deep textarea.mat-mdc-input-element {
      color: rgba(0,0,0,0.87);
      background: #ffffff !important;
      opacity: 1 !important;
    }
    :host ::ng-deep .mat-mdc-form-field {
      opacity: 1 !important;
    }
    :host ::ng-deep .mat-mdc-select-panel {
      background-color: #ffffff !important;
      opacity: 1 !important;
      box-shadow: 0 12px 24px rgba(0,0,0,0.18);
    }
    mat-dialog-actions {
      gap: 10px;
    }
    .btn-cancel {
      background-color: #d32f2f;
      color: #fff;
      border-radius: 999px;
      padding: 0.7rem 1.25rem;
    }
    .btn-cancel:hover {
      background-color: #b71c1c;
    }
    .btn-create {
      background-color: #56A080;
      color: #fff;
      border-radius: 999px;
      padding: 0.7rem 1.25rem;
    }
    .btn-create:hover:not(:disabled) {
      background-color: #4a8d6f;
    }
  `]
})
export class StakeholderCreateDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<StakeholderCreateDialogComponent>);
  private stakeholderService = inject(StakeholderService);
  private dashboardService = inject(DashboardService);
  private authService = inject(AuthService);

  form: FormGroup;
  isSubmitting = signal(false);

  constructor() {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      impact: ['MITTEL', [Validators.required]],
      description: ['']
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const formValue = this.form.value;

    this.stakeholderService.createGroup({
      name: formValue.name,
      impact: formValue.impact,
      description: formValue.description || undefined
    }).subscribe({
      next: () => {
        // Dashboard-Daten neu laden nach erfolgreichem Create
        this.dashboardService.getDashboardData().subscribe();
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Error creating stakeholder group:', error);
        this.isSubmitting.set(false);
        
        let errorMessage = 'Fehler beim Erstellen der Stakeholder-Gruppe.';
        if (error.status === 403) {
          errorMessage = 'Sie haben keine Berechtigung für diese Aktion. Bitte stellen Sie sicher, dass Sie eingeloggt sind.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        alert(errorMessage);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

