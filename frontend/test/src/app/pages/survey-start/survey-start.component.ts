import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { SurveyService } from '../../services/survey.service';
import { ParticipantType } from '../../models/survey.model';

@Component({
  selector: 'app-survey-start',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatRadioModule,
    MatFormFieldModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './survey-start.component.html',
  styleUrl: './survey-start.component.css'
})
export class SurveyStartComponent implements OnInit {
  templateId = signal<string>('');
  participantType = signal<ParticipantType | null>(null);
  isLoading = signal(false);
  error = signal<string | null>(null);

  constructor(
    private surveyService: SurveyService,
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
  }

  onParticipantTypeChange(type: ParticipantType) {
    this.participantType.set(type);
  }

  startSurvey() {
    if (!this.participantType()) {
      this.error.set('Bitte wählen Sie Ihre Rolle aus');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    this.surveyService.createInstance(this.templateId(), this.participantType()!).subscribe({
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
