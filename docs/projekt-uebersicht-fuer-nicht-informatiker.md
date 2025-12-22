# ChangeReady360 - Projektübersicht für Nicht-Informatiker

## 📖 Was ist ChangeReady360?

ChangeReady360 ist eine **B2B-Webanwendung** (Business-to-Business), die Unternehmen dabei hilft, ihre Veränderungsbereitschaft zu analysieren. Stellen Sie sich das wie eine digitale Plattform vor, auf der Unternehmen ihre Mitarbeiter befragen können, um zu verstehen, wie bereit sie für Veränderungen sind.

**Einfach gesagt:** Es ist wie ein Online-Fragebogen-System, aber speziell für Unternehmen entwickelt, mit strengen Sicherheitsregeln und der Möglichkeit, dass mehrere Unternehmen gleichzeitig die Plattform nutzen, ohne sich gegenseitig zu sehen.

---

## 🏗️ Wie ist die Anwendung aufgebaut?

Stellen Sie sich die Anwendung wie ein **Restaurant** vor:

### 🎨 **Frontend** (Die Speisekarte und der Speisesaal)
- **Was ist das?** Das ist der Teil, den Sie im Browser sehen - die Webseite, auf der Sie klicken und interagieren.
- **Technologie:** Angular (eine moderne Technologie für Webseiten)
- **Wo läuft es?** In einem Docker-Container auf Port 4200
- **Zugriff:** http://localhost:4200

**Analogie:** Das Frontend ist wie die Speisekarte und der Speisesaal im Restaurant - hier sehen und benutzen die Gäste (Benutzer) alles.

### ⚙️ **Backend** (Die Küche)
- **Was ist das?** Das ist der unsichtbare Teil, der im Hintergrund arbeitet - er verarbeitet Anfragen, speichert Daten, prüft Berechtigungen.
- **Technologie:** Spring Boot (Java)
- **Wo läuft es?** In einem Docker-Container auf Port 8080
- **Zugriff:** http://localhost:8080

**Analogie:** Das Backend ist wie die Küche im Restaurant - die Gäste sehen sie nicht, aber hier wird alles vorbereitet und verarbeitet.

### 🗄️ **Datenbank** (Das Lager)
- **Was ist das?** Hier werden alle Informationen gespeichert: Benutzer, Unternehmen, Einladungen, etc.
- **Technologie:** PostgreSQL (eine professionelle Datenbank)
- **Wo läuft es?** In einem Docker-Container auf Port 5432

**Analogie:** Die Datenbank ist wie das Lager im Restaurant - hier werden alle Zutaten (Daten) sicher aufbewahrt.

### 🐳 **Docker** (Das Restaurant-Gebäude)
- **Was ist das?** Docker ist eine Technologie, die alle Teile der Anwendung in "Container" verpackt - wie einzelne Räume in einem Gebäude.
- **Vorteil:** Alles läuft isoliert und kann einfach gestartet, gestoppt oder aktualisiert werden.

**Analogie:** Docker ist wie das gesamte Restaurant-Gebäude - es hält alle Räume (Container) zusammen und sorgt dafür, dass alles funktioniert.

---

## 👥 Wer kann die Anwendung nutzen? (Rollen-System)

Die Anwendung hat drei verschiedene **Rollen**, die unterschiedliche Berechtigungen haben:

### 1. 🔑 **SYSTEM_ADMIN** (Der Restaurantbesitzer)
- **Wer ist das?** Der Superadministrator - das sind Sie als Betreiber der Plattform
- **Was kann er tun?**
  - Neue Unternehmen erstellen
  - Unternehmen aktivieren oder deaktivieren
  - Alle Unternehmen und deren Benutzer einsehen
  - Company-Admins einladen (dabei wird automatisch ein Unternehmen erstellt, falls es noch nicht existiert)
- **Analogie:** Der Restaurantbesitzer kann neue Restaurants eröffnen, alle Restaurants verwalten und neue Manager einstellen.

### 2. 👔 **COMPANY_ADMIN** (Der Restaurant-Manager)
- **Wer ist das?** Der Administrator eines Unternehmens
- **Was kann er tun?**
  - Benutzer innerhalb seines eigenen Unternehmens verwalten
  - Neue Mitarbeiter (COMPANY_USER) einladen
  - Nur Daten seines eigenen Unternehmens sehen
