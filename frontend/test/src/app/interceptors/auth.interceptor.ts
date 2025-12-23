import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Token aus dem Service holen
  const token = authService.token();

  // Authorization Header hinzufügen, wenn Token vorhanden
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Request weiterleiten und Fehler behandeln
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Bei 401 (Unauthorized) oder 403 (Forbidden): ausloggen und zu Login weiterleiten
      if (error.status === 401 || error.status === 403) {
        authService.logout().subscribe();
      }
      
      return throwError(() => error);
    })
  );
};

