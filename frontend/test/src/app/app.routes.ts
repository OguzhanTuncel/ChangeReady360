import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { demoGuard } from './guards/demo.guard';

export const routes: Routes = [
  // Öffentliche Routen
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  // Root-Route: Redirect zu Login (wird dann zu Dashboard weitergeleitet wenn eingeloggt)
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  // Geschützte Routen unter /app
  {
    path: 'app',
    loadComponent: () => import('./layouts/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'surveys',
        loadComponent: () => import('./pages/surveys/surveys.component').then(m => m.SurveysComponent)
      },
      {
        path: 'survey/:id/start',
        loadComponent: () => import('./pages/survey-start/survey-start.component').then(m => m.SurveyStartComponent)
      },
      {
        path: 'survey/:id/fill',
        loadComponent: () => import('./pages/survey-fill/survey-fill.component').then(m => m.SurveyFillComponent)
      },
      {
        path: 'survey/:id/review',
        loadComponent: () => import('./pages/survey-review/survey-review.component').then(m => m.SurveyReviewComponent)
      },
      {
        path: 'survey/:id/success',
        loadComponent: () => import('./pages/survey-success/survey-success.component').then(m => m.SurveySuccessComponent)
      },
      {
        path: 'survey/:id/summary',
        loadComponent: () => import('./pages/survey-summary/survey-summary.component').then(m => m.SurveySummaryComponent)
      },
      {
        path: 'results',
        loadComponent: () => import('./pages/results/results.component').then(m => m.ResultsComponent)
      },
      {
        path: 'stakeholder',
        loadComponent: () => import('./pages/stakeholder/stakeholder.component').then(m => m.StakeholderComponent)
      },
      {
        path: 'settings',
        loadComponent: () => import('./pages/settings/settings.component').then(m => m.SettingsComponent)
      },
      // Wizard-Routen
      {
        path: 'wizard/mode',
        canActivate: [demoGuard],
        loadComponent: () => import('./pages/wizard-mode/wizard-mode.component').then(m => m.WizardModeComponent)
      },
      {
        path: 'wizard/context',
        canActivate: [demoGuard],
        loadComponent: () => import('./pages/wizard-context/wizard-context.component').then(m => m.WizardContextComponent)
      },
      {
        path: 'wizard/category/:index',
        canActivate: [demoGuard],
        loadComponent: () => import('./pages/wizard-category/wizard-category.component').then(m => m.WizardCategoryComponent)
      },
      {
        path: 'wizard/summary',
        canActivate: [demoGuard],
        loadComponent: () => import('./pages/wizard-summary/wizard-summary.component').then(m => m.WizardSummaryComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  // Fallback: Nicht gefundene Routen → Login (wird dann zu Dashboard weitergeleitet wenn eingeloggt)
  {
    path: '**',
    redirectTo: '/login'
  }
];