- **Analogie:** Der Restaurant-Manager kann nur in seinem eigenen Restaurant arbeiten und neue Mitarbeiter einstellen, aber nicht in anderen Restaurants.

### 3. 👤 **COMPANY_USER** (Der normale Mitarbeiter)
- **Wer ist das?** Ein normaler Mitarbeiter eines Unternehmens
- **Was kann er tun?**
  - Sich einloggen
  - Die Anwendung nutzen (z.B. Fragebögen ausfüllen)
  - Nur Daten seines eigenen Unternehmens sehen
- **Analogie:** Der normale Mitarbeiter kann im Restaurant arbeiten und die Speisekarte sehen, aber keine Verwaltungsaufgaben übernehmen.

---

## 🔐 Wie funktioniert die Sicherheit?

### Passwort-Sicherheit
- **Keine Klartext-Passwörter:** Passwörter werden niemals im Klartext gespeichert
- **Verschlüsselung:** Passwörter werden mit einem speziellen Algorithmus (BCrypt) "gehasht" - das ist wie ein Einweg-Verschlüsselung
- **Beispiel:** Ihr Passwort "MeinPasswort123" wird zu etwas wie "aB3$kL9mN2pQ..." - selbst wenn jemand die Datenbank hackt, kann er Ihr ursprüngliches Passwort nicht sehen

### JWT-Token (Der Ausweis)
- **Was ist das?** Nach dem Login erhalten Sie einen "Token" - das ist wie ein digitaler Ausweis
- **Wie funktioniert es?**
  1. Sie loggen sich mit E-Mail und Passwort ein
  2. Das System prüft, ob Ihre Daten korrekt sind
  3. Wenn ja, erhalten Sie einen Token (wie einen Ausweis)
  4. Bei jeder Anfrage zeigen Sie diesen Token vor
  5. Das System prüft: "Ist dieser Token gültig? Welche Rolle hat dieser Benutzer? Zu welchem Unternehmen gehört er?"
- **Vorteil:** Sie müssen sich nicht bei jeder Aktion neu einloggen

### Multi-Tenancy (Unternehmen-Isolation)
- **Was bedeutet das?** Jedes Unternehmen ist von anderen Unternehmen isoliert
- **Wie funktioniert es?**
  - Jeder Benutzer gehört zu einem Unternehmen
  - Der Token enthält die Information, zu welchem Unternehmen der Benutzer gehört
  - Das System stellt sicher, dass Sie nur Daten Ihres eigenen Unternehmens sehen können
- **Beispiel:** Wenn Sie zu "Firma A" gehören, können Sie niemals Daten von "Firma B" sehen, auch wenn Sie es versuchen

**Analogie:** Es ist wie in einem Bürogebäude - jede Firma hat ihre eigene Etage mit eigenen Schlüsseln. Sie können nicht einfach in die Etage einer anderen Firma gehen.

---

## 📧 Wie funktioniert das Einladungssystem?

Das Einladungssystem funktioniert in **zwei Stufen**:

### Stufe 1: SYSTEM_ADMIN lädt COMPANY_ADMIN ein
1. **SYSTEM_ADMIN** erstellt eine Einladung für eine E-Mail-Adresse
2. **System prüft:** Existiert bereits ein Unternehmen mit diesem Namen?
   - **Wenn NEIN:** Ein neues Unternehmen wird automatisch erstellt
   - **Wenn JA:** Das bestehende Unternehmen wird verwendet
3. **System erstellt:** Eine Einladung mit einem einmaligen Token (wie ein Einmal-Passwort)
4. **Der Eingeladene erhält:** Einen Link mit diesem Token (z.B. per E-Mail)
5. **Der Eingeladene klickt auf den Link:** Er kommt auf eine Seite, wo er sein Passwort setzen kann
6. **Nach dem Passwort-Setzen:** Der Benutzer ist als COMPANY_ADMIN aktiv und kann sich einloggen

### Stufe 2: COMPANY_ADMIN lädt COMPANY_USER ein
1. **COMPANY_ADMIN** erstellt eine Einladung für eine E-Mail-Adresse
2. **System prüft:** Der COMPANY_ADMIN kann nur Benutzer in seinem eigenen Unternehmen einladen
3. **System erstellt:** Eine Einladung mit einem einmaligen Token
4. **Der Eingeladene erhält:** Einen Link mit diesem Token
5. **Der Eingeladene klickt auf den Link:** Er kommt auf eine Seite, wo er sein Passwort setzen kann
6. **Nach dem Passwort-Setzen:** Der Benutzer ist als COMPANY_USER aktiv und kann sich einloggen

