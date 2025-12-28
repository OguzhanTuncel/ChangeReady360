# Backend-Inventar: ChangeReady360

## Entities

### Bestehend
- `User.java` - Benutzer-Entity (id, email, passwordHash, role, company, active, timestamps)
- `Company.java` - Company-Entity (id, name, active, timestamps)
- `Measure.java` - Maßnahmen-Entity (id, title, description, status enum, company, timestamps)
- `SurveyTemplate.java` - Umfrage-Vorlagen-Entity (id, name, description, version, active, categoriesJson, company, timestamps) **[GERADE ERSTELLT]**
- `CompanyAccessRequest.java` - Company-Zugriffsanfragen
- `Invite.java` - Einladungen
- `Department.java` - Enum (EINKAUF, VERTRIEB, LAGER_LOGISTIK, IT, GESCHAEFTSFUEHRUNG)
- `Role.java` - Enum (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER)

### Fehlend
- `SurveyInstance.java` - Gestartete Umfragen (DRAFT/SUBMITTED Status)
- `SurveyAnswer.java` - Einzelne Antworten auf Fragen (Likert 1-5)
- `StakeholderGroup.java` - Stakeholder-Gruppen (manuell angelegt)
- `StakeholderPerson.java` - Personen in Stakeholder-Gruppen

## Repositories

### Bestehend
- `UserRepository.java` - findByEmail, findByCompanyId, etc.
- `CompanyRepository.java` - Standard CRUD
- `MeasureRepository.java` - findByCompanyIdAndStatus, findByCompanyIdAndStatusIn
- `CompanyAccessRequestRepository.java`
- `InviteRepository.java`

### Fehlend
- `SurveyTemplateRepository.java` - findByActive, findByCompanyId
- `SurveyInstanceRepository.java` - findByUserIdAndCompanyId, findByCompanyIdAndStatus, findByTemplateIdAndCompanyId
- `SurveyAnswerRepository.java` - findByInstanceId
- `StakeholderGroupRepository.java` - findByCompanyId, findByIdAndCompanyId
- `StakeholderPersonRepository.java` - findByGroupId

## Controllers & Endpoints

### Bestehend

#### AuthController (`/api/v1/auth`)
- `POST /login` - Login
- `POST /logout` - Logout

#### DashboardController (`/api/v1/dashboard`)
- `GET /kpis` - Dashboard KPIs (TODO: echte Daten)
- `GET /trends` - Trend-Daten (TODO: echte Daten)

#### StakeholderController (`/api/v1/stakeholder`)
- `GET /groups` - Alle Gruppen (TODO: echte Daten)
- `GET /kpis` - Stakeholder KPIs (TODO: echte Daten)
- `GET /groups/{id}` - Gruppen-Details (TODO: echte Daten)
- `GET /groups/{id}/persons` - Personen einer Gruppe (TODO: echte Daten)

#### ReportingController (`/api/v1/reporting`)
- `GET /data` - Alle Reporting-Daten (TODO: echte Daten)
- `GET /summary` - Management Summary (TODO: echte Daten)
- `GET /departments` - Abteilungs-Readiness (TODO: echte Daten)
- `GET /trends` - Trend-Daten (TODO: echte Daten)

#### MeasureController (`/api/v1/measures`)
- `GET /open` - Aktive Maßnahmen (implementiert)

#### UserController (`/api/v1/admin/users`)
- `POST /` - User erstellen
- `POST /company-admin` - Company-Admin erstellen
- `GET /` - Alle Users
- `GET /{id}` - User-Details
- `PUT /{id}` - User aktualisieren

#### CompanyController (`/api/v1/admin/companies`)
- `POST /` - Company erstellen
- `GET /` - Alle Companies
- `GET /{id}` - Company-Details
- `PUT /{id}` - Company aktualisieren

#### CompanyAccessRequestController (`/api/v1/company-access-requests`)
- `POST /` - Anfrage erstellen
- `GET /` - Alle Anfragen
- `GET /status/{status}` - Anfragen nach Status
- `GET /{id}` - Anfrage-Details
- `PUT /{id}` - Anfrage aktualisieren

### Fehlend
- **SurveyController** (`/api/v1/surveys`)
  - `GET /templates` - Alle aktiven Templates
  - `POST /instances` - Neue Instanz erstellen
  - `GET /instances` - Alle Instanzen des Users
  - `GET /instances/{id}` - Instanz-Details mit Template und Antworten
  - `PUT /instances/{id}/answers` - Antworten speichern (Autosave)
  - `POST /instances/{id}/submit` - Instanz abschicken

- **StakeholderController** (Erweiterung)
  - `POST /groups` - Neue Gruppe erstellen (COMPANY_ADMIN)
  - `PUT /groups/{id}` - Gruppe aktualisieren (COMPANY_ADMIN)
  - `POST /groups/{id}/persons` - Person zu Gruppe hinzufügen (COMPANY_ADMIN)

## Services

