# Backend-Struktur: Vollständig manuelles Onboarding-System

## Übersicht

Das Backend verwendet ein vollständig manuelles Onboarding-System ohne automatische Prozesse, Invite-Token-Flows oder erzwungene Passwortwechsel. Alle User werden manuell angelegt und können sich danach sofort einloggen.

## Wichtige Prinzipien

### 1. Keine automatische Company-Erstellung

**Regel:**
- Companies können **nur noch manuell** über `POST /api/v1/admin/companies` erstellt werden.
- Keine automatische Company-Erstellung mehr möglich, nirgendwo im System.

**Betroffene Dateien:**
- `InviteService.java` - `findOrCreateCompany()` deaktiviert (falls noch vorhanden)

### 2. Invite-System vollständig entfernt

**Status:**
- Alle Invite-Endpoints wurden entfernt (`InviteController` gelöscht)
- Invite-System ist nicht mehr verfügbar
- Keine Token-basierte Registrierung mehr möglich
- Es gibt weiterhin keine Self-Registration

**Entfernte Endpoints:**
- `POST /api/v1/invites/company-admin` - Entfernt
- `POST /api/v1/invites/company-user` - Entfernt
- `GET /api/v1/invites` - Entfernt
- `GET /api/v1/invites/my-company` - Entfernt
- `GET /api/v1/invites/validate/{token}` - Entfernt
- `POST /api/v1/invites/accept` - Entfernt

### 3. Manuelles User-Onboarding

**Flow:**
1. SYSTEM_ADMIN erstellt Company: `POST /api/v1/admin/companies`
2. SYSTEM_ADMIN erstellt COMPANY_ADMIN: `POST /api/v1/admin/users/company-admin?companyId={id}`
3. COMPANY_ADMIN erstellt COMPANY_USER: `POST /api/v1/admin/users`
4. User kann sich sofort mit Initial-Passwort einloggen
5. Keine Passwortwechsel-Logik, keine Sonderbehandlung

**Neue Endpoints:**

#### `POST /api/v1/admin/users/company-admin?companyId={id}`
- **Zugriff:** Nur SYSTEM_ADMIN
- **Rolle:** Wird automatisch auf `COMPANY_ADMIN` gesetzt (unabhängig vom Request)
- **Request Body:**
  ```json
  {
    "email": "admin@company.com",
    "password": "InitialPassword123",
    "role": "COMPANY_ADMIN",  // Wird ignoriert, immer COMPANY_ADMIN
    "active": true
  }
  ```
- **Response:**
  ```json
  {
    "id": 1,
    "email": "admin@company.com",
    "role": "COMPANY_ADMIN",
    "companyId": 1,
    "active": true,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
  ```
- **Wichtig:** 
  - Passwort wird gehasht gespeichert
  - User kann sich sofort einloggen
  - Keine Passwortwechsel-Logik
  - **Passwort wird NICHT im Response zurückgegeben** (Sicherheit)

#### `POST /api/v1/admin/users`
- **Zugriff:** Nur COMPANY_ADMIN
- **Rolle:** Wird automatisch auf `COMPANY_USER` gesetzt (unabhängig vom Request)
- **Request Body:**
  ```json
  {
    "email": "user@company.com",
    "password": "UserPassword123",
    "role": "COMPANY_USER",  // Wird ignoriert, immer COMPANY_USER
    "active": true
  }
  ```
- **Response:**
  ```json
  {
    "id": 2,
    "email": "user@company.com",
    "role": "COMPANY_USER",
    "companyId": 1,
    "active": true,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
  ```
- **Wichtig:**
  - COMPANY_ADMIN kann nur Users in der eigenen Company erstellen
  - Rolle wird immer auf COMPANY_USER gesetzt
  - **Passwort wird NICHT im Response zurückgegeben** (Sicherheit)

### 4. Standard-Login ohne Sonderlogik

**Verhalten:**
- Login funktioniert standardmäßig: korrekte Credentials → Token, falsche → 401
- Keine Passwortwechsel-Logik mehr
- Keine `passwordChangeRequired` Checks
- User können sich sofort nach Erstellung einloggen

**Login-Endpoint:**
- `POST /api/v1/auth/login`
- Standard-Authentifizierung ohne Sonderbehandlung

**Betroffene Dateien:**
- `AuthServiceImpl.java` - Standard-Login ohne Passwortwechsel-Logik
- Keine `PasswordChangeRequiredException` mehr
- Kein `/change-password` Endpoint mehr

### 5. Rollenerzwingung

