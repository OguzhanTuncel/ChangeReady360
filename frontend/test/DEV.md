# Frontend Development Setup

## Lokale Entwicklung mit Hot Reload

Das Frontend läuft jetzt lokal mit `ng serve` für Hot Reload und Auto-Refresh.

### Voraussetzungen

- Node.js installiert (empfohlen: Node 22+)
- npm installiert
- Backend läuft auf `http://localhost:8080` (via Docker Compose)

### Setup

1. **Dependencies installieren** (einmalig):
```bash
cd frontend/test
npm install
```

### Development Server starten

```bash
cd frontend/test
npm start
# oder
ng serve
```

Das Frontend läuft dann auf: **http://localhost:4200**

### Features

✅ **Hot Reload**: Änderungen werden automatisch im Browser aktualisiert  
✅ **Live Reload**: Bei Code-Änderungen wird die Seite automatisch neu geladen  
✅ **Source Maps**: Debugging direkt im Browser möglich  
✅ **Fast Refresh**: Schnelle Updates ohne vollständigen Reload

### API-Verbindung

Das Frontend kommuniziert mit dem Backend auf:
- **Development**: `http://localhost:8080/api/v1` (siehe `src/environments/environment.ts`)

### Docker Compose (nur Backend + DB)

Um nur Backend und Datenbank zu starten:

```bash
# Im Projekt-Root
docker-compose up -d
```

Dies startet:
- PostgreSQL auf Port 5432
- Spring Boot Backend auf Port 8080

Das Frontend läuft separat lokal mit `ng serve`.

### Troubleshooting

**Port bereits belegt?**
```bash
ng serve --port 4201
```

**Backend nicht erreichbar?**
- Prüfe ob Backend läuft: `http://localhost:8080`
- Prüfe CORS-Einstellungen im Backend
- Prüfe Browser-Konsole auf Fehler

**Dependencies Probleme?**
```bash
rm -rf node_modules package-lock.json
npm install
```
