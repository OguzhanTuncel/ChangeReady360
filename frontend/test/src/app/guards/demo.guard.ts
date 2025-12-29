import { CanActivateFn } from '@angular/router';
import { environment } from '../../environments/environment';

/**
 * Guard to isolate Demo/Wizard flows from productive user flow.
 * Default: disabled (returns false).
 */
export const demoGuard: CanActivateFn = () => {
  return environment.enableWizardFlow === true;
};


