 Änderungen: Manuelles Onboarding

 Was wurde entfernt:

-  Invite-System komplett entfernt (keine Token-Links mehr)
-  Automatische Company-Erstellung entfernt
-  Passwortwechsel-Logik entfernt (kein "Passwort ändern erforderlich")
-  Alle `/api/v1/invites/**` Endpoints entfernt

 Was gilt jetzt:

 Company-Erstellung
- Nur SYSTEM_ADMIN kann Companies manuell anlegen(in dem fall sind SYSTEM_ADMIN wir)
- Keine automatische Erstellung mehr möglich

 User-Erstellung:
- SYSTEM_ADMIN erstellt COMPANY_ADMIN für bestehende Company
- COMPANY_ADMIN erstellt COMPANY_USER in eigener Company
- User werden mit Initial-Passwort angelegt
- User können sich sofort einloggen (keine Sonderlogik)

 Rollen
- Rollen werden erzwungen, nicht aus Request übernommen:
  - `/company-admin` Endpoint → immer COMPANY_ADMIN
  - `/users` Endpoint (COMPANY_ADMIN) → immer COMPANY_USER

 Login:
- Standard-Login ohne Sonderbehandlung
- Korrekte Credentials → Token
- Falsche Credentials → 401 error wird zurückgegeben. 

 Sicherheit:
- Passwörter werden niemals in API-Responses zurückgegeben
- Initial-Passwörter müssen extern übermittelt werden
- Keine Self-Registration möglich

 Workflow zusammenfassung: 

1. SYSTEM_ADMIN erstellt Company
2. SYSTEM_ADMIN erstellt COMPANY_ADMIN (mit Initial-Passwort)
3. SYSTEM_ADMIN sendet Zugangsdaten extern (Email außerhalb des Systems)
4. COMPANY_ADMIN loggt sich ein
5. COMPANY_ADMIN erstellt COMPANY_USER (mit Initial-Passwort)
6. COMPANY_USER loggt sich ein

**Alles manuell, keine Automatisierung.**

