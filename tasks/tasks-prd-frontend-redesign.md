# Task List: Frontend Redesign - Modern Dashboard UI

## Relevant Files

### Frontend Files
- `frontend/test/src/app/layouts/dashboard-layout/dashboard-layout.component.ts` - Haupt-Layout-Komponente, muss umgestaltet werden
- `frontend/test/src/app/layouts/dashboard-layout/dashboard-layout.component.html` - Layout-Template für Sidebar und Header
- `frontend/test/src/app/layouts/dashboard-layout/dashboard-layout.component.css` - Styles für neues Design
- `frontend/test/src/app/pages/dashboard/dashboard.component.ts` - Dashboard-Komponente mit KPIs
- `frontend/test/src/app/pages/dashboard/dashboard.component.html` - Dashboard-Template
- `frontend/test/src/app/pages/dashboard/dashboard.component.css` - Dashboard-Styles
- `frontend/test/src/app/pages/stakeholder/stakeholder.component.ts` - Stakeholder-Komponente
- `frontend/test/src/app/pages/stakeholder/stakeholder.component.html` - Stakeholder-Template
- `frontend/test/src/app/pages/stakeholder/stakeholder.component.css` - Stakeholder-Styles
- `frontend/test/src/app/pages/surveys/surveys.component.ts` - Surveys-Komponente (wird zu Assessment)
- `frontend/test/src/app/pages/surveys/surveys.component.html` - Surveys-Template
- `frontend/test/src/app/pages/results/results.component.ts` - Results-Komponente (wird zu Reporting)
- `frontend/test/src/app/pages/results/results.component.html` - Results-Template
- `frontend/test/src/app/services/dashboard.service.ts` - Neuer Service für Dashboard-KPIs (muss erstellt werden)
- `frontend/test/src/app/services/stakeholder.service.ts` - Neuer Service für Stakeholder-Daten (muss erstellt werden)
- `frontend/test/src/app/services/reporting.service.ts` - Neuer Service für Reporting-Daten (muss erstellt werden)
- `frontend/test/src/app/components/donut-chart/donut-chart.component.ts` - Neue Donut-Chart-Komponente (muss erstellt werden)
- `frontend/test/src/app/components/line-chart/line-chart.component.ts` - Neue Line-Chart-Komponente (muss erstellt werden)
- `frontend/test/src/app/models/dashboard.model.ts` - Modelle für Dashboard-Daten (muss erstellt werden)
- `frontend/test/src/app/models/stakeholder.model.ts` - Modelle für Stakeholder-Daten (muss erstellt werden)
- `frontend/test/src/app/models/reporting.model.ts` - Modelle für Reporting-Daten (muss erstellt werden)
- `frontend/test/package.json` - Dependencies für Chart-Bibliothek hinzufügen

### Backend Files
- `backend/src/main/java/com/changeready/controller/DashboardController.java` - Neuer Controller für Dashboard-KPIs (muss erstellt werden)
- `backend/src/main/java/com/changeready/controller/StakeholderController.java` - Neuer Controller für Stakeholder-Daten (muss erstellt werden)
- `backend/src/main/java/com/changeready/controller/ReportingController.java` - Neuer Controller für Reporting-Daten (muss erstellt werden)
- `backend/src/main/java/com/changeready/service/DashboardService.java` - Service-Interface für Dashboard (muss erstellt werden)
- `backend/src/main/java/com/changeready/service/DashboardServiceImpl.java` - Service-Implementierung für Dashboard (muss erstellt werden)
- `backend/src/main/java/com/changeready/service/StakeholderService.java` - Service-Interface für Stakeholder (muss erstellt werden)
- `backend/src/main/java/com/changeready/service/StakeholderServiceImpl.java` - Service-Implementierung für Stakeholder (muss erstellt werden)
- `backend/src/main/java/com/changeready/service/ReportingService.java` - Service-Interface für Reporting (muss erstellt werden)
- `backend/src/main/java/com/changeready/service/ReportingServiceImpl.java` - Service-Implementierung für Reporting (muss erstellt werden)
- `backend/src/main/java/com/changeready/dto/dashboard/DashboardKpisResponse.java` - DTO für Dashboard-KPIs (muss erstellt werden)
- `backend/src/main/java/com/changeready/dto/dashboard/TrendDataResponse.java` - DTO für Trend-Daten (muss erstellt werden)
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupResponse.java` - DTO für Stakeholder-Gruppen (muss erstellt werden)
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupDetailResponse.java` - DTO für Stakeholder-Gruppen-Details (muss erstellt werden)
- `backend/src/main/java/com/changeready/entity/Measure.java` - Entity für Maßnahmen (muss erstellt werden, falls nicht vorhanden)
- `backend/src/main/java/com/changeready/repository/MeasureRepository.java` - Repository für Maßnahmen (muss erstellt werden, falls nicht vorhanden)

### Notes
- Chart-Bibliothek muss installiert werden (z.B. Chart.js mit ng2-charts oder Recharts)
- Alle Backend-Endpoints müssen rollenbasiert geschützt sein
- Company-Isolation muss bei allen Endpoints sichergestellt werden
- Empty States müssen für alle Komponenten implementiert werden

## Tasks

