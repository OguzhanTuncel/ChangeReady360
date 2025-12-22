# ChangeReady360 - Projektstruktur

## 📋 Überblick

Dieses Dokument beschreibt die aktuelle, stabilisierte Projektstruktur nach dem kontrollierten Neustart.

## 🏗️ Architektur

### Frontend (Angular 21)
- **Laufmodus**: Lokal mit `ng serve` (Hot Reload)
- **Port**: 4200
- **Backend-URL**: `http://localhost:8080/api/v1`

### Backend (Spring Boot)
- **Laufmodus**: Docker Container
- **Port**: 8080
- **Datenbank**: PostgreSQL (Port 5432)

## 📁 Frontend-Struktur

```
frontend/test/src/app/
├── app.ts                    # Root Component
├── app.config.ts             # App-Konfiguration (HTTP, Router, Interceptors)
├── app.routes.ts             # Routing-Konfiguration
│
├── services/                 # Business Logic
│   ├── auth.service.ts       # ✅ EINZIGE Auth-Implementierung
│   └── wizard-state.service.ts
│
├── guards/                   # Route Protection
│   └── auth.guard.ts         # ✅ EINZIGER Guard
│
├── interceptors/             # HTTP Interceptors
│   └── auth.interceptor.ts  # ✅ EINZIGER Interceptor (Token + 401/403 Handling)
│
├── models/                   # TypeScript Interfaces
│   ├── auth.model.ts         # Auth-Typen
│   └── wizard-data.model.ts
│
├── layouts/                  # Layout-Komponenten
│   └── dashboard-layout/    # ✅ EINZIGES Layout (Sidebar + Topbar)
│
├── pages/                    # Seiten-Komponenten
│   ├── login/                # Login-Seite (öffentlich)
│   ├── dashboard/            # Dashboard (geschützt)
│   ├── surveys/              # Umfragen (geschützt)
│   ├── results/              # Ergebnisse (geschützt)
│   ├── settings/             # Einstellungen (geschützt)
│   └── wizard-*/             # Wizard-Seiten (geschützt)
│
└── components/               # Wiederverwendbare Komponenten
    ├── option-card/
    ├── progress-indicator/
    ├── question-item/
    ├── score-preview/
    └── wizard-shell/
```

## 🔐 Authentifizierung

### Flow
1. **Login**: `/login` → `LoginComponent` → `AuthService.login()`
2. **Token**: Wird in `localStorage` gespeichert
3. **Interceptor**: Fügt automatisch `Authorization: Bearer <token>` hinzu
4. **Guard**: Prüft bei geschützten Routen (`/app/*`)
5. **Logout**: `AuthService.logout()` → löscht Token → Redirect zu `/login`

### Verantwortlichkeiten
- **AuthService**: Login, Logout, Token-Management, State
- **AuthGuard**: Route-Schutz
- **AuthInterceptor**: HTTP-Header, 401/403 Handling
- **LoginComponent**: UI für Login-Formular

## 🛣️ Routing

### Öffentliche Routen
- `/login` - Login-Seite

### Geschützte Routen (unter `/app`)
- `/app/dashboard` - Dashboard
- `/app/surveys` - Umfragen
- `/app/results` - Ergebnisse
- `/app/settings` - Einstellungen
- `/app/wizard/*` - Wizard-Flow

### Redirects
- `/` → `/login` (wenn nicht eingeloggt)
- `/` → `/app/dashboard` (wenn eingeloggt, via LoginComponent)
- `/**` → `/login` (404 Fallback)

## 🎨 Layout-Struktur

### Dashboard Layout
- **Sidebar**: Navigation (Dashboard, Umfragen, Ergebnisse, Einstellungen)
- **Topbar**: Seitentitel, User-Email, Logout-Button
- **Content**: `<router-outlet />` für Seiten-Inhalt

## 🔧 Backend-Struktur

```
backend/src/main/java/com/changeready/
├── config/
│   ├── CorsConfig.java           # CORS-Konfiguration
│   ├── SecurityConfig.java       # Spring Security
│   ├── InitialAdminSetup.java    # Admin-User Setup beim Start
│   └── OpenApiConfig.java        # Swagger/OpenAPI
│
├── controller/
│   ├── AuthController.java       # ✅ EINZIGER Auth-Controller
│   ├── AdminSetupController.java # Temporär (Setup-Hilfe)
│   └── ... (weitere Controller)
│
├── service/
│   ├── AuthService.java          # ✅ EINZIGER Auth-Service
│   └── AuthServiceImpl.java
│
└── security/
    ├── JwtAuthenticationFilter.java
    ├── JwtTokenProvider.java
    └── UserDetailsServiceImpl.java
```

## 🚀 Starten des Projekts

### 1. Backend & Datenbank starten
```bash
docker-compose up -d
```

### 2. Frontend starten
```bash
cd frontend/test
npm install  # nur beim ersten Mal
npm start
```

### 3. Zugriff
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## 🔑 Login-Daten

**Admin-User:**
- Email: `admin@changeready360.com`
- Password: `Admin123!`

## ✅ Basis-Flow (getestet)

1. ✅ Nutzer anlegen (via `/api/v1/admin-setup/create-user`)
2. ✅ Nutzer anmelden (via `/login`)
3. ✅ Nach Anmeldung geschützte Seiten sehen (`/app/dashboard`)
4. ✅ Abmelden (Logout-Button)

## 📝 Wichtige Hinweise

- **Keine Duplikate**: Jede Verantwortlichkeit hat genau EINE Implementierung
- **Klare Trennung**: Services → Guards → Interceptors → Components
- **Einheitliche Struktur**: Alle Seiten unter `/app`, Layout konsolidiert
- **CORS**: Konfiguriert für `localhost:4200`

## 🗑️ Entfernte/Redundante Teile

- ❌ Landing-Page entfernt (wird durch Login ersetzt)
- ❌ Legacy-Routen entfernt (alle unter `/app` konsolidiert)
