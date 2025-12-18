# Test-Ausführungs-Anleitung

## Übersicht

Diese Anleitung zeigt, wie Sie alle Tests im Projekt ausführen können.

## Test-Befehle

### Alle Tests ausführen

```bash
cd backend
./gradlew test
```

### Tests mit Details ausführen

```bash
# Mit mehr Informationen
./gradlew test --info

# Mit Debug-Output
./gradlew test --debug

# Tests neu kompilieren und ausführen
./gradlew clean test
```

### Nur bestimmte Tests ausführen

```bash
# Nur eine Test-Klasse
./gradlew test --tests "CompanyAccessRequestServiceTest"

# Nur eine Test-Methode
./gradlew test --tests "CompanyAccessRequestServiceTest.create_ShouldSaveAndReturnResponse"

# Alle Tests in einem Package
./gradlew test --tests "com.changeready.service.*"
```

### Test-Report anzeigen

Nach dem Ausführen der Tests können Sie den HTML-Report öffnen:

**Windows:**
```bash
Invoke-Item build\reports\tests\test\index.html
```

**Linux/Mac:**
```bash
open build/reports/tests/test/index.html
```

## Verfügbare Test-Klassen

### 1. BackendApplicationTests
- **Zweck:** Prüft, ob der Spring Application Context erfolgreich lädt
- **Laufzeit:** ~2-3 Sekunden

### 2. CompanyAccessRequestServiceTest
- **Zweck:** Unit-Tests für die CompanyAccessRequestService-Logik
- **Anzahl Tests:** 9 Tests
- **Testet:**
  - ✅ Erstellen von Anfragen
  - ✅ Finden von Anfragen (by ID, by Status, all)
  - ✅ Validierung (Rejection ohne Reason)
  - ✅ Status-Änderungen (Approve/Reject)
  - ✅ Bereits bearbeitete Anfragen können nicht geändert werden

## Test-Konfiguration

Die Tests verwenden eine **H2 In-Memory-Datenbank** (keine PostgreSQL-Verbindung nötig).

Konfiguration: `src/test/resources/application-test.properties`

## Beispiel-Ausgabe

```
> Task :test

BackendApplicationTests > contextLoads() PASSED
CompanyAccessRequestServiceTest > create_ShouldSaveAndReturnResponse() PASSED
CompanyAccessRequestServiceTest > findById_WhenExists_ShouldReturnResponse() PASSED
CompanyAccessRequestServiceTest > findById_WhenNotExists_ShouldThrowException() PASSED
CompanyAccessRequestServiceTest > update_WhenRejectingWithoutReason_ShouldThrowValidationException() PASSED
CompanyAccessRequestServiceTest > update_WhenRejectingWithReason_ShouldUpdateSuccessfully() PASSED
CompanyAccessRequestServiceTest > update_WhenAlreadyProcessed_ShouldThrowValidationException() PASSED
CompanyAccessRequestServiceTest > update_WhenApproving_ShouldUpdateSuccessfully() PASSED
CompanyAccessRequestServiceTest > findAll_ShouldReturnAllRequests() PASSED
CompanyAccessRequestServiceTest > findByStatus_ShouldReturnFilteredRequests() PASSED

BUILD SUCCESSFUL in 23s
10 tests completed, 10 passed
```

## Troubleshooting

### Problem: Tests schlagen fehl mit Datenbank-Fehler

**Lösung:** Die Tests sollten automatisch H2 verwenden. Prüfen Sie `application-test.properties`.

### Problem: "Class not found" Fehler

**Lösung:** 
```bash
./gradlew clean test
```

### Problem: Tests laufen zu langsam

**Lösung:** 
- Verwenden Sie `--no-daemon` nur wenn nötig
- Normalerweise: `./gradlew test` (mit Daemon ist schneller)

## Nächste Schritte

Um weitere Tests hinzuzufügen:

1. Erstellen Sie eine neue Test-Klasse in `src/test/java/com/changeready/`
2. Verwenden Sie JUnit 5 (`@Test`, `@BeforeEach`, etc.)
3. Verwenden Sie Mockito für Mocking (`@Mock`, `@InjectMocks`)
4. Führen Sie Tests aus mit: `./gradlew test`

## Best Practices

- ✅ Jede Service-Klasse sollte eine Test-Klasse haben
- ✅ Test-Methoden sollten aussagekräftige Namen haben (z.B. `methodName_WhenCondition_ShouldExpectedResult`)
- ✅ Tests sollten isoliert sein (keine Abhängigkeiten zwischen Tests)
- ✅ Verwenden Sie Mocking für externe Dependencies
- ✅ Testen Sie sowohl Erfolgs- als auch Fehlerfälle

