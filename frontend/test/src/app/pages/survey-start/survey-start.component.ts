import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { SurveyService } from '../../services/survey.service';
import { ParticipantType, Department, DEPARTMENT_DISPLAY_NAMES } from '../../models/survey.model';
import { CompanyService } from '../../services/company.service';

@Component({
  selector: 'app-survey-start',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatRadioModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './survey-start.component.html',
  styleUrl: './survey-start.component.css'
})
export class SurveyStartComponent implements OnInit {
  templateId = signal<string>('');
  department = signal<Department | null>(null);
  participantType = signal<ParticipantType | null>(null);
  isLoading = signal(false);
  error = signal<string | null>(null);

  readonly departments: Department[] = ['EINKAUF', 'VERTRIEB', 'LAGER_LOGISTIK', 'IT', 'GESCHAEFTSFUEHRUNG'];
  readonly departmentDisplayNames = DEPARTMENT_DISPLAY_NAMES;

  constructor(
    private surveyService: SurveyService,
    private companyService: CompanyService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.templateId.set(id);
    } else {
      this.error.set('Keine Vorlage angegeben');
    }

    // Check if company is selected
    if (!this.companyService.getSelectedCompanyId()) {
      this.error.set('Bitte wählen Sie zuerst ein Unternehmen aus');
    }
  }

  onDepartmentChange(department: Department) {
    this.department.set(department);
    this.error.set(null);
  }

  onParticipantTypeChange(type: ParticipantType) {
    this.participantType.set(type);
    this.error.set(null);
  }

  startSurvey() {
    if (!this.department()) {
      this.error.set('Bitte wählen Sie Ihre Abteilung aus');
      return;
    }

    if (!this.participantType()) {
      this.error.set('Bitte wählen Sie Ihre Rolle aus');
      return;
    }

    if (!this.companyService.getSelectedCompanyId()) {
      this.error.set('Bitte wählen Sie zuerst ein Unternehmen aus');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    this.surveyService.createInstance(
      this.templateId(), 
      this.participantType()!, 
      this.department()!
    ).subscribe({
      next: (instance) => {
        this.router.navigate(['/app/survey', instance.id, 'fill']);
      },
      error: (err) => {
        this.error.set('Fehler beim Starten des Fragebogens');
        this.isLoading.set(false);
      }
    });
  }
}

