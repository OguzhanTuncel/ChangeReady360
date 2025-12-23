import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, UserInfo, AuthState } from '../models/auth.model';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authState = signal<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null
  });

  // Computed signals für einfachen Zugriff
  isAuthenticated = computed(() => this.authState().isAuthenticated);
  currentUser = computed(() => this.authState().user);
  token = computed(() => this.authState().token);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Beim App-Start Token wiederherstellen
    this.restoreAuth();
  }

  /**
   * Login mit Email und Passwort
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setAuth(response.token, response.userInfo);
        }),
        catchError(error => {
          console.error('Login error:', error);
          throw error;
        })
      );
  }

  /**
   * Logout
   */
  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/logout`, {})
      .pipe(
        tap(() => {
          this.clearAuth();
          this.router.navigate(['/login']);
        }),
        catchError(error => {
          // Auch bei Fehler lokal ausloggen
          this.clearAuth();
          this.router.navigate(['/login']);
          return of(void 0);
        })
      );
  }

  /**
   * Aktuellen User abrufen (falls Backend Endpoint vorhanden)
   */
  getCurrentUser(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${environment.apiBaseUrl}/auth/me`)
      .pipe(
        tap(user => {
          this.authState.update(state => ({
            ...state,
            user
          }));
          localStorage.setItem(USER_KEY, JSON.stringify(user));
        })
      );
  }

  /**
   * Token und User im State und localStorage speichern
   */
  private setAuth(token: string, user: UserInfo): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    
    this.authState.set({
      isAuthenticated: true,
      user,
      token
    });
  }

  /**
   * Auth-Daten löschen
   */
  private clearAuth(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    
    this.authState.set({
      isAuthenticated: false,
      user: null,
      token: null
    });
  }

  /**
   * Token und User aus localStorage wiederherstellen
   */
  private restoreAuth(): void {
    const token = localStorage.getItem(TOKEN_KEY);
    const userStr = localStorage.getItem(USER_KEY);

    if (token && userStr) {
      try {
        const user = JSON.parse(userStr) as UserInfo;
        this.authState.set({
          isAuthenticated: true,
          user,
          token
        });
      } catch (error) {
        console.error('Error restoring auth:', error);
        this.clearAuth();
      }
    }
  }

  /**
   * Prüft ob User eingeloggt ist
   */
  checkAuth(): boolean {
    return this.isAuthenticated();
  }
}

