# Docker Container neu bauen
# Führt alle notwendigen Schritte aus: Container stoppen, Images entfernen, neu bauen

Write-Host "=== Docker Container neu bauen ===" -ForegroundColor Cyan

# 1. Container stoppen und entfernen
Write-Host "`n1. Container stoppen und entfernen..." -ForegroundColor Yellow
docker-compose down -v

# 2. Alte Images entfernen (optional, aber sauberer)
Write-Host "`n2. Alte Images entfernen..." -ForegroundColor Yellow
docker-compose rm -f

# 3. Images neu bauen (ohne Cache für sauberen Build)
Write-Host "`n3. Images neu bauen (ohne Cache)..." -ForegroundColor Yellow
docker-compose build --no-cache

# 4. Container starten
Write-Host "`n4. Container starten..." -ForegroundColor Yellow
docker-compose up -d

# 5. Logs anzeigen
Write-Host "`n5. Container-Logs anzeigen..." -ForegroundColor Yellow
Write-Host "`n=== Backend Logs ===" -ForegroundColor Green
docker-compose logs -f backend

