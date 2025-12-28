# Task List: Backend End-to-End Data Flow Implementation

## Relevant Files

### Entities
- `backend/src/main/java/com/changeready/entity/SurveyTemplate.java` - Entity für Umfrage-Vorlagen mit Kategorien, Subkategorien und Fragen (inkl. ADKAR-Mapping)
- `backend/src/main/java/com/changeready/entity/SurveyInstance.java` - Entity für gestartete Umfragen mit Status DRAFT/SUBMITTED
- `backend/src/main/java/com/changeready/entity/SurveyAnswer.java` - Entity für einzelne Antworten auf Fragen (Likert 1-5)
- `backend/src/main/java/com/changeready/entity/StakeholderGroup.java` - Entity für Stakeholder-Gruppen (manuell angelegt)
- `backend/src/main/java/com/changeready/entity/StakeholderPerson.java` - Entity für Personen in Stakeholder-Gruppen
- `backend/src/main/java/com/changeready/entity/ReadinessSnapshot.java` - Optional: Entity für Readiness-Snapshots zur Performance-Optimierung

### Repositories
- `backend/src/main/java/com/changeready/repository/SurveyTemplateRepository.java` - Repository für SurveyTemplate mit Company-Filterung
- `backend/src/main/java/com/changeready/repository/SurveyInstanceRepository.java` - Repository für SurveyInstance mit findByUserIdAndCompanyId, findByCompanyIdAndStatus
- `backend/src/main/java/com/changeready/repository/SurveyAnswerRepository.java` - Repository für SurveyAnswer mit findByInstanceId
- `backend/src/main/java/com/changeready/repository/StakeholderGroupRepository.java` - Repository für StakeholderGroup mit findByCompanyId, findByIdAndCompanyId
- `backend/src/main/java/com/changeready/repository/StakeholderPersonRepository.java` - Repository für StakeholderPerson mit findByGroupId
- `backend/src/main/java/com/changeready/repository/ReadinessSnapshotRepository.java` - Optional: Repository für ReadinessSnapshot mit findByCompanyIdAndDate

### DTOs
- `backend/src/main/java/com/changeready/dto/survey/SurveyTemplateResponse.java` - Response DTO für Templates mit Kategorien-Struktur
- `backend/src/main/java/com/changeready/dto/survey/SurveyInstanceResponse.java` - Response DTO für Instanzen (Liste)
- `backend/src/main/java/com/changeready/dto/survey/SurveyInstanceDetailResponse.java` - Detail-Response mit Template-Daten und allen Antworten
- `backend/src/main/java/com/changeready/dto/survey/SurveyInstanceCreateRequest.java` - Request DTO für Instanz-Erstellung (templateId, participantType, department)
- `backend/src/main/java/com/changeready/dto/survey/SurveyAnswerUpdateRequest.java` - Request DTO für Antwort-Updates (Array von {questionId, value})
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupCreateRequest.java` - Request DTO für Gruppen-Erstellung
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderGroupUpdateRequest.java` - Request DTO für Gruppen-Updates
- `backend/src/main/java/com/changeready/dto/stakeholder/StakeholderPersonCreateRequest.java` - Request DTO für Personen-Erstellung

### Services
- `backend/src/main/java/com/changeready/service/SurveyService.java` - Service-Interface für Survey-Operationen (createInstance, getInstances, saveAnswers, submitInstance)
- `backend/src/main/java/com/changeready/service/SurveyServiceImpl.java` - Service-Implementierung für Survey-Logik mit Company-Isolation
- `backend/src/main/java/com/changeready/service/StakeholderService.java` - Bereits vorhanden, muss erweitert werden (createGroup, updateGroup, addPerson)
- `backend/src/main/java/com/changeready/service/StakeholderServiceImpl.java` - Bereits vorhanden, muss echte Daten zurückgeben und CRUD-Operationen implementieren
- `backend/src/main/java/com/changeready/service/DashboardService.java` - Bereits vorhanden, Interface bleibt gleich
- `backend/src/main/java/com/changeready/service/DashboardServiceImpl.java` - Bereits vorhanden, muss echte Datenaggregation implementieren
- `backend/src/main/java/com/changeready/service/ReportingService.java` - Bereits vorhanden, Interface bleibt gleich
- `backend/src/main/java/com/changeready/service/ReportingServiceImpl.java` - Bereits vorhanden, muss echte Datenaggregation implementieren
- `backend/src/main/java/com/changeready/service/ReadinessCalculationService.java` - Neuer Service für Readiness-Berechnungen (calculateReadiness, calculatePromoterNeutralCritic, calculateTrend)
- `backend/src/main/java/com/changeready/service/ReadinessCalculationServiceImpl.java` - Implementierung der Readiness-Logik

### Controllers
- `backend/src/main/java/com/changeready/controller/SurveyController.java` - Controller für Survey-Endpoints (POST /instances, GET /instances, GET /instances/{id}, PUT /instances/{id}/answers, POST /instances/{id}/submit, GET /templates)
- `backend/src/main/java/com/changeready/controller/StakeholderController.java` - Bereits vorhanden, muss erweitert werden (POST /groups, PUT /groups/{id}, POST /groups/{id}/persons)

