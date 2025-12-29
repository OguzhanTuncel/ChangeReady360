import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm!: FormGroup; // Definitiv initialisiert im Constructor
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Wenn bereits eingeloggt, direkt zu Dashboard weiterleiten
    if (this.authService.checkAuth()) {
      this.router.navigate(['/app/dashboard']);
      return;
    }

    // FormGroup initialisieren
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.loginForm.value;

    this.authService.login({ email, password }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.redirectAfterLogin();
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Login error details:', error);
        
        let message = 'Login fehlgeschlagen. Bitte überprüfen Sie Ihre Anmeldedaten.';
        
        if (error.status === 0 || error.status === undefined) {
          message = 'Verbindung zum Backend fehlgeschlagen. Bitte stellen Sie sicher, dass der Backend-Server auf Port 8080 läuft.';
        } else if (error.status === 404) {
          message = 'Backend-Endpoint nicht gefunden. Bitte überprüfen Sie, ob das Backend läuft.';
        } else if (error.status === 401 || error.status === 403) {
          message = 'Ungültige E-Mail oder Passwort.';
        } else if (error.error?.message) {
          message = error.error.message;
        }
        
        this.errorMessage.set(message);
      }
    });
  }

  private redirectAfterLogin(): void {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/app/dashboard';
    this.router.navigate([returnUrl]);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }
}