### Bestehend (mit TODOs)
- `DashboardService/Impl` - getKpis(), getTrendData() - **TODO: echte Datenaggregation**
- `StakeholderService/Impl` - getGroups(), getKpis(), getGroupDetail(), getGroupPersons() - **TODO: echte Daten**
- `ReportingService/Impl` - getReportingData(), getManagementSummary(), getDepartmentReadiness(), getTrendData() - **TODO: echte Datenaggregation**

### Bestehend (implementiert)
- `AuthService/Impl` - Login/Logout
- `CompanyService/Impl` - Company CRUD
- `UserService/Impl` - User CRUD
- `CompanyAccessRequestService/Impl` - Request CRUD
- `MeasureService/Impl` - getActiveMeasures() - **implementiert**

### Fehlend
- `SurveyService/Impl` - Survey-Operationen (createInstance, getInstances, getInstance, saveAnswers, submitInstance, getTemplates)
- `ReadinessCalculationService/Impl` - Readiness-Berechnungslogik (calculateReadiness, calculatePromoterNeutralCritic, calculateTrend, calculateStatus)

## DTOs

### Bestehend
- `dashboard/DashboardKpisResponse.java` - KPIs für Dashboard
- `dashboard/TrendDataResponse.java` - Trend-Daten
- `dashboard/TrendDataPointResponse.java` - Einzelner Trend-Punkt
- `stakeholder/StakeholderGroupResponse.java` - Gruppen-Response
- `stakeholder/StakeholderGroupDetailResponse.java` - Gruppen-Details
- `stakeholder/StakeholderPersonResponse.java` - Personen-Response
- `stakeholder/StakeholderKpisResponse.java` - Stakeholder KPIs
- `stakeholder/ReadinessHistoryPointResponse.java` - Historische Readiness-Punkte
- `reporting/ReportingDataResponse.java` - Alle Reporting-Daten
- `reporting/ManagementSummaryResponse.java` - Management Summary
- `reporting/DepartmentReadinessResponse.java` - Abteilungs-Readiness
- `measure/MeasureResponse.java` - Maßnahmen-Response
- `user/UserResponse.java`, `UserCreateRequest.java`, `UserUpdateRequest.java`
- `company/CompanyResponse.java`, `CompanyRequest.java`
- `auth/LoginRequest.java`, `LoginResponse.java`
- `companyaccessrequest/*` - Request/Response DTOs
- `invite/*` - Invite DTOs
- `error/ErrorResponse.java`

### Fehlend
- `survey/SurveyTemplateResponse.java` - Template-Response mit Kategorien-Struktur
- `survey/SurveyInstanceResponse.java` - Instanz-Response (Liste)
- `survey/SurveyInstanceDetailResponse.java` - Instanz-Details mit Template und Antworten
- `survey/SurveyInstanceCreateRequest.java` - Instanz-Erstellung (templateId, participantType, department)
- `survey/SurveyAnswerUpdateRequest.java` - Antwort-Updates (Array von {questionId, value})
- `stakeholder/StakeholderGroupCreateRequest.java` - Gruppen-Erstellung
- `stakeholder/StakeholderGroupUpdateRequest.java` - Gruppen-Update
- `stakeholder/StakeholderPersonCreateRequest.java` - Personen-Erstellung

## Frontend-Erwartungen

### SurveyService (Frontend)
- `getTemplates()` → `GET /api/v1/surveys/templates`
- `getActiveTemplates()` → `GET /api/v1/surveys/templates?active=true`
- `createInstance(templateId, participantType, department)` → `POST /api/v1/surveys/instances`
- `getInstance(id)` → `GET /api/v1/surveys/instances/{id}`
- `getUserInstances()` → `GET /api/v1/surveys/instances`
- `saveAnswer(instanceId, questionId, value)` → `PUT /api/v1/surveys/instances/{id}/answers`
- `submitInstance(instanceId)` → `POST /api/v1/surveys/instances/{id}/submit`

### DashboardService (Frontend)
- `getDashboardData()` → `GET /api/v1/dashboard/kpis` + `GET /api/v1/dashboard/trends` + `GET /api/v1/stakeholder/groups`
- Erwartet: `DashboardKpisResponse` mit `overallReadiness`, `readinessTrend`, `totalStakeholders`, `stakeholderGroupsCount`, `criticsCount`, `openMeasuresCount`, `overdueMeasuresCount`

### StakeholderService (Frontend)
- `getGroups()` → `GET /api/v1/stakeholder/groups` ✅
- `getGroupDetail(groupId)` → `GET /api/v1/stakeholder/groups/{id}` ✅
- `getKpis()` → `GET /api/v1/stakeholder/kpis` ✅
- `getGroupPersons(groupId)` → `GET /api/v1/stakeholder/groups/{id}/persons` ✅
- **Fehlend**: `addStakeholder()` → `POST /api/v1/stakeholder/groups` (Frontend erwartet diese Methode)

### ReportingService (Frontend)
- `getReportingData()` → `GET /api/v1/reporting/data` ✅
- `getManagementSummary()` → `GET /api/v1/reporting/summary` ✅
- `getDepartmentReadiness()` → `GET /api/v1/reporting/departments` ✅
- `getTrendData()` → `GET /api/v1/reporting/trends` ✅

