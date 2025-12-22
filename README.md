# ChangeReady360

Willkommen im neuen Entwicklungsbranch von ChangeReady360. Mit diesem Branch beginnt offiziell die Implementierungsphase des Projekts. Bisher befand sich ChangeReady in der Konzeptions-Planungsphase. Dieser Branch dient als Grundlage für den Aufbau der gesamten Codebasis.

## Projekt-Struktur

- **Backend**: Spring Boot (Java 17, Gradle) - läuft in Docker
- **Frontend**: Angular 21 - läuft lokal mit Hot Reload
- **Datenbank**: PostgreSQL 15 - läuft in Docker

## 🚀 Projekt starten

### 1. Backend & Datenbank starten (Docker)

```bash
docker-compose up --build
# oder im Hintergrund:
docker-compose up --build -d
```

Dies startet:
- PostgreSQL auf Port **5432**
- Spring Boot Backend auf Port **8080**

### 2. Frontend starten (lokal mit Hot Reload)

```bash
cd frontend/test
npm install  # nur beim ersten Mal
npm start
```

Frontend läuft auf: **http://localhost:4200**

**Vorteil**: Änderungen am Frontend werden automatisch im Browser aktualisiert - kein Docker-Rebuild nötig! ✨

## 📋 Zugriff

- **Frontend**: http://localhost:4200 (lokal mit `ng serve`)
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 🔧 Nützliche Befehle

**Docker Container prüfen:**
```bash
docker ps
```

**Nur Backend/Datenbank stoppen:**
```bash
docker-compose down
```

**Frontend Development:**
```bash
cd frontend/test
npm start  # Hot Reload aktiviert
```

Siehe [frontend/test/DEV.md](./frontend/test/DEV.md) für detaillierte Frontend-Entwicklungsanleitung.