### Notes
- Alle neuen Entities müssen JPA-Annotationen verwenden und dem bestehenden Pattern folgen (User.java als Referenz: @Entity, @Table, @PrePersist, @PreUpdate)
- Repositories müssen JpaRepository erweitern und Company-Isolation unterstützen (UserRepository als Referenz)
- Services müssen bestehende Patterns befolgen (CompanyServiceImpl als Referenz: Constructor-Injection, @Service)
- Controllers müssen bestehende Security-Patterns verwenden (@PreAuthorize, UserPrincipal aus SecurityContext)
- DTOs müssen Lombok-Annotationen verwenden (@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)
- Company-Isolation: Alle Queries filtern automatisch nach companyId des aktuellen Users

## Tasks

- [ ] 1.0 Survey Entities & Repositories erstellen
  - [x] 1.1 SurveyTemplate Entity erstellen: Felder (id, name, description, version, active, categories), ADKAR-Mapping unterstützen
  - [ ] 1.2 SurveyInstance Entity erstellen: Felder (id, templateId, userId, companyId, participantType, department, status enum DRAFT/SUBMITTED, timestamps)
  - [ ] 1.3 SurveyAnswer Entity erstellen: Felder (id, instanceId, questionId, value 1-5, timestamps)
  - [ ] 1.4 SurveyTemplateRepository erstellen: findByActive, findByCompanyId (falls company-spezifisch)
  - [ ] 1.5 SurveyInstanceRepository erstellen: findByUserIdAndCompanyId, findByCompanyIdAndStatus, findByTemplateIdAndCompanyId
  - [ ] 1.6 SurveyAnswerRepository erstellen: findByInstanceId, save mit Update-Logik
- [ ] 2.0 Survey Service & Controller implementieren
  - [ ] 2.1 Survey DTOs erstellen: SurveyTemplateResponse, SurveyInstanceResponse, SurveyInstanceDetailResponse, CreateRequest, AnswerUpdateRequest
  - [ ] 2.2 SurveyService Interface erstellen: createInstance, getInstances, getInstance, saveAnswers, submitInstance, getTemplates
  - [ ] 2.3 SurveyServiceImpl implementieren: Alle Methoden mit Company-Isolation und Validierung
  - [ ] 2.4 SurveyController erstellen: POST /instances, GET /instances, GET /instances/{id}, PUT /instances/{id}/answers, POST /instances/{id}/submit, GET /templates
  - [ ] 2.5 Security & Validierung: @PreAuthorize für COMPANY_USER, Validierung von Requests
- [ ] 3.0 Stakeholder Entities & Repositories erstellen
  - [ ] 3.1 StakeholderGroup Entity erstellen: Felder (id, name, icon, companyId, impact enum, description, timestamps)
  - [ ] 3.2 StakeholderPerson Entity erstellen: Felder (id, groupId, name, role, email optional, createdAt)
  - [ ] 3.3 StakeholderGroupRepository erstellen: findByCompanyId, findByIdAndCompanyId
  - [ ] 3.4 StakeholderPersonRepository erstellen: findByGroupId
- [ ] 4.0 Stakeholder Service erweitern & Controller erweitern
  - [ ] 4.1 Stakeholder DTOs erstellen: StakeholderGroupCreateRequest, StakeholderGroupUpdateRequest, StakeholderPersonCreateRequest
  - [ ] 4.2 StakeholderService erweitern: createGroup, updateGroup, addPerson Methoden hinzufügen
  - [ ] 4.3 StakeholderServiceImpl erweitern: CRUD-Operationen implementieren, bestehende GET-Methoden mit echten Daten füllen
  - [ ] 4.4 StakeholderController erweitern: POST /groups, PUT /groups/{id}, POST /groups/{id}/persons Endpoints hinzufügen
  - [ ] 4.5 Security: @PreAuthorize für COMPANY_ADMIN bei Schreiboperationen
- [ ] 5.0 Readiness-Berechnungslogik implementieren
  - [ ] 5.1 ReadinessCalculationService Interface erstellen: calculateReadiness, calculatePromoterNeutralCritic, calculateTrend, calculateStatus
  - [ ] 5.2 ReadinessCalculationServiceImpl implementieren: Readiness aus Survey-Antworten berechnen (Formel: ((Durchschnitt - 1) / 4) * 100)
  - [ ] 5.3 Promoter/Neutral/Kritiker-Logik: Automatische Kategorisierung basierend auf Durchschnittswerten
  - [ ] 5.4 Trend-Berechnung: Vergleich aktueller Wert mit Wert von vor 30 Tagen
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
- [ ] 7.0 Performance-Optimierung & Aggregationen
  - [ ] 7.1 ReadinessSnapshot Entity erstellen (optional): Felder (date, companyId, overallReadiness, departmentReadiness JSON)
  - [ ] 7.2 ReadinessSnapshotRepository erstellen (optional): findByCompanyIdAndDate, findByCompanyIdOrderByDateDesc
  - [ ] 7.3 Snapshot-Erstellung nach Survey-Submit: Automatische Aktualisierung nach Submit
  - [ ] 7.4 Trend-Berechnung optimieren: Nutzung von Snapshots falls vorhanden, sonst direkte Berechnung
  - [ ] 7.5 Empty States sicherstellen: Alle Services geben korrekte leere Werte zurück wenn keine Daten vorhanden
