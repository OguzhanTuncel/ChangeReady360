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
      // Nur bei 401: Session ist ungültig/abgelaufen -> lokal ausloggen.
      // 403 bedeutet: eingeloggt, aber keine Berechtigung. NICHT automatisch ausloggen (sonst wirkt es wie "nicht eingeloggt").
      if (error.status === 401) {
        authService.logout().subscribe();
      }
      
      return throwError(() => error);
    })
  );
};