### Wichtige Sicherheitsmerkmale:
- ✅ **Keine Self-Registration:** Niemand kann sich selbst registrieren - nur über Einladungen
- ✅ **Einmalige Tokens:** Jeder Token kann nur einmal verwendet werden
- ✅ **Ablaufdatum:** Tokens haben ein Ablaufdatum (z.B. 7 Tage)
- ✅ **Keine Passwörter in E-Mails:** Passwörter werden niemals per E-Mail verschickt - jeder setzt sein Passwort selbst

**Analogie:** Es ist wie bei einem exklusiven Club - Sie können nicht einfach reingehen, sondern müssen von einem Mitglied eingeladen werden. Sie erhalten eine Einladungskarte (Token), mit der Sie sich registrieren können.

---

## 🎯 Was kann die Anwendung aktuell?

### ✅ Bereits implementiert:

1. **Benutzer-Authentifizierung**
   - Login mit E-Mail und Passwort
   - JWT-Token-basierte Authentifizierung
   - Sichere Passwort-Speicherung

2. **Unternehmen-Verwaltung**
   - SYSTEM_ADMIN kann Unternehmen erstellen, anzeigen, aktivieren/deaktivieren
   - Jedes Unternehmen ist isoliert (Multi-Tenancy)

3. **Benutzer-Verwaltung**
   - COMPANY_ADMIN kann Benutzer in seinem Unternehmen verwalten
   - Benutzer können aktiviert/deaktiviert werden

4. **Einladungssystem**
   - SYSTEM_ADMIN kann COMPANY_ADMIN einladen (erstellt automatisch Unternehmen)
   - COMPANY_ADMIN kann COMPANY_USER einladen
   - Einmalige Tokens mit Ablaufdatum
   - Passwort-Setup über Einladungs-Link

5. **Zugangsanfragen**
   - Unternehmen können eine Anfrage stellen, um Zugang zur Plattform zu erhalten
   - SYSTEM_ADMIN kann diese Anfragen einsehen und bearbeiten

6. **Fehlerbehandlung**
   - Zentrale Fehlerbehandlung für alle API-Anfragen
   - Verständliche Fehlermeldungen

