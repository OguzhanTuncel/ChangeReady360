# Test-Anleitung: Company Access Requests

## Übersicht

Diese Anleitung zeigt, wie Sie die neue Funktionalität "Zugangsanfrage für Unternehmen" testen können.

## Voraussetzungen

1. **Datenbank läuft** (PostgreSQL)
2. **Backend läuft** (Spring Boot auf Port 8080)
3. **Initial Admin User** wurde erstellt (automatisch beim ersten Start)

---

## Schritt 1: Anwendung starten

### Option A: Mit Docker Compose (empfohlen)

```bash
# Im Projekt-Root-Verzeichnis
docker-compose up --build
```

Dies startet:
- PostgreSQL auf Port 5432
- Backend auf Port 8080
- Frontend auf Port 4200

### Option B: Nur Backend lokal (wenn PostgreSQL bereits läuft)

```bash
cd backend
./gradlew bootRun
```

**Wichtig:** Stellen Sie sicher, dass PostgreSQL läuft und die Datenbank `changeready360` existiert.

---

## Schritt 2: Initial Admin Login

Bevor Sie die geschützten Endpoints testen können, müssen Sie sich als SYSTEM_ADMIN einloggen.

### Login-Request

**Endpoint:** `POST http://localhost:8080/api/v1/auth/login`

**Request Body:**
```json
{
  "email": "admin@changeready360.com",
  "password": "Admin123!"
}
```

### Beispiel mit curl:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@changeready360.com",
    "password": "Admin123!"
  }'
```

### Erwartete Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userInfo": {
    "id": 1,
    "email": "admin@changeready360.com",
    "role": "SYSTEM_ADMIN",
    "companyId": 1
  }
}
```

**Wichtig:** Kopieren Sie das `token` für die folgenden Requests!

---

## Schritt 3: Öffentliche Anfrage erstellen (ohne Login)

Dieser Endpoint ist öffentlich - jeder kann eine Zugangsanfrage stellen.

### Request

**Endpoint:** `POST http://localhost:8080/api/v1/company-access-requests`

**Request Body:**
```json
{
  "companyName": "Beispiel GmbH",
  "contactName": "Max Mustermann",
  "contactEmail": "max@beispiel.de",
  "contactPhone": "+49 123 456789",
  "message": "Wir möchten Zugang zur Plattform erhalten."
}
```

### Beispiel mit curl:

```bash
curl -X POST http://localhost:8080/api/v1/company-access-requests \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Beispiel GmbH",
    "contactName": "Max Mustermann",
    "contactEmail": "max@beispiel.de",
    "contactPhone": "+49 123 456789",
    "message": "Wir möchten Zugang zur Plattform erhalten."
  }'
```

### Erwartete Response (201 Created):

```json
{
  "id": 1,
  "companyName": "Beispiel GmbH",
  "contactName": "Max Mustermann",
  "contactEmail": "max@beispiel.de",
  "contactPhone": "+49 123 456789",
  "message": "Wir möchten Zugang zur Plattform erhalten.",
  "status": "PENDING",
  "processedBy": null,
  "processedAt": null,
  "rejectionReason": null,
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00"
}
```

**Wichtig:** Notieren Sie sich die `id` für weitere Tests!

---

## Schritt 4: Alle Anfragen abrufen (SYSTEM_ADMIN)

### Request

**Endpoint:** `GET http://localhost:8080/api/v1/company-access-requests`

**Header:** `Authorization: Bearer <token>` (vom Login)

### Beispiel mit curl:

