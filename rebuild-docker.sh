#!/bin/bash
# Docker Container neu bauen
# Führt alle notwendigen Schritte aus: Container stoppen, Images entfernen, neu bauen

echo "=== Docker Container neu bauen ==="

# 1. Container stoppen und entfernen
echo ""
echo "1. Container stoppen und entfernen..."
docker-compose down -v

# 2. Alte Images entfernen (optional, aber sauberer)
echo ""
echo "2. Alte Images entfernen..."
docker-compose rm -f

# 3. Images neu bauen (ohne Cache für sauberen Build)
echo ""
echo "3. Images neu bauen (ohne Cache)..."
docker-compose build --no-cache

# 4. Container starten
echo ""
echo "4. Container starten..."
docker-compose up -d

# 5. Logs anzeigen
echo ""
echo "5. Container-Logs anzeigen..."
echo ""
echo "=== Backend Logs ==="
docker-compose logs -f backend

