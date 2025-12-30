# Survey-Instanzen löschen (Hard Delete) + sofortige UI-Aktualisierung

Ziel: **100% backend-driven** – wenn eine offene (DRAFT) oder abgeschlossene (SUBMITTED) Umfrage gelöscht wird, muss sie

- sofort aus der UI verschwinden
- dauerhaft aus Backend/DB entfernt sein (auch nach Neustart)
- in Dashboard/Stakeholder/Reporting/Results nicht mehr auftauchen bzw. mitgerechnet werden

## Backend

### Neuer Endpoint

- **DELETE** `/api/v1/surveys/instances/{instanceId}`
  - **Rollen**: `SYSTEM_ADMIN` & `COMPANY_ADMIN`
  - **Company-Isolation**: nur Instanzen der eigenen Company
  - **Hard Delete**: keine Soft-Deletes, keine Flags

### Was wird mit gelöscht?

- `survey_instances` (die Instanz selbst)
- `survey_answers` (alle Antworten zur Instanz)

Um FK/Orphans zu vermeiden, werden Antworten **vor** der Instanz gelöscht.

## Frontend

### UI-Änderung

Auf `/app/surveys` gibt es in:

- **Offene Umfragen**
- **Abgeschlossene Umfragen**

jeweils einen **Löschen**-Button (Trash Icon) pro Instanz, mit Confirmation Dialog.

### Nach erfolgreichem Delete werden neu geladen

- `GET /api/v1/surveys/instances` (Listen aktualisieren)
- `GET /api/v1/dashboard/kpis` (über `DashboardService.getDashboardData()`)
- `GET /api/v1/stakeholder/groups` (ebenfalls über `DashboardService.getDashboardData()` + zusätzlich direkt)
- `GET /api/v1/reporting/data` (Reporting/Trend aktualisieren)

Damit sind **Surveys-Liste + Dashboard + Stakeholder + Reporting** sofort konsistent.

## Verifikation (Quick Checklist)

- **Delete** einer DRAFT-Instanz → verschwindet sofort aus „Offene Umfragen“, nach Reload/Restart bleibt sie weg
- **Delete** einer SUBMITTED-Instanz → verschwindet sofort aus „Abgeschlossene Umfragen“, KPI/Readiness/Trends passen sich an
- Dashboard/Stakeholder/Reporting zeigen nach Delete keine Werte mehr, die von der gelöschten Instanz stammen