```bash
# Setzen Sie TOKEN auf das Token vom Login
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/v1/company-access-requests \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### Erwartete Response (200 OK):

```json
[
  {
    "id": 1,
    "companyName": "Beispiel GmbH",
    "contactName": "Max Mustermann",
    "contactEmail": "max@beispiel.de",
    "contactPhone": "+49 123 456789",
    "message": "Wir möchten Zugang zur Plattform erhalten.",
    "status": "PENDING",
    "processedBy": null,
    "processedAt": null,
    "rejectionReason": null,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
]
```

---

## Schritt 5: Anfrage nach Status filtern (SYSTEM_ADMIN)

### Request

**Endpoint:** `GET http://localhost:8080/api/v1/company-access-requests/status/PENDING`

**Mögliche Status-Werte:**
- `PENDING` - Wartet auf Bearbeitung
- `APPROVED` - Genehmigt
- `REJECTED` - Abgelehnt

### Beispiel mit curl:

```bash
curl -X GET http://localhost:8080/api/v1/company-access-requests/status/PENDING \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## Schritt 6: Einzelne Anfrage abrufen (SYSTEM_ADMIN)

### Request

**Endpoint:** `GET http://localhost:8080/api/v1/company-access-requests/{id}`

### Beispiel mit curl:

```bash
curl -X GET http://localhost:8080/api/v1/company-access-requests/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## Schritt 7: Anfrage genehmigen (SYSTEM_ADMIN)

### Request

**Endpoint:** `PUT http://localhost:8080/api/v1/company-access-requests/{id}`

**Request Body:**
```json
{
  "status": "APPROVED"
}
```

### Beispiel mit curl:

```bash
curl -X PUT http://localhost:8080/api/v1/company-access-requests/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED"
  }'
```

### Erwartete Response (200 OK):

```json
{
  "id": 1,
  "companyName": "Beispiel GmbH",
  "contactName": "Max Mustermann",
  "contactEmail": "max@beispiel.de",
  "contactPhone": "+49 123 456789",
  "message": "Wir möchten Zugang zur Plattform erhalten.",
  "status": "APPROVED",
  "processedBy": 1,
  "processedAt": "2024-01-01T12:30:00",
  "rejectionReason": null,
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:30:00"
}
```

---

## Schritt 8: Anfrage ablehnen (SYSTEM_ADMIN)

### Request

**Endpoint:** `PUT http://localhost:8080/api/v1/company-access-requests/{id}`

**Request Body:**
```json
{
  "status": "REJECTED",
  "rejectionReason": "Unvollständige Angaben oder nicht zutreffend"
}
```

**Wichtig:** Bei `REJECTED` ist `rejectionReason` **erforderlich**!

### Beispiel mit curl:

```bash
curl -X PUT http://localhost:8080/api/v1/company-access-requests/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "REJECTED",
    "rejectionReason": "Unvollständige Angaben"
  }'
```

---

## Test-Szenarien

### Szenario 1: Validierungsfehler testen

**Test:** Anfrage ohne Company Name

```bash
curl -X POST http://localhost:8080/api/v1/company-access-requests \
  -H "Content-Type: application/json" \
  -d '{
    "contactName": "Max Mustermann",
    "contactEmail": "max@beispiel.de"
  }'
```

**Erwartete Response (400 Bad Request):**
```json
{
  "error": "Validation failed: {companyName=Company name is required}",
  "code": "VALIDATION_ERROR",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Szenario 2: Rejection ohne Reason (sollte fehlschlagen)

```bash
curl -X PUT http://localhost:8080/api/v1/company-access-requests/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "REJECTED"
  }'
