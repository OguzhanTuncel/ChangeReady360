# Task List: Backend Gap-Only Implementation

## Relevant Files

### Entities (Neu zu erstellen)
- `backend/src/main/java/com/changeready/entity/SurveyInstance.java` - Entity für gestartete Umfragen mit Status DRAFT/SUBMITTED
- `backend/src/main/java/com/changeready/entity/SurveyAnswer.java` - Entity für einzelne Antworten auf Fragen (Likert 1-5)
- `backend/src/main/java/com/changeready/entity/StakeholderGroup.java` - Entity für Stakeholder-Gruppen (manuell angelegt)
- `backend/src/main/java/com/changeready/entity/StakeholderPerson.java` - Entity für Personen in Stakeholder-Gruppen

### Entities (Bereits vorhanden - NICHT ändern)
- `backend/src/main/java/com/changeready/entity/SurveyTemplate.java` - ✅ Bereits vorhanden (gerade erstellt)

### Repositories (Neu zu erstellen)
- `backend/src/main/java/com/changeready/repository/SurveyTemplateRepository.java` - Repository für SurveyTemplate mit findByActive
- `backend/src/main/java/com/changeready/repository/SurveyInstanceRepository.java` - Repository für SurveyInstance mit findByUserIdAndCompanyId, findByCompanyIdAndStatus
- `backend/src/main/java/com/changeready/repository/SurveyAnswerRepository.java` - Repository für SurveyAnswer mit findByInstanceId
- `backend/src/main/java/com/changeready/repository/StakeholderGroupRepository.java` - Repository für StakeholderGroup mit findByCompanyId, findByIdAndCompanyId
- `backend/src/main/java/com/changeready/repository/StakeholderPersonRepository.java` - Repository für StakeholderPerson mit findByGroupId

