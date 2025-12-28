# PRD: Backend Gap-Only Implementation - End-to-End Data Flow

## Problem Statement

Das Frontend erwartet vollständige Backend-Endpoints für Survey-Management, Stakeholder-Management und Datenaggregation. Aktuell existieren bereits:
- ✅ Dashboard/Stakeholder/Reporting Controller mit Endpoints (aber mit TODO-Implementierungen)
- ✅ DTOs für alle Response-Typen
- ✅ SurveyTemplate Entity (gerade erstellt)
- ✅ Measure Entity & Repository & Service (vollständig implementiert)

**Fehlend sind:**
- Survey-Entities (Instance, Answer) und Repositories
- Survey Service & Controller (komplett neu)
- Stakeholder Entities (Group, Person) und Repositories
- Stakeholder CRUD-Endpoints (nur GET vorhanden)
- Readiness-Berechnungslogik
- Echte Datenaggregation in bestehenden Services

## Goals

1. **Survey-Flow end-to-end**: User kann Survey starten, Antworten speichern, abschicken
2. **Stakeholder-Management**: COMPANY_ADMIN kann Gruppen erstellen/bearbeiten, Personen hinzufügen
3. **Readiness-Berechnung**: Automatische Berechnung aus Survey-Antworten
4. **Datenaggregation**: Dashboard/Reporting/Stakeholder Services liefern echte Daten
5. **Keine Breaking Changes**: Bestehende Endpoints/Signaturen bleiben unverändert

## Non-Goals

- Performance-Optimierung mit Snapshots (kommt später)
- Breaking Changes an bestehenden Endpoints
- Löschen/Umbennen von bestehenden Entities/DTOs
- Änderungen an bestehenden Service-Signaturen

## User Stories

### Survey-Management
- **US-S1**: Als COMPANY_USER möchte ich aktive Survey-Templates sehen, damit ich eine Umfrage starten kann
- **US-S2**: Als COMPANY_USER möchte ich eine Survey-Instanz erstellen, damit ich eine Umfrage starten kann
- **US-S3**: Als COMPANY_USER möchte ich Antworten speichern (Autosave), damit ich später weitermachen kann
- **US-S4**: Als COMPANY_USER möchte ich eine Survey abschicken, damit die Ergebnisse ausgewertet werden
- **US-S5**: Als COMPANY_USER möchte ich meine offenen und abgeschlossenen Surveys sehen

### Stakeholder-Management
- **US-ST1**: Als COMPANY_ADMIN möchte ich Stakeholder-Gruppen erstellen, damit ich Stakeholder organisieren kann
- **US-ST2**: Als COMPANY_ADMIN möchte ich Stakeholder-Gruppen bearbeiten, damit ich Details aktualisieren kann
- **US-ST3**: Als COMPANY_ADMIN möchte ich Personen zu Gruppen hinzufügen, damit ich Stakeholder zuordnen kann
- **US-ST4**: Als COMPANY_USER möchte ich Stakeholder-Gruppen mit Readiness-Werten sehen (bereits vorhanden, muss mit echten Daten gefüllt werden)

### Readiness-Berechnung
- **US-R1**: Das System soll Readiness automatisch aus Survey-Antworten berechnen (Formel: ((Durchschnitt - 1) / 4) * 100)
- **US-R2**: Das System soll Promoter/Neutral/Kritiker automatisch kategorisieren basierend auf Readiness-Werten
- **US-R3**: Das System soll Trends berechnen (aktueller Wert vs. Wert vor 30 Tagen)
- **US-R4**: Das System soll Status berechnen (ready >=75%, attention 50-75%, critical <50%)

### Datenaggregation
- **US-D1**: Dashboard soll echte KPIs aus SurveyInstances, StakeholderGroups und Measures aggregieren
- **US-D2**: Dashboard soll echte Trend-Daten aus historischen Survey-Ergebnissen zeigen
- **US-D3**: Reporting soll echte Management Summary, Department Readiness und Trends zeigen
- **US-D4**: Stakeholder-Service soll echte Gruppen-Daten mit berechneten Readiness-Werten zurückgeben