```

**Erwartete Response (400 Bad Request):**
```json
{
  "error": "Rejection reason is required when rejecting a request",
  "code": "VALIDATION_ERROR",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Szenario 3: Status-Änderung bei bereits bearbeiteter Anfrage (sollte fehlschlagen)

Wenn eine Anfrage bereits `APPROVED` oder `REJECTED` ist, kann der Status nicht mehr geändert werden.

```bash
# Erst APPROVE
curl -X PUT http://localhost:8080/api/v1/company-access-requests/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED"}'

# Dann versuchen zu REJECT (sollte fehlschlagen)
curl -X PUT http://localhost:8080/api/v1/company-access-requests/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "REJECTED",
    "rejectionReason": "Test"
  }'
```

**Erwartete Response (400 Bad Request):**
```json
{
  "error": "Cannot change status of a request that has already been processed",
  "code": "VALIDATION_ERROR",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Szenario 4: Unauthorized Access (ohne Token)

```bash
curl -X GET http://localhost:8080/api/v1/company-access-requests
```

**Erwartete Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "code": "UNAUTHORIZED",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

---

## Postman Collection

Sie können auch eine Postman Collection erstellen:

### Collection-Struktur:

1. **Login** (POST `/api/v1/auth/login`)
   - Setzt Environment Variable `token` aus Response

2. **Create Access Request** (POST `/api/v1/company-access-requests`)
   - Öffentlich, kein Token nötig

3. **Get All Requests** (GET `/api/v1/company-access-requests`)
   - Header: `Authorization: Bearer {{token}}`

4. **Get Requests by Status** (GET `/api/v1/company-access-requests/status/PENDING`)
   - Header: `Authorization: Bearer {{token}}`

5. **Get Request by ID** (GET `/api/v1/company-access-requests/{id}`)
   - Header: `Authorization: Bearer {{token}}`

6. **Approve Request** (PUT `/api/v1/company-access-requests/{id}`)
   - Header: `Authorization: Bearer {{token}}`
   - Body: `{"status": "APPROVED"}`

7. **Reject Request** (PUT `/api/v1/company-access-requests/{id}`)
   - Header: `Authorization: Bearer {{token}}`
   - Body: `{"status": "REJECTED", "rejectionReason": "..."}`

---

## Datenbank prüfen

Sie können auch direkt in der Datenbank prüfen:

```sql
-- PostgreSQL Verbindung
psql -h localhost -U changeready -d changeready360

-- Alle Anfragen anzeigen
SELECT * FROM company_access_requests;

-- Nur PENDING Anfragen
SELECT * FROM company_access_requests WHERE status = 'PENDING';

-- Anfrage mit Details
SELECT 
    id,
    company_name,
    contact_name,
    contact_email,
    status,
    processed_by,
    processed_at,
    created_at
FROM company_access_requests
ORDER BY created_at DESC;
```

---

## Troubleshooting

### Problem: "Connection refused" bei Backend

**Lösung:** 
- Prüfen Sie, ob Backend läuft: `docker ps` oder `./gradlew bootRun`
- Prüfen Sie Port 8080: `netstat -an | findstr 8080` (Windows) oder `lsof -i :8080` (Linux/Mac)

### Problem: "Database connection failed"

**Lösung:**
- Prüfen Sie, ob PostgreSQL läuft: `docker ps`
- Prüfen Sie Connection-String in `application.properties`
- Prüfen Sie, ob Datenbank existiert: `psql -U changeready -l`

### Problem: "401 Unauthorized"

**Lösung:**
- Stellen Sie sicher, dass Sie sich eingeloggt haben
- Prüfen Sie, ob Token im Header korrekt ist: `Authorization: Bearer <token>`
- Prüfen Sie, ob Token abgelaufen ist (Standard: 24 Stunden)

### Problem: "403 Forbidden"

**Lösung:**
- Stellen Sie sicher, dass Sie als SYSTEM_ADMIN eingeloggt sind
- Prüfen Sie die Rolle im Login-Response

---

## Vollständiges Test-Script (Bash)

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "=== 1. Login als SYSTEM_ADMIN ==="
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@changeready360.com",
    "password": "Admin123!"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"
echo ""

echo "=== 2. Öffentliche Anfrage erstellen ==="
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/company-access-requests" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Test GmbH",
    "contactName": "Test User",
    "contactEmail": "test@example.com",
    "contactPhone": "+49 123 456789",
    "message": "Test-Anfrage"
  }')

REQUEST_ID=$(echo $CREATE_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Request ID: $REQUEST_ID"
echo ""

echo "=== 3. Alle Anfragen abrufen ==="
curl -s -X GET "$BASE_URL/company-access-requests" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .
echo ""

echo "=== 4. Anfrage genehmigen ==="
curl -s -X PUT "$BASE_URL/company-access-requests/$REQUEST_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED"
  }' | jq .
echo ""

echo "=== Test abgeschlossen ==="
```

**Verwendung:**
```bash
chmod +x test-script.sh
./test-script.sh
```

---

## Zusammenfassung

✅ **Öffentlicher Endpoint:** POST `/api/v1/company-access-requests` - Jeder kann Anfragen stellen  
✅ **Geschützte Endpoints:** Alle GET/PUT Endpoints erfordern SYSTEM_ADMIN Login  
✅ **Validierungen:** Rejection-Reason erforderlich, Status-Änderungen nur bei PENDING  
✅ **Error Handling:** Standardisierte Error-Responses mit Codes

Viel Erfolg beim Testen! 🚀

