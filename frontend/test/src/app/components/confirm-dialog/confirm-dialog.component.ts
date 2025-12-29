import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p style="margin: 0; color: rgba(0,0,0,0.75)">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end" style="gap: 8px;">
      <button mat-button (click)="close(false)">{{ data.cancelText || 'Abbrechen' }}</button>
      <button mat-raised-button color="primary" (click)="close(true)">{{ data.confirmText || 'Bestätigen' }}</button>
    </mat-dialog-actions>
  `
})
export class ConfirmDialogComponent {
  private dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
  data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);

  close(result: boolean) {
    this.dialogRef.close(result);
  }
}