## Functional Requirements

### Survey-Management

#### FR-S1: SurveyTemplate Repository
- `SurveyTemplateRepository` erstellen
- Methoden: `findByActive(Boolean active)`, `findByCompanyId(Long companyId)` (optional, falls company-spezifisch)

#### FR-S2: SurveyInstance Entity
- Felder: id, templateId (FK), userId (FK), companyId (FK), participantType (enum: PMA/AFFECTED), department (enum), status (enum: DRAFT/SUBMITTED), createdAt, updatedAt, submittedAt (optional)
- JPA-Annotationen wie bestehende Entities (User.java als Referenz)

#### FR-S3: SurveyAnswer Entity
- Felder: id, instanceId (FK), questionId (String, referenziert Frage-ID aus Template JSON), value (Integer 1-5), createdAt, updatedAt
- Composite Key oder separate ID (wie bestehende Pattern)

#### FR-S4: Survey Repositories
- `SurveyInstanceRepository`: `findByUserIdAndCompanyId(Long userId, Long companyId)`, `findByCompanyIdAndStatus(Long companyId, SurveyInstanceStatus status)`, `findByTemplateIdAndCompanyId(Long templateId, Long companyId)`
- `SurveyAnswerRepository`: `findByInstanceId(Long instanceId)`, `save` mit Update-Logik (falls Frage bereits beantwortet)

#### FR-S5: Survey DTOs
- `SurveyTemplateResponse`: Template mit Kategorien-Struktur (aus categoriesJson parsen)
- `SurveyInstanceResponse`: Instanz für Liste (id, templateId, templateName, status, createdAt, submittedAt)
- `SurveyInstanceDetailResponse`: Instanz-Details mit vollständigem Template und allen Antworten
- `SurveyInstanceCreateRequest`: templateId, participantType, department
- `SurveyAnswerUpdateRequest`: Array von {questionId, value}

#### FR-S6: Survey Service
- `getTemplates(UserPrincipal)`: Alle aktiven Templates für Company
- `createInstance(CreateRequest, UserPrincipal)`: Neue Instanz erstellen (Status: DRAFT)
- `getInstances(UserPrincipal)`: Alle Instanzen des Users
- `getInstance(Long instanceId, UserPrincipal)`: Instanz-Details mit Template und Antworten
- `saveAnswers(Long instanceId, AnswerUpdateRequest, UserPrincipal)`: Antworten speichern/aktualisieren (Autosave)
- `submitInstance(Long instanceId, UserPrincipal)`: Status auf SUBMITTED setzen, submittedAt setzen

#### FR-S7: Survey Controller
- `GET /api/v1/surveys/templates` - Alle aktiven Templates
- `POST /api/v1/surveys/instances` - Neue Instanz erstellen
- `GET /api/v1/surveys/instances` - Alle Instanzen des Users
- `GET /api/v1/surveys/instances/{id}` - Instanz-Details
- `PUT /api/v1/surveys/instances/{id}/answers` - Antworten speichern
- `POST /api/v1/surveys/instances/{id}/submit` - Instanz abschicken
- Security: `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER')")`

### Stakeholder-Management

#### FR-ST1: StakeholderGroup Entity
- Felder: id, name, icon (String), companyId (FK), impact (enum: HOCH, NIEDRIG, KRITISCH, STRATEGISCH), description (optional), createdAt, updatedAt
- JPA-Annotationen wie bestehende Entities

#### FR-ST2: StakeholderPerson Entity
- Felder: id, groupId (FK), name, role (String), email (optional), createdAt
- JPA-Annotationen wie bestehende Entities

#### FR-ST3: Stakeholder Repositories
- `StakeholderGroupRepository`: `findByCompanyId(Long companyId)`, `findByIdAndCompanyId(Long id, Long companyId)`
- `StakeholderPersonRepository`: `findByGroupId(Long groupId)`

#### FR-ST4: Stakeholder DTOs (Request)
- `StakeholderGroupCreateRequest`: name, icon, impact, description
- `StakeholderGroupUpdateRequest`: name, icon, impact, description (alle optional)
- `StakeholderPersonCreateRequest`: name, role, email (optional)