**Regeln:**
- `POST /api/v1/admin/users/company-admin` → Erzwingt immer `COMPANY_ADMIN`
- `POST /api/v1/admin/users` (COMPANY_ADMIN) → Erzwingt immer `COMPANY_USER`
- Rollen werden nicht aus dem Request übernommen, sondern erzwungen

**Betroffene Dateien:**
- `UserServiceImpl.java` - Rollen werden erzwungen, nicht aus Request übernommen

### 6. Zugriffskontrolle / Rollenregeln

**Unverändert:**
- Nur SYSTEM_ADMIN darf Companies anlegen (`POST /api/v1/admin/companies`)
- Nur SYSTEM_ADMIN darf COMPANY_ADMIN erstellen (`POST /api/v1/admin/users/company-admin`)
- COMPANY_ADMIN kann nur COMPANY_USER in der eigenen Company erstellen (`POST /api/v1/admin/users`)
- COMPANY_USER kann sich nur einloggen und Fragebögen ausfüllen
- Multi-Tenancy bleibt strikt (keine Daten über Unternehmensgrenzen)

### 7. Zugangsanfragen (company_access_requests)

**Unverändert:**
- Anfragen können weiterhin gestellt werden (`POST /api/v1/company-access-requests`)
- SYSTEM_ADMIN kann Anfragen ansehen und bearbeiten
- **Wichtig:** "Approved" erzeugt **keine** automatischen Accounts
- Workflow: Anfrage anschauen → intern entscheiden → Company manuell erstellen → Admin manuell erstellen → externe Mail senden

### 8. Sicherheit

**Wichtig:**
- **Passwörter werden niemals in API-Responses zurückgegeben**
- Passwörter werden sicher gehasht gespeichert (BCrypt)
- Initial-Passwörter müssen extern übermittelt werden (außerhalb des Systems)
- Keine Self-Registration möglich

## API-Endpunkte Übersicht