- [x] 1.0 Layout & Struktur umgestalten
  - [x] 1.1 Sidebar umgestalten: Dunkelblaue Sidebar mit Logo "ChangeReady 360" (360 in grünem Badge)
  - [x] 1.2 Navigation-Items aktualisieren: Dashboard, Stakeholder, Assessment, Maßnahmen, Reporting
  - [x] 1.3 User-Profil in Sidebar: Avatar, Name, Rolle am unteren Rand
  - [x] 1.4 "POWERED BY Innovera Consulting" Footer in Sidebar hinzufügen
  - [x] 1.5 Header-Bar umgestalten: Firmenname mit Dropdown links, Suchleiste Mitte, Benachrichtigungen & User rechts
  - [x] 1.6 Action-Buttons im Header: "Aktualisieren" und "Report" Button hinzufügen
  - [x] 1.7 Responsive Design: Sidebar auf Mobile als Overlay, Grid-Layouts anpassen
- [x] 2.0 Dashboard-Seite neu gestalten
  - [x] 2.1 Dashboard-Model erstellen: Interfaces für KPIs, Readiness-Daten, Stakeholder-Gruppen
  - [x] 2.2 Dashboard-Service erstellen: HTTP-Service für Backend-KPIs
  - [x] 2.3 KPI-Cards oben implementieren: 4 Cards (Readiness Score, Stakeholder, Kritiker, Maßnahmen)
  - [x] 2.4 Donut-Chart-Komponente erstellen: Readiness-Visualisierung ohne Zahlen im Chart
  - [x] 2.5 Gesamt-Readiness Card: Donut-Chart mit Promoter/Neutral/Kritiker-Zahlen darunter
  - [x] 2.6 Stakeholder-Gruppen Card: Liste mit Readiness-%, Trends, Progress-Bars, Status-Badges
  - [x] 2.7 Empty States für Dashboard: Saubere Empty States wenn keine Daten vorhanden
- [ ] 3.0 Stakeholder-Seite neu gestalten
  - [x] 3.1 Stakeholder-Model erstellen: Interfaces für Gruppen, Details, Trends
  - [x] 3.2 Stakeholder-Service erstellen: HTTP-Service für Stakeholder-Daten
  - [x] 3.3 Stakeholder-Übersicht Header: Titel, Untertitel, "+ Stakeholder hinzufügen" Button
  - [x] 3.4 KPI-Cards oben: Gesamt, Promoter, Neutral, Kritiker (aus Backend)
  - [x] 3.5 Gruppen-Cards Grid: Cards mit Icon, Status-Badge, Titel, Subtitle, Dots, Readiness-%, Trend
  - [x] 3.6 Stakeholder-Detailansicht: Zurück-Button, Gruppen-Header, Statistiken, Readiness-Gauge
  - [x] 3.7 Trend-Indikatoren: Berechnung und Anzeige von Trends (+X%/-X%/0%)
  - [ ] 3.8 Empty States für Stakeholder: Wenn keine Gruppen oder Daten vorhanden
- [ ] 4.0 Assessment-Seite neu gestalten
  - [ ] 4.1 Assessment-Start-Screen: Titel, Untertitel, zentrale Card mit Icon und Beschreibung
  - [ ] 4.2 ADKAR-Bereiche Cards: 4 Cards für Wissen, Fähigkeit, Motivation, Kommunikation
  - [ ] 4.3 Assessment-Info: Geschätzte Dauer, Zwischenspeicherung möglich
  - [ ] 4.4 Assessment-Start-Button: Grüner Button mit Play-Icon
  - [ ] 4.5 ADKAR-Modell Integration: Survey-Templates müssen ADKAR-Struktur unterstützen
- [ ] 5.0 Reporting-Seite neu gestalten
  - [ ] 5.1 Reporting-Model erstellen: Interfaces für Management Summary, Trend-Daten
  - [ ] 5.2 Reporting-Service erstellen: HTTP-Service für Reporting-Daten
  - [ ] 5.3 Management Summary Section: Titel, Datum, 4 KPI-Cards, 5 Abteilungs-Cards
  - [ ] 5.4 Line-Chart-Komponente erstellen: Readiness-Verlauf & Prognose Chart
  - [ ] 5.5 Trend-Chart implementieren: Grüne Linie (Ist), orange gestrichelte Linie (Ziel)
  - [ ] 5.6 Export-Funktionen: "Drucken" und "Report exportieren" Buttons im Header
  - [ ] 5.7 Empty States für Reporting: Wenn keine Trend-Daten vorhanden
- [ ] 6.0 Backend-Endpoints implementieren
  - [ ] 6.1 Dashboard DTOs erstellen: DashboardKpisResponse, TrendDataResponse
  - [ ] 6.2 Dashboard Service & Controller: GET /api/v1/dashboard/kpis Endpoint
  - [ ] 6.3 Stakeholder DTOs erstellen: StakeholderGroupResponse, StakeholderGroupDetailResponse
  - [ ] 6.4 Stakeholder Service & Controller: GET /api/v1/stakeholder/groups Endpoints
  - [ ] 6.5 Reporting Service & Controller: GET /api/v1/reporting/trends Endpoint
  - [ ] 6.6 Maßnahmen Entity & Repository: Falls nicht vorhanden, Measure Entity erstellen
  - [ ] 6.7 Maßnahmen Endpoint: GET /api/v1/measures/open Endpoint
  - [ ] 6.8 Security Config aktualisieren: Alle neuen Endpoints rollenbasiert schützen

