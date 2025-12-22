# Quick Start Guide

## 🚀 Projekt starten

### Schritt 1: Backend & Datenbank
```bash
docker-compose up -d
```
Wartet ca. 20-30 Sekunden bis Backend bereit ist.

### Schritt 2: Frontend
```bash
cd frontend/test
npm start
```

### Schritt 3: Browser öffnen
- Öffne: http://localhost:4200
- Du wirst automatisch zu `/login` weitergeleitet

## 🔑 Einloggen

**Admin-User:**
- Email: `admin@changeready360.com`
- Password: `Admin123!`

Nach erfolgreichem Login → Weiterleitung zu `/app/dashboard`

## ✅ Basis-Flow testen

1. **Login**: http://localhost:4200 → Login-Seite → Credentials eingeben
2. **Dashboard**: Nach Login → Dashboard wird angezeigt
3. **Navigation**: Sidebar-Navigation funktioniert
4. **Logout**: Logout-Button in Sidebar → Zurück zu Login

## 🛠️ Troubleshooting

**Backend nicht erreichbar?**
```bash
docker-compose ps  # Prüfe ob Container laufen
docker logs spring_backend  # Prüfe Logs
```

**Frontend startet nicht?**
```bash
cd frontend/test
rm -rf node_modules package-lock.json
npm install
npm start
```

**CORS-Fehler?**
- Backend muss neu gebaut werden: `docker-compose build backend`
- Backend neu starten: `docker-compose restart backend`

## 📋 Projektstruktur

Siehe `PROJECT_STRUCTURE.md` für detaillierte Struktur-Übersicht.