#### FR-ST5: Stakeholder Service Erweiterung
- `createGroup(CreateRequest, UserPrincipal)`: Neue Gruppe erstellen
- `updateGroup(Long groupId, UpdateRequest, UserPrincipal)`: Gruppe aktualisieren
- `addPerson(Long groupId, PersonCreateRequest, UserPrincipal)`: Person zu Gruppe hinzufügen
- Bestehende GET-Methoden mit echten Daten füllen (siehe FR-ST6)

#### FR-ST6: Stakeholder Service - Echte Daten
- `getGroups(UserPrincipal)`: Echte Gruppen aus Repository laden, Readiness berechnen (siehe Readiness-Berechnung)
- `getKpis(UserPrincipal)`: Aggregation aus allen Gruppen (total, promoters, neutrals, critics)
- `getGroupDetail(Long groupId, UserPrincipal)`: Echte Gruppen-Daten mit Readiness-Historie
- `getGroupPersons(Long groupId, UserPrincipal)`: Echte Personen aus Repository laden

#### FR-ST7: Stakeholder Controller Erweiterung
- `POST /api/v1/stakeholder/groups` - Neue Gruppe erstellen
- `PUT /api/v1/stakeholder/groups/{id}` - Gruppe aktualisieren
- `POST /api/v1/stakeholder/groups/{id}/persons` - Person hinzufügen
- Security: `@PreAuthorize("hasRole('COMPANY_ADMIN')")` für Schreiboperationen

### Readiness-Berechnung

#### FR-R1: ReadinessCalculationService Interface
- `calculateReadiness(List<SurveyAnswer> answers)`: Readiness aus Antworten berechnen
  - Formel: `((Durchschnitt - 1) / 4) * 100`
  - Durchschnitt aller Antwort-Werte (1-5)
- `calculatePromoterNeutralCritic(double readiness)`: Kategorisierung
  - Promoter: >= 75%
  - Neutral: 50-75%
  - Kritiker: < 50%
- `calculateTrend(double currentReadiness, double previousReadiness)`: Trend berechnen
  - Rückgabe: Integer (positiv = besser, negativ = schlechter, 0 = gleich)
- `calculateStatus(double readiness)`: Status berechnen
  - ready: >= 75%
  - attention: 50-75%
  - critical: < 50%

#### FR-R2: ReadinessCalculationServiceImpl
- Implementierung aller Methoden aus FR-R1
- Wiederverwendbar für Dashboard, Reporting, Stakeholder

### Datenaggregation

#### FR-D1: DashboardServiceImpl.getKpis() - Echte Daten
- `totalSurveys`: Anzahl aller SurveyInstances für Company
- `completedSurveys`: Anzahl SUBMITTED SurveyInstances
- `openSurveys`: Anzahl DRAFT SurveyInstances
- `totalStakeholders`: Anzahl aller StakeholderPersons in Gruppen der Company
- `promoters/neutrals/critics`: Aggregation aus Readiness-Berechnung aller StakeholderGroups
- `overallReadiness`: Durchschnittliche Readiness aller StakeholderGroups
- `activeMeasures`: Anzahl OPEN/IN_PROGRESS Measures (bereits implementiert via MeasureService)

#### FR-D2: DashboardServiceImpl.getTrendData() - Echte Daten
- Historische Readiness-Daten aus Survey-Ergebnissen
- Gruppierung nach Datum (z.B. täglich/wöchentlich)
- Vergleich aktueller Wert vs. Wert vor 30 Tagen für Trend-Berechnung
- Insight-Text generieren basierend auf Trend

#### FR-D3: ReportingServiceImpl - Echte Daten
- `getManagementSummary()`: Aggregation aus SurveyInstances, StakeholderGroups, Measures
- `getDepartmentReadiness()`: Readiness pro Department aus Survey-Antworten (nach department gruppieren)
- `getTrendData()`: Wiederverwendung von Dashboard-Logik