### DTOs (Neu zu erstellen)
- `backend/src/main/java/com/changeready/dto/survey/SurveyTemplateResponse.java` - Response DTO für Templates mit Kategorien-Struktur (aus JSON parsen)
- `backend/src/main/java/com/changeready/dto/survey/SurveyInstanceResponse.java` - Response DTO für Instanzen (Liste)
- `backend/src/main/java/com/changeready/dto/survey/SurveyInstanceDetailResponse.java` - Detail-Response mit Template-Daten und allen Antworten
- `backend/src/main/java/com/changeready/dto/survey/SurveyInstanceCreateRequest.java` - Request DTO für Instanz-Erstellung (templateId, participantType, department)
- `backend/src/main/java/com/changeready/dto/survey/SurveyAnswerUpdateRequest.java` - Request DTO für Antwort-Updates (Array von {questionId, value})
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupCreateRequest.java` - Request DTO für Gruppen-Erstellung
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupUpdateRequest.java` - Request DTO für Gruppen-Updates
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderPersonCreateRequest.java` - Request DTO für Personen-Erstellung

### DTOs (Bereits vorhanden - NICHT ändern)
- `backend/src/main/java/com/changeready/dto/dashboard/DashboardKpisResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/dashboard/TrendDataResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/dashboard/TrendDataPointResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupDetailResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderPersonResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderKpisResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/stakeholder/ReadinessHistoryPointResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/reporting/ReportingDataResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/reporting/ManagementSummaryResponse.java` - ✅ Bereits vorhanden
- `backend/src/main/java/com/changeready/dto/reporting/DepartmentReadinessResponse.java` - ✅ Bereits vorhanden

### Services (Neu zu erstellen)
- `backend/src/main/java/com/changeready/service/SurveyService.java` - Service-Interface für Survey-Operationen (createInstance, getInstances, getInstance, saveAnswers, submitInstance, getTemplates)
- `backend/src/main/java/com/changeready/service/SurveyServiceImpl.java` - Service-Implementierung für Survey-Logik mit Company-Isolation
- `backend/src/main/java/com/changeready/service/ReadinessCalculationService.java` - Service-Interface für Readiness-Berechnungen (calculateReadiness, calculatePromoterNeutralCritic, calculateTrend, calculateStatus)
- `backend/src/main/java/com/changeready/service/ReadinessCalculationServiceImpl.java` - Implementierung der Readiness-Logik

### Services (Bereits vorhanden - ERWEITERN, nicht neu erstellen)
- `backend/src/main/java/com/changeready/service/StakeholderService.java` - ✅ Bereits vorhanden, muss erweitert werden (createGroup, updateGroup, addPerson)
- `backend/src/main/java/com/changeready/service/StakeholderServiceImpl.java` - ✅ Bereits vorhanden, muss echte Daten zurückgeben und CRUD-Operationen implementieren
- `backend/src/main/java/com/changeready/service/DashboardService.java` - ✅ Bereits vorhanden, Interface bleibt gleich
- `backend/src/main/java/com/changeready/service/DashboardServiceImpl.java` - ✅ Bereits vorhanden, muss echte Datenaggregation implementieren
- `backend/src/main/java/com/changeready/service/ReportingService.java` - ✅ Bereits vorhanden, Interface bleibt gleich
- `backend/src/main/java/com/changeready/service/ReportingServiceImpl.java` - ✅ Bereits vorhanden, muss echte Datenaggregation implementieren

### Controllers (Neu zu erstellen)
- `backend/src/main/java/com/changeready/controller/SurveyController.java` - Controller für Survey-Endpoints (POST /instances, GET /instances, GET /instances/{id}, PUT /instances/{id}/answers, POST /instances/{id}/submit, GET /templates)

### Controllers (Bereits vorhanden - ERWEITERN, nicht neu erstellen)
- `backend/src/main/java/com/changeready/controller/StakeholderController.java` - ✅ Bereits vorhanden, muss erweitert werden (POST /groups, PUT /groups/{id}, POST /groups/{id}/persons)

### Notes
- Alle neuen Entities müssen JPA-Annotationen verwenden und dem bestehenden Pattern folgen (User.java als Referenz: @Entity, @Table, @PrePersist, @PreUpdate)
- Repositories müssen JpaRepository erweitern und Company-Isolation unterstützen (UserRepository als Referenz)
- Services müssen bestehende Patterns befolgen (CompanyServiceImpl als Referenz: Constructor-Injection, @Service)
- Controllers müssen bestehende Security-Patterns verwenden (@PreAuthorize, UserPrincipal aus SecurityContext)
- DTOs müssen Lombok-Annotationen verwenden (@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)
- Company-Isolation: Alle Queries filtern automatisch nach companyId des aktuellen Users
- **KEINE Breaking Changes**: Bestehende Endpoints/Signaturen bleiben unverändert
- **NUR additive Änderungen**: Neue Endpoints/DTOs/Felder hinzufügen, nichts löschen/umbenennen

## Tasks

- [x] 1.0 Survey Entities & Repositories erstellen
  - [x] 1.1 SurveyInstance Entity erstellen: Felder (id, templateId FK, userId FK, companyId FK, participantType enum, department enum, status enum DRAFT/SUBMITTED, timestamps, submittedAt optional)
  - [x] 1.2 SurveyAnswer Entity erstellen: Felder (id, instanceId FK, questionId String, value Integer 1-5, timestamps)
  - [x] 1.3 SurveyTemplateRepository erstellen: findByActive(Boolean active)
  - [x] 1.4 SurveyInstanceRepository erstellen: findByUserIdAndCompanyId, findByCompanyIdAndStatus, findByTemplateIdAndCompanyId
  - [x] 1.5 SurveyAnswerRepository erstellen: findByInstanceId(Long instanceId)
- [x] 2.0 Survey Service & Controller implementieren
  - [x] 2.1 Survey DTOs erstellen: SurveyTemplateResponse (JSON parsen), SurveyInstanceResponse, SurveyInstanceDetailResponse, CreateRequest, AnswerUpdateRequest
  - [x] 2.2 SurveyService Interface erstellen: createInstance, getInstances, getInstance, saveAnswers, submitInstance, getTemplates
  - [x] 2.3 SurveyServiceImpl implementieren: Alle Methoden mit Company-Isolation und Validierung
  - [x] 2.4 SurveyController erstellen: POST /instances, GET /instances, GET /instances/{id}, PUT /instances/{id}/answers, POST /instances/{id}/submit, GET /templates
  - [x] 2.5 Security & Validierung: @PreAuthorize für COMPANY_USER, Validierung von Requests
- [x] 3.0 Stakeholder Entities & Repositories erstellen
  - [x] 3.1 StakeholderGroup Entity erstellen: Felder (id, name, icon String, companyId FK, impact enum, description optional, timestamps)
  - [x] 3.2 StakeholderPerson Entity erstellen: Felder (id, groupId FK, name, role String, email optional, createdAt)
  - [x] 3.3 StakeholderGroupRepository erstellen: findByCompanyId, findByIdAndCompanyId
  - [x] 3.4 StakeholderPersonRepository erstellen: findByGroupId
- [ ] 4.0 Stakeholder Service erweitern & Controller erweitern
  - [ ] 4.1 Stakeholder Request DTOs erstellen: StakeholderGroupCreateRequest, StakeholderGroupUpdateRequest, StakeholderPersonCreateRequest
  - [ ] 4.2 StakeholderService Interface erweitern: createGroup, updateGroup, addPerson Methoden hinzufügen
  - [ ] 4.3 StakeholderServiceImpl erweitern: CRUD-Operationen implementieren (createGroup, updateGroup, addPerson)
  - [ ] 4.4 StakeholderServiceImpl erweitern: Bestehende GET-Methoden mit echten Daten füllen (getGroups, getKpis, getGroupDetail, getGroupPersons)
  - [ ] 4.5 StakeholderController erweitern: POST /groups, PUT /groups/{id}, POST /groups/{id}/persons Endpoints hinzufügen
  - [ ] 4.6 Security: @PreAuthorize für COMPANY_ADMIN bei Schreiboperationen
- [ ] 5.0 Readiness-Berechnungslogik implementieren
  - [ ] 5.1 ReadinessCalculationService Interface erstellen: calculateReadiness, calculatePromoterNeutralCritic, calculateTrend, calculateStatus
  - [ ] 5.2 ReadinessCalculationServiceImpl implementieren: Readiness aus Survey-Antworten berechnen (Formel: ((Durchschnitt - 1) / 4) * 100)
  - [ ] 5.3 Promoter/Neutral/Kritiker-Logik: Automatische Kategorisierung basierend auf Readiness-Werten (Promoter >=75%, Neutral 50-75%, Kritiker <50%)
  - [ ] 5.4 Trend-Berechnung: Vergleich aktueller Wert mit Wert von vor 30 Tagen (Integer: positiv/negativ/0)
  - [ ] 5.5 Status-Berechnung: ready (>=75%), attention (50-75%), critical (<50%)
- [ ] 6.0 Dashboard & Reporting Services mit echten Daten implementieren
  - [ ] 6.1 DashboardServiceImpl.getKpis implementieren: Echte Datenaggregation aus SurveyInstances, StakeholderGroups, Measures
  - [ ] 6.2 DashboardServiceImpl.getTrendData implementieren: Historische Readiness-Daten aus Survey-Ergebnissen
  - [ ] 6.3 ReportingServiceImpl.getManagementSummary implementieren: Echte Datenaggregation
  - [ ] 6.4 ReportingServiceImpl.getDepartmentReadiness implementieren: Readiness pro Department aus Survey-Antworten
  - [ ] 6.5 ReportingServiceImpl.getTrendData implementieren: Wiederverwendung von Dashboard-Logik
  - [ ] 6.6 StakeholderServiceImpl.getGroups implementieren: Echte Gruppen-Daten mit berechneten Readiness-Werten
  - [ ] 6.7 StakeholderServiceImpl.getKpis implementieren: Aggregation aus allen Gruppen
  - [ ] 6.8 StakeholderServiceImpl.getGroupDetail implementieren: Detail-Daten mit Readiness-Historie
  - [ ] 6.9 StakeholderServiceImpl.getGroupPersons implementieren: Echte Personen-Daten aus Repository