### Öffentliche Endpoints (keine Authentifizierung)
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/logout` - Logout
- `POST /api/v1/company-access-requests` - Zugangsanfrage stellen

### SYSTEM_ADMIN Endpoints
- `POST /api/v1/admin/companies` - Company erstellen
- `GET /api/v1/admin/companies` - Alle Companies anzeigen
- `GET /api/v1/admin/companies/{id}` - Company anzeigen
- `PUT /api/v1/admin/companies/{id}` - Company aktualisieren
- `POST /api/v1/admin/users/company-admin?companyId={id}` - COMPANY_ADMIN erstellen
- `GET /api/v1/company-access-requests` - Zugangsanfragen anzeigen
- `PUT /api/v1/company-access-requests/{id}` - Zugangsanfrage bearbeiten

### COMPANY_ADMIN Endpoints
- `POST /api/v1/admin/users` - COMPANY_USER erstellen
- `GET /api/v1/admin/users` - Alle Users der eigenen Company anzeigen
- `GET /api/v1/admin/users/{id}` - User anzeigen
- `PUT /api/v1/admin/users/{id}` - User aktualisieren

### Entfernte Endpoints
- Alle `/api/v1/invites/**` Endpoints - Entfernt
- `POST /api/v1/auth/change-password` - Entfernt

## Beispiel-Workflow: Neues Unternehmen onboarden

### Schritt 1: Zugangsanfrage (optional)
```bash
POST /api/v1/company-access-requests
{
  "companyName": "Beispiel GmbH",
  "contactName": "Max Mustermann",
  "contactEmail": "max@beispiel.de",
  "contactPhone": "+49 123 456789",
  "message": "Wir möchten ChangeReady360 nutzen"
}
```

### Schritt 2: SYSTEM_ADMIN prüft Anfrage
```bash
GET /api/v1/company-access-requests/{id}
# SYSTEM_ADMIN entscheidet intern
PUT /api/v1/company-access-requests/{id}
{
  "status": "APPROVED"
}
```

### Schritt 3: SYSTEM_ADMIN erstellt Company
```bash
POST /api/v1/admin/companies
{
  "name": "Beispiel GmbH",
  "active": true
}
# Response: { "id": 1, "name": "Beispiel GmbH", ... }
```

### Schritt 4: SYSTEM_ADMIN erstellt COMPANY_ADMIN
```bash
POST /api/v1/admin/users/company-admin?companyId=1
{
  "email": "admin@beispiel.de",
  "password": "InitialPassword123",
  "role": "COMPANY_ADMIN",  // Wird ignoriert
  "active": true
}
# Response: { "id": 1, "email": "admin@beispiel.de", "role": "COMPANY_ADMIN", ... }
# WICHTIG: Passwort wird NICHT zurückgegeben
```

### Schritt 5: SYSTEM_ADMIN sendet Zugangsdaten extern
- Email außerhalb des Systems senden mit:
  - Email: `admin@beispiel.de`
  - Initial-Passwort: `InitialPassword123`
  - Hinweis: Passwort kann beim ersten Login verwendet werden

### Schritt 6: COMPANY_ADMIN kann sich sofort einloggen
```bash
POST /api/v1/auth/login
{
  "email": "admin@beispiel.de",
  "password": "InitialPassword123"
}
# Login erfolgreich, Token erhalten
```

### Schritt 7: COMPANY_ADMIN erstellt COMPANY_USER
```bash
POST /api/v1/admin/users
{
  "email": "user@beispiel.de",
  "password": "UserPassword123",
  "role": "COMPANY_USER",  // Wird ignoriert, immer COMPANY_USER
  "active": true
}
# Response: { "id": 2, "email": "user@beispiel.de", "role": "COMPANY_USER", ... }
# WICHTIG: Passwort wird NICHT zurückgegeben
```

### Schritt 8: COMPANY_USER kann sich sofort einloggen
```bash
POST /api/v1/auth/login
{
  "email": "user@beispiel.de",
  "password": "UserPassword123"
}
# Login erfolgreich
```

## Entfernte Komponenten

### Entfernte Dateien
- `InviteController.java` - Komplett entfernt
- `PasswordChangeRequest.java` - Entfernt
- `PasswordChangeRequiredException.java` - Entfernt
- `migration/add_password_change_required.sql` - Entfernt

### Entfernte Felder
- `User.passwordChangeRequired` - Entfernt
- `UserResponse.passwordChangeRequired` - Entfernt

### Entfernte Methoden
- `AuthService.changePassword()` - Entfernt
- `AuthServiceImpl.changePassword()` - Entfernt
- `InviteService.createCompanyAdminInvite()` - Deaktiviert (wirft Exception)
- `InviteService.findOrCreateCompany()` - Deaktiviert (wirft Exception)

### Entfernte Endpoints
- `POST /api/v1/auth/change-password` - Entfernt
- Alle `/api/v1/invites/**` Endpoints - Entfernt

## Technische Details

### User Entity
```java
@Entity
public class User {
    // ... bestehende Felder ...
    // KEIN passwordChangeRequired Feld mehr
}
```

### Login-Logik
```java
// Standard-Login ohne Sonderlogik
Authentication authentication = authenticationManager.authenticate(...);
String token = tokenProvider.generateToken(authentication);
return new LoginResponse(token, "Bearer", userInfo);
```

### Rollenerzwingung
```java
// COMPANY_ADMIN Endpoint
Role enforcedRole = Role.COMPANY_USER; // Immer COMPANY_USER

// SYSTEM_ADMIN Endpoint für COMPANY_ADMIN
Role enforcedRole = Role.COMPANY_ADMIN; // Immer COMPANY_ADMIN
```

## Sicherheitsaspekte

1. **Keine automatische Company-Erstellung:** Verhindert ungewollte Company-Erstellung
2. **Rollen werden erzwungen:** Nicht aus Request übernommen
3. **Passwörter werden nicht zurückgegeben:** API-Responses enthalten keine Passwörter
4. **Manueller Prozess:** Alle kritischen Schritte erfordern manuelle Entscheidung
5. **Multi-Tenancy:** Strikte Isolation zwischen Companies bleibt erhalten
6. **Rollenbasierte Zugriffskontrolle:** SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER haben klare Berechtigungen

## Zusammenfassung

- ✅ Keine automatische Company-Erstellung mehr
- ✅ Invite-System vollständig entfernt
- ✅ Manueller Onboarding-Flow implementiert
- ✅ Keine Passwortwechsel-Logik mehr
- ✅ User können sich sofort nach Erstellung einloggen
- ✅ Rollen werden erzwungen, nicht aus Request übernommen
- ✅ Zugriffskontrolle und Multi-Tenancy bleiben intakt
- ✅ Zugangsanfragen bleiben manuell (keine Automatisierung)
- ✅ API bereinigt (Invite-Endpoints entfernt)
- ✅ Passwörter werden niemals in Responses zurückgegeben

Das System ist jetzt vollständig auf manuelles Onboarding umgestellt, ohne Invite-Token-Flows und ohne erzwungene Passwortwechsel-Logik.
