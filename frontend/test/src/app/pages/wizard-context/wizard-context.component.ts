import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WizardShellComponent } from '../../components/wizard-shell/wizard-shell.component';
import { OptionCardComponent } from '../../components/option-card/option-card.component';
import { WizardStateService } from '../../services/wizard-state.service';
import { CONTEXT_TYPES, ContextType } from '../../models/wizard-data.model';

@Component({
  selector: 'app-wizard-context',
  standalone: true,
  imports: [CommonModule, WizardShellComponent, OptionCardComponent],
  templateUrl: './wizard-context.component.html',
  styleUrl: './wizard-context.component.css'
})
export class WizardContextComponent implements OnInit {
  contextTypes = CONTEXT_TYPES;
  selectedContext: ContextType | null = null;

  constructor(
    private router: Router,
    private wizardState: WizardStateService
  ) {}

  ngOnInit() {
    const state = this.wizardState.getState();
    this.selectedContext = state().contextType || null;
  }

  onContextSelected(contextId: string) {
    this.selectedContext = contextId as ContextType;
    this.wizardState.setContextType(this.selectedContext);
  }

  onBack() {
    this.router.navigate(['/wizard/mode']);
  }

  onNext() {
    if (this.selectedContext) {
      this.wizardState.setCurrentCategoryIndex(0);
      this.router.navigate(['/wizard/category', 0]);
    }
  }
}

