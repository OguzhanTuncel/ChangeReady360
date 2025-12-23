import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';
import { SurveyService } from '../../services/survey.service';
import { SurveyInstance } from '../../models/survey.model';

@Component({
  selector: 'app-survey-success',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, DatePipe],
  templateUrl: './survey-success.component.html',
  styleUrl: './survey-success.component.css'
})
export class SurveySuccessComponent implements OnInit {
  instance = signal<SurveyInstance | null>(null);
  isLoading = signal(true);

  constructor(
    private surveyService: SurveyService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    const instanceId = this.route.snapshot.paramMap.get('id');
    if (!instanceId) {
      this.isLoading.set(false);
      return;
    }

    this.surveyService.getInstance(instanceId).subscribe({
      next: (instance) => {
        this.instance.set(instance);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  goToSurveys() {
    this.router.navigate(['/app/surveys']);
  }
}