#### FR-D4: StakeholderServiceImpl - Echte Daten
- `getGroups()`: Echte Gruppen aus Repository, Readiness aus Survey-Antworten berechnen
- `getKpis()`: Aggregation aus allen Gruppen
- `getGroupDetail()`: Echte Gruppen-Daten mit Readiness-Historie (aus historischen Survey-Ergebnissen)
- `getGroupPersons()`: Echte Personen aus Repository

## Technical Considerations

### Company-Isolation
- Alle Queries filtern automatisch nach `companyId` des aktuellen Users (aus `UserPrincipal`)
- Repositories müssen `findByCompanyId` oder ähnliche Methoden unterstützen
- Services validieren Company-Zugehörigkeit vor Zugriff

### Survey Lifecycle
1. **Erstellung**: User erstellt Instanz → Status: DRAFT
2. **Autosave**: User speichert Antworten → Antworten werden gespeichert/aktualisiert
3. **Submit**: User sendet ab → Status: SUBMITTED, submittedAt gesetzt
4. **Readiness-Berechnung**: Nach Submit wird Readiness für betroffene StakeholderGroups aktualisiert

### Readiness-Berechnung Details
- Readiness wird für **StakeholderGroups** berechnet, nicht für einzelne Personen
- Berechnung basiert auf Survey-Antworten von Personen, die zu dieser Gruppe gehören
- Mapping: Person → Group via `StakeholderPerson.groupId`
- Person → Survey-Antworten via `SurveyInstance.userId` (User hat email, Person hat email)
- Oder: Person → User via email-Matching

### Datenmodell-Beziehungen
```
SurveyTemplate (1) → (N) SurveyInstance
SurveyInstance (1) → (N) SurveyAnswer
SurveyInstance.userId → User.id
SurveyInstance.companyId → Company.id
StakeholderGroup (1) → (N) StakeholderPerson
StakeholderGroup.companyId → Company.id
StakeholderPerson.email → User.email (optional, für Mapping)
```

### JSON-Struktur für SurveyTemplate.categoriesJson
```json
[
  {
    "name": "A) Kommunikation",
    "subcategories": [
      {
        "name": "1) Unterstützung",
        "questions": [
          {
            "id": "A1.1",
            "text": "Ich weiß, wer meine Ansprechpartner...",
            "onlyPMA": false,
            "reverse": false,
            "order": 1,
            "adkar": "AWARENESS" // optional
          }
        ]
      }
    ]
  }
]
```

## Implementation Order

1. **Survey Entities & Repositories** (1.0)
   - SurveyInstance Entity
   - SurveyAnswer Entity
   - SurveyTemplateRepository
   - SurveyInstanceRepository
   - SurveyAnswerRepository

2. **Survey Service & Controller** (2.0)
   - Survey DTOs
   - SurveyService Interface
   - SurveyServiceImpl
   - SurveyController

3. **Stakeholder Entities & Repositories** (3.0)
   - StakeholderGroup Entity
   - StakeholderPerson Entity
   - StakeholderGroupRepository
   - StakeholderPersonRepository

4. **Stakeholder Service Erweiterung** (4.0)
   - Stakeholder Request DTOs
   - StakeholderService erweitern
   - StakeholderServiceImpl erweitern
   - StakeholderController erweitern

5. **Readiness-Berechnungslogik** (5.0)
   - ReadinessCalculationService Interface
   - ReadinessCalculationServiceImpl

6. **Datenaggregation** (6.0)
   - DashboardServiceImpl mit echten Daten
   - ReportingServiceImpl mit echten Daten
   - StakeholderServiceImpl mit echten Daten

## Success Criteria

- ✅ User kann Survey starten, Antworten speichern, abschicken
- ✅ COMPANY_ADMIN kann Stakeholder-Gruppen erstellen/bearbeiten, Personen hinzufügen
- ✅ Readiness wird automatisch aus Survey-Antworten berechnet
- ✅ Dashboard zeigt echte KPIs und Trends
- ✅ Reporting zeigt echte Management Summary, Department Readiness, Trends
- ✅ Stakeholder-Seite zeigt echte Gruppen mit Readiness-Werten
- ✅ Keine Breaking Changes an bestehenden Endpoints
- ✅ Alle Endpoints sind company-isoliert

