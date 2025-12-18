import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WizardShellComponent } from '../../components/wizard-shell/wizard-shell.component';
import { OptionCardComponent } from '../../components/option-card/option-card.component';
import { WizardStateService } from '../../services/wizard-state.service';
import { ANALYSIS_MODES, AnalysisMode } from '../../models/wizard-data.model';

@Component({
  selector: 'app-wizard-mode',
  standalone: true,
  imports: [CommonModule, WizardShellComponent, OptionCardComponent],
  templateUrl: './wizard-mode.component.html',
  styleUrl: './wizard-mode.component.css'
})
export class WizardModeComponent implements OnInit {
  analysisModes = ANALYSIS_MODES;
  selectedMode: AnalysisMode | null = null;

  constructor(
    private router: Router,
    private wizardState: WizardStateService
  ) {}

  ngOnInit() {
    const state = this.wizardState.getState();
    this.selectedMode = state().analysisMode || null;
  }

  onModeSelected(modeId: string) {
    this.selectedMode = modeId as AnalysisMode;
    this.wizardState.setAnalysisMode(this.selectedMode);
  }

  onBack() {
    this.router.navigate(['/']);
  }

  onNext() {
    if (this.selectedMode) {
      this.router.navigate(['/wizard/context']);
    }
  }
}

