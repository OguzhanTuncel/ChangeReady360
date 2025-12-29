export const environment = {
  production: false,
  // Development: Vollständige URL zum Backend
  apiBaseUrl: 'http://localhost:8080/api/v1',
  /**
   * Demo-/Wizard-Flow Feature-Flag.
   * Muss im produktiven Pfad deaktiviert sein, um Mock/Demo-Flows auszuschließen.
   */
  enableWizardFlow: false
};

