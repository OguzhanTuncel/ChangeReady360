import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'wizard/mode',
    loadComponent: () => import('./pages/wizard-mode/wizard-mode.component').then(m => m.WizardModeComponent)
  },
  {
    path: 'wizard/context',
    loadComponent: () => import('./pages/wizard-context/wizard-context.component').then(m => m.WizardContextComponent)
  },
  {
    path: 'wizard/category/:index',
    loadComponent: () => import('./pages/wizard-category/wizard-category.component').then(m => m.WizardCategoryComponent)
  },
  {
    path: 'wizard/summary',
    loadComponent: () => import('./pages/wizard-summary/wizard-summary.component').then(m => m.WizardSummaryComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