7. **API-Dokumentation**
   - Swagger UI für API-Dokumentation (http://localhost:8080/swagger-ui.html)

8. **Frontend-Demo**
   - Demo-Website mit Wizard-Flow (Fragebogen-Durchlauf)
   - Moderne Benutzeroberfläche
   - Responsive Design

---

## 🔄 Wie funktioniert der Datenfluss? (Einfach erklärt)

Stellen Sie sich vor, Sie möchten sich einloggen:

1. **Sie öffnen die Webseite** (Frontend)
   - Sie sehen ein Login-Formular

2. **Sie geben E-Mail und Passwort ein und klicken auf "Login"**
   - Das Frontend sendet diese Daten an das Backend

3. **Das Backend prüft Ihre Daten**
   - Es sucht in der Datenbank nach Ihrer E-Mail
   - Es vergleicht Ihr eingegebenes Passwort mit dem gespeicherten Hash
   - Es prüft, ob Ihr Konto aktiv ist

4. **Wenn alles korrekt ist:**
   - Das Backend erstellt einen JWT-Token
   - Dieser Token enthält: Ihre E-Mail, Ihre Rolle, Ihre Unternehmens-ID
   - Das Backend sendet diesen Token zurück an das Frontend

5. **Das Frontend speichert den Token**
   - Bei jeder weiteren Anfrage sendet das Frontend diesen Token mit

6. **Bei jeder weiteren Anfrage:**
   - Das Backend prüft den Token
   - Es liest Ihre Rolle und Unternehmens-ID aus dem Token
   - Es stellt sicher, dass Sie nur Daten Ihres Unternehmens sehen können
   - Es sendet die angeforderten Daten zurück

**Analogie:** Es ist wie beim Betreten eines Bürogebäudes:
1. Sie zeigen Ihren Ausweis am Empfang (Login)
2. Der Sicherheitsdienst prüft Ihren Ausweis (Backend prüft Token)
3. Sie erhalten einen Besucherausweis (JWT-Token)
4. Bei jedem Raum, den Sie betreten möchten, zeigen Sie diesen Ausweis (Token bei jeder Anfrage)
5. Der Sicherheitsdienst prüft: "Gehört dieser Raum zu Ihrer Firma?" (Unternehmens-Isolation)

---

## 🚀 Wie startet man die Anwendung?

### Voraussetzungen:
- Docker und Docker Compose müssen installiert sein

### Starten:
```bash
docker-compose up --build
```

**Was passiert dabei?**
1. Docker lädt alle benötigten "Container" (wie vorgefertigte Pakete)
2. Es baut das Frontend (Angular) neu
3. Es baut das Backend (Spring Boot) neu
4. Es startet die Datenbank (PostgreSQL)
5. Alle drei Container werden gestartet und verbunden

### Im Hintergrund starten:
```bash
docker-compose up --build -d
```
(Das `-d` bedeutet "detached" - die Container laufen im Hintergrund)

### Zugriff:
- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080
- **Swagger UI (API-Dokumentation):** http://localhost:8080/swagger-ui.html

### Container-Status prüfen:
```bash
docker ps
```

### Anwendung stoppen:
```bash
docker-compose down
```

---

## 📁 Wie ist der Code organisiert?

### Backend-Struktur (Spring Boot):

```
backend/
├── src/main/java/com/changeready/
│   ├── controller/      → Empfängt HTTP-Anfragen (wie ein Empfangsschalter)
│   ├── service/         → Enthält die Geschäftslogik (wie die Verwaltung)
│   ├── repository/      → Zugriff auf die Datenbank (wie ein Archiv)
│   ├── entity/          → Datenmodelle (wie Formulare)
│   ├── dto/             → Datenübertragungsobjekte (wie Briefumschläge)
│   ├── security/        → Sicherheits-Konfiguration (wie Sicherheitsdienst)
│   ├── config/          → Allgemeine Konfiguration (wie Gebäudeverwaltung)
│   └── exception/       → Fehlerbehandlung (wie Beschwerdestelle)
```

**Einfach erklärt:**
- **Controller:** Empfängt Anfragen von außen (wie ein Empfangsschalter)
- **Service:** Verarbeitet die Anfragen und führt die eigentliche Arbeit aus (wie die Verwaltung)
- **Repository:** Speichert und lädt Daten aus der Datenbank (wie ein Archiv)
- **Entity:** Beschreibt, wie Daten in der Datenbank gespeichert werden (wie ein Formular)
- **DTO:** Beschreibt, wie Daten zwischen Frontend und Backend übertragen werden (wie ein Briefumschlag)

### Frontend-Struktur (Angular):

```
frontend/test/src/app/
├── pages/          → Die verschiedenen Seiten (Landing, Wizard-Mode, etc.)
├── components/     → Wiederverwendbare Bausteine (Buttons, Cards, etc.)
├── services/       → Services für Datenverwaltung (wie ein Vermittler)
├── models/         → Datenmodelle (wie Formulare)
└── app.routes.ts   → Definiert, welche Seite bei welcher URL angezeigt wird
```

**Einfach erklärt:**
- **Pages:** Die verschiedenen Seiten, die der Benutzer sieht
- **Components:** Kleine wiederverwendbare Bausteine (z.B. ein Button, eine Karte)
- **Services:** Vermitteln zwischen Frontend und Backend (senden Anfragen, empfangen Antworten)

---

## 🛡️ Sicherheits-Features im Detail

### 1. Passwort-Hashing
- **Problem:** Wenn jemand die Datenbank hackt, könnte er alle Passwörter sehen
- **Lösung:** Passwörter werden mit BCrypt "gehasht" - das ist eine Einweg-Verschlüsselung
- **Beispiel:** "MeinPasswort123" → "aB3$kL9mN2pQ..." (kann nicht zurückgewandelt werden)

### 2. JWT-Token
- **Problem:** Wie weiß das System, wer Sie sind, ohne dass Sie sich bei jeder Aktion neu einloggen müssen?
- **Lösung:** Nach dem Login erhalten Sie einen Token, der Ihre Identität bestätigt
- **Vorteil:** Sie müssen sich nur einmal einloggen, dann funktioniert alles automatisch

### 3. Rollenbasierte Zugriffskontrolle (RBAC)
- **Problem:** Nicht jeder sollte alles tun können
- **Lösung:** Jeder Benutzer hat eine Rolle (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER)
- **Wie es funktioniert:** Das System prüft bei jeder Aktion: "Hat dieser Benutzer die richtige Rolle?"

### 4. Multi-Tenancy (Unternehmens-Isolation)
- **Problem:** Unternehmen A sollte nicht die Daten von Unternehmen B sehen können
- **Lösung:** Jeder Benutzer gehört zu einem Unternehmen, und das System filtert alle Daten nach Unternehmens-ID
- **Wie es funktioniert:** Der Token enthält die Unternehmens-ID, und bei jeder Datenbankabfrage wird nur nach Daten dieses Unternehmens gesucht

### 5. Zentrale Fehlerbehandlung
- **Problem:** Wenn etwas schiefgeht, sollte der Benutzer eine verständliche Fehlermeldung erhalten
- **Lösung:** Alle Fehler werden zentral behandelt und in ein einheitliches Format gebracht
- **Vorteil:** Konsistente Fehlermeldungen, keine technischen Details für den Benutzer

---

## 🔍 Was passiert beim Login? (Schritt für Schritt)

1. **Benutzer gibt E-Mail und Passwort ein** (Frontend)
2. **Frontend sendet diese Daten an Backend** (HTTP POST Request)
3. **Backend empfängt die Anfrage** (AuthController)
4. **Backend sucht den Benutzer in der Datenbank** (UserRepository)
5. **Backend prüft, ob der Benutzer existiert und aktiv ist**
6. **Backend vergleicht das eingegebene Passwort mit dem gespeicherten Hash** (BCrypt)
7. **Wenn Passwort korrekt:**
   - Backend erstellt einen JWT-Token mit:
     - E-Mail
     - Rolle (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER)
     - Unternehmens-ID
     - Ablaufdatum
   - Backend sendet den Token zurück an Frontend
8. **Frontend speichert den Token** (meist im Browser-Speicher)
9. **Bei jeder weiteren Anfrage:**
   - Frontend sendet den Token mit
   - Backend prüft den Token (ist er gültig? ist er abgelaufen?)
   - Backend liest Rolle und Unternehmens-ID aus dem Token
   - Backend führt die angeforderte Aktion aus (mit den entsprechenden Berechtigungen)

---

## 📊 Datenbank-Struktur (Vereinfacht)

Die Datenbank speichert folgende Informationen:

### **companies** (Unternehmen)
- ID (eindeutige Nummer)
- Name (z.B. "Firma ABC")
- Aktiv (ja/nein)
- Erstellt am (Datum)
- Aktualisiert am (Datum)

### **users** (Benutzer)
- ID (eindeutige Nummer)
- E-Mail (z.B. "max.mustermann@firma.de")
- Passwort-Hash (verschlüsseltes Passwort)
- Rolle (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER)
- Unternehmen (zu welchem Unternehmen gehört dieser Benutzer?)
- Aktiv (ja/nein)
- Erstellt am (Datum)
- Aktualisiert am (Datum)

### **invites** (Einladungen)
- ID (eindeutige Nummer)
- Token (einmaliger Einladungs-Code)
- E-Mail (wer wird eingeladen?)
- Rolle (welche Rolle soll der Eingeladene erhalten?)
- Unternehmen (zu welchem Unternehmen gehört die Einladung?)
- Status (PENDING, ACCEPTED, EXPIRED, CANCELLED)
- Ablaufdatum (wann läuft die Einladung ab?)
- Erstellt von (wer hat die Einladung erstellt?)
- Akzeptiert am (wann wurde die Einladung angenommen?)

### **company_access_requests** (Zugangsanfragen)
- ID (eindeutige Nummer)
- E-Mail (wer stellt die Anfrage?)
- Firmenname (welches Unternehmen möchte Zugang?)
- Nachricht (optionale Nachricht)
- Status (PENDING, APPROVED, REJECTED)
- Erstellt am (Datum)

---

## 🎨 Frontend-Demo (Wizard-Flow)

Das Frontend enthält aktuell eine **Demo-Website** mit einem **Wizard-Flow** (Assistenten-Durchlauf):

1. **Landing Page** (Startseite)
   - Begrüßung und Einführung

2. **Wizard Mode** (Modus-Auswahl)
   - Benutzer wählt einen Analyse-Modus

3. **Wizard Context** (Kontext-Auswahl)
   - Benutzer wählt einen Kontext

4. **Wizard Category** (Kategorien mit Fragen)
   - Benutzer beantwortet Fragen in verschiedenen Kategorien
   - Fragen werden mit einer 1-5 Skala bewertet

5. **Wizard Summary** (Zusammenfassung)
   - Übersicht über die Auswahl
   - Vorschau eines Scores (aktuell noch Demo-Daten)

**Hinweis:** Dies ist aktuell nur eine Demo - die tatsächliche Verbindung zum Backend für das Speichern von Antworten ist noch nicht implementiert.

---

## 🔧 Technische Details (Für Interessierte)

### Backend:
- **Framework:** Spring Boot 4.0.0
- **Sprache:** Java 17
- **Build-Tool:** Gradle
- **Datenbank:** PostgreSQL 15
- **Sicherheit:** Spring Security mit JWT
- **API-Dokumentation:** SpringDoc OpenAPI (Swagger)

### Frontend:
- **Framework:** Angular 21
- **Sprache:** TypeScript
- **Build-Tool:** npm
- **Webserver:** Nginx (für die Produktion)
- **Styling:** CSS mit Corporate Design

### Deployment:
- **Containerisierung:** Docker
- **Orchestrierung:** Docker Compose
- **Netzwerk:** Bridge-Netzwerk für Container-Kommunikation

---

## 📝 Zusammenfassung

**ChangeReady360** ist eine sichere, mehrstufige B2B-Webanwendung, die:

✅ **Sicherheit** durch JWT-Tokens, Passwort-Hashing und Rollen-basierte Zugriffskontrolle bietet

✅ **Multi-Tenancy** durch strikte Unternehmens-Isolation gewährleistet

✅ **Einladungssystem** mit zwei Stufen (SYSTEM_ADMIN → COMPANY_ADMIN → COMPANY_USER) implementiert

✅ **Moderne Architektur** mit klarer Trennung zwischen Frontend, Backend und Datenbank verwendet

✅ **Docker-basiertes Deployment** für einfache Installation und Wartung bietet

✅ **API-Dokumentation** über Swagger UI zur Verfügung stellt

✅ **Frontend-Demo** mit Wizard-Flow für erste Tests enthält

---

## 🎯 Nächste Schritte (Was noch kommt)

Aktuell ist die **Grundlage** (Foundation) implementiert:
- Authentifizierung ✅
- Autorisierung ✅
- Unternehmen-Verwaltung ✅
- Benutzer-Verwaltung ✅
- Einladungssystem ✅

**Geplant für die Zukunft:**
- Survey-Funktionalität (Fragebögen erstellen und verwalten)
- Teilnahme-System (Mitarbeiter können Fragebögen ausfüllen)
- Ergebnis-Analyse (Auswertung der Antworten)
- Reporting (Berichte und Visualisierungen)
- Erweiterte Benutzer-Verwaltung (Profile bearbeiten, etc.)

---

## ❓ Häufige Fragen (FAQ)

### Warum Docker?
Docker stellt sicher, dass die Anwendung auf jedem Computer gleich läuft - unabhängig vom Betriebssystem. Es ist wie eine "Box", in der alles enthalten ist, was die Anwendung braucht.

### Warum drei Rollen?
Die drei Rollen ermöglichen eine klare Hierarchie:
- SYSTEM_ADMIN verwaltet die gesamte Plattform
- COMPANY_ADMIN verwaltet sein Unternehmen
- COMPANY_USER nutzt die Plattform

### Warum JWT-Token statt Sessions?
JWT-Token sind "stateless" - das bedeutet, der Server muss sich nicht merken, wer eingeloggt ist. Das macht die Anwendung schneller und einfacher zu skalieren.

### Warum Multi-Tenancy?
Multi-Tenancy ermöglicht es, dass mehrere Unternehmen die gleiche Plattform nutzen, ohne sich gegenseitig zu sehen. Das ist effizienter als für jedes Unternehmen eine separate Installation.

### Wie sicher ist das System?
Das System verwendet moderne Sicherheitsstandards:
- Passwörter werden gehasht (nicht verschlüsselt - das ist sicherer)
- JWT-Token haben ein Ablaufdatum
- Alle Anfragen werden auf Berechtigung geprüft
- Unternehmen sind strikt voneinander isoliert

---

**Erstellt am:** 18. Dezember 2025  
**Version:** 1.0  
**Status:** Foundation Phase abgeschlossen


