# Status-Dokumentation: Auswertung & Ergebnisse

**Stand:** Dezember 2024  
**Projekt:** ChangeReady360

---

## Übersicht

Dieses Dokument beschreibt den aktuellen Stand der Auswertungs- und Ergebnis-Funktionalität im ChangeReady360-Projekt. Es richtet sich an Projektmanager, Stakeholder und alle, die einen Überblick über den Fortschritt benötigen.

Das Projekt gliedert sich in vier Hauptbereiche:

1. **Datenmodell für Auswertung** – Wie werden die Daten strukturiert?
2. **API Endpoints für Ergebnisse** – Wie kommunizieren Frontend und Backend?
3. **Scoring Algorithmus Basis** – Wie werden die Ergebnisse berechnet?
4. **Chart Library Evaluierung** – Wie werden die Ergebnisse visualisiert?

---

## 1. Datenmodell für Auswertung

### ✅ Frontend: Vollständig fertig

**Was bedeutet das?**  
Das Frontend (die Benutzeroberfläche) weiß genau, welche Informationen für eine Auswertung benötigt werden und wie diese strukturiert sein müssen.

**Was ist definiert:**
- **Ergebnis-Struktur:** Für jede Kategorie und Unterkategorie wird gespeichert:
  - Der Durchschnittswert aller Antworten
  - Wie viele Fragen beantwortet wurden
  - Wie viele Fragen insgesamt vorhanden sind
  - Welche Fragen "umgekehrt" bewertet werden müssen (Reverse-Items)
- **Antwort-Struktur:** Jede einzelne Antwort wird mit der Frage-ID und dem Wert (1-5) gespeichert
- **Frage-Struktur:** Jede Frage enthält Informationen darüber, zu welcher Kategorie sie gehört und ob sie umgekehrt bewertet werden muss

**Status:** ✅ **Fertig** – Alle benötigten Datenfelder sind definiert und können verwendet werden.

### ❌ Backend: Noch nicht vorhanden

**Was bedeutet das?**  
Das Backend (der Server) hat noch keine Struktur, um Umfrage-Daten zu speichern und zu verwalten.

**Was fehlt:**
- Keine Datenbank-Struktur für Umfrage-Vorlagen
- Keine Datenbank-Struktur für gestartete Umfragen
- Keine Datenbank-Struktur für abgegebene Antworten
- Keine Datenbank-Struktur für Auswertungsergebnisse

**Status:** ❌ **Nicht vorhanden** – Das Backend muss noch entwickelt werden.

---

## 2. API Endpoints für Ergebnisse

### ⚠️ Frontend: Test-Version vorhanden

**Was bedeutet das?**  
Die Benutzeroberfläche kann bereits Ergebnisse berechnen und anzeigen, aber nur mit Test-Daten, die nicht dauerhaft gespeichert werden.

**Was funktioniert:**
- ✅ Ergebnisse können berechnet werden
- ✅ Ergebnisse werden nach Kategorien gruppiert angezeigt
- ✅ Durchschnitte und Antwortquoten werden korrekt berechnet
- ✅ Umgekehrte Fragen (Reverse-Items) werden erkannt
- ⚠️ **Daten werden nur im Browser gespeichert**
- ⚠️ **Daten gehen verloren, wenn die Seite neu geladen wird**
- ⚠️ **Daten können nicht zwischen verschiedenen Geräten geteilt werden**

**Status:** ⚠️ **Funktioniert für Tests, aber nicht für den produktiven Einsatz** – Muss durch eine echte Server-Verbindung ersetzt werden.

### ❌ Backend: Keine Verbindungen vorhanden

**Was bedeutet das?**  
Der Server bietet noch keine Schnittstellen, über die die Benutzeroberfläche Umfrage-Daten abrufen oder speichern kann.

**Was fehlt:**
- Keine Möglichkeit, Umfrage-Vorlagen vom Server abzurufen
- Keine Möglichkeit, eine neue Umfrage zu starten
- Keine Möglichkeit, Antworten zu speichern
- Keine Möglichkeit, eine Umfrage abzuschicken
- Keine Möglichkeit, Ergebnisse zu berechnen

**Was funktioniert bereits:**
- ✅ Benutzer können sich anmelden
- ✅ Benutzer können sich abmelden
- ✅ Benutzer können ihre eigenen Daten abrufen
- ✅ Benutzer können verwaltet werden
- ✅ Firmen können verwaltet werden

**Status:** ❌ **Nicht implementiert** – Die Server-Funktionen für Umfragen müssen noch entwickelt werden.

---

## 3. Scoring Algorithmus Basis

### ✅ Frontend: Berechnungslogik fertig

**Was bedeutet das?**  
Die Formel, mit der aus den Antworten ein Score (Wertung) berechnet wird, ist vollständig implementiert.

**Wie funktioniert die Berechnung:**

1. **Schritt 1:** Alle Antwortwerte werden zusammengezählt und durch die Anzahl geteilt (Durchschnitt)
   - Beispiel: Wenn jemand 10 Fragen mit den Werten 3, 4, 5, 4, 3, 5, 4, 3, 4, 5 beantwortet hat
   - Summe = 40, Anzahl = 10, Durchschnitt = 4,0

2. **Schritt 2:** Der Durchschnitt wird von der 1-5 Skala auf eine 0-100% Skala umgerechnet
   - Wert 1 (Stimme gar nicht zu) → 0%
   - Wert 2 → 25%
   - Wert 3 (Neutral) → 50%
   - Wert 4 → 75%
   - Wert 5 (Stimme voll zu) → 100%
   - Beispiel: Durchschnitt 4,0 → 75%

**Was funktioniert:**
- ✅ Gesamt-Score wird korrekt berechnet
- ✅ Score pro Kategorie wird berechnet
- ✅ Score pro Unterkategorie wird berechnet
- ✅ Antwortquote wird berechnet (wie viele Fragen wurden beantwortet)

**Was fehlt noch:**
- ⚠️ **Umgekehrte Fragen (Reverse-Items) werden noch nicht korrekt behandelt**
  - Einige Fragen sind "negativ" formuliert und müssen umgekehrt bewertet werden
  - Beispiel: "Ich bin unzufrieden" – Wer hier "5" (stimme voll zu) sagt, sollte als "1" gewertet werden
  - Aktuell werden diese Fragen noch wie normale Fragen behandelt

**Status:** ✅ **Basis-Berechnung funktioniert** – Umgekehrte Fragen müssen noch integriert werden.

### ❌ Backend: Keine Berechnungslogik vorhanden

**Was bedeutet das?**  
Der Server kann noch keine Scores berechnen. Alle Berechnungen laufen aktuell nur im Browser.

**Was fehlt:**
- Keine Server-seitige Berechnung von Scores
- Keine Berücksichtigung von umgekehrten Fragen
- Keine Möglichkeit, Berechnungen zu beschleunigen (bei vielen Antworten)

**Status:** ❌ **Nicht vorhanden** – Die Berechnungslogik muss noch auf dem Server implementiert werden.

---

## 4. Chart Library Evaluierung

### ✅ Frontend: Diagramm-Anzeige fertig

**Was bedeutet das?**  
Die Ergebnisse werden bereits als Kreisdiagramm (Donut-Chart) angezeigt.

**Wie funktioniert es:**
- Ein Kreisdiagramm zeigt den Gesamt-Score als Prozentwert
- In der Mitte steht die Prozentzahl (z.B. "75%")
- Der gefärbte Teil des Kreises zeigt visuell den Score
- Die Farben entsprechen dem Corporate Design (Blau für den Score, Creme für den Hintergrund)

**Vorteile der aktuellen Lösung:**
- ✅ Keine zusätzlichen Programme oder Bibliotheken nötig
- ✅ Schnell und leichtgewichtig
- ✅ Vollständige Kontrolle über das Aussehen
- ✅ Passt perfekt zum Design-System

**Nachteile:**
- ⚠️ Bei Änderungen muss alles manuell angepasst werden
- ⚠️ Keine Animationen beim Laden
- ⚠️ Keine zusätzlichen Informationen beim Überfahren mit der Maus

**Status:** ✅ **Funktioniert und ist einsatzbereit** für einfache Kreisdiagramme.

### 📋 Externe Chart-Bibliotheken: Nicht geprüft

**Was bedeutet das?**  
Es wurde noch nicht geprüft, ob zusätzliche Programme für komplexere Diagramme benötigt werden.

**Aktuelle Situation:**
- Keine zusätzlichen Diagramm-Bibliotheken installiert
- Die aktuelle Lösung reicht für die aktuellen Anforderungen aus

**Wann wäre eine Evaluierung nötig?**
- Falls komplexere Diagramme benötigt werden (z.B. Balkendiagramme, Zeitreihen, Vergleichsdiagramme)
- Falls interaktive Features gewünscht werden (z.B. Zoom, Filter, Tooltips)

**Mögliche Alternativen (falls benötigt):**
- Verschiedene kommerzielle und kostenlose Bibliotheken stehen zur Verfügung
- Diese würden zusätzliche Funktionen bieten, aber auch mehr Komplexität und Dateigröße bedeuten

**Status:** ✅ **Aktuelle Lösung ist ausreichend** – Keine externe Bibliothek erforderlich für die aktuellen Anforderungen.

---

## Zusammenfassung

### ✅ Was ist fertig:

1. **Datenstruktur im Frontend** – Alle benötigten Datenfelder sind definiert
2. **Berechnungsformel** – Die Formel zur Umrechnung von Antworten (1-5) in Prozent (0-100%) funktioniert
3. **Diagramm-Anzeige** – Kreisdiagramm zeigt Ergebnisse korrekt an
4. **Test-Version** – Funktioniert für Entwicklung und Tests

### ⚠️ Was teilweise fertig ist:

1. **Umgekehrte Fragen** – Werden erkannt, aber noch nicht korrekt in die Berechnung einbezogen
2. **Server-Verbindung** – Test-Version vorhanden, echte Verbindung zum Server fehlt

### ❌ Was noch nicht vorhanden ist:

1. **Datenbank-Struktur** – Keine Möglichkeit, Umfrage-Daten dauerhaft zu speichern
2. **Server-Funktionen** – Keine Schnittstellen für Umfragen und Ergebnisse
3. **Server-Berechnung** – Keine Berechnung von Scores auf dem Server
4. **Dauerhafte Speicherung** – Daten gehen verloren, wenn die Seite neu geladen wird

---

## Nächste Schritte

### Priorität 1 (Sehr wichtig):

1. **Datenbank-Struktur erstellen**
   - Tabellen für Umfrage-Vorlagen anlegen
   - Tabellen für gestartete Umfragen anlegen
   - Tabellen für abgegebene Antworten anlegen

2. **Server-Funktionen entwickeln**
   - Funktionen zum Abrufen von Umfrage-Vorlagen
   - Funktionen zum Starten von Umfragen
   - Funktionen zum Speichern von Antworten
   - Funktionen zum Abschicken von Umfragen
   - Funktionen zum Berechnen von Ergebnissen

3. **Frontend mit Server verbinden**
   - Test-Version durch echte Server-Verbindung ersetzen
   - Sicherstellen, dass Daten dauerhaft gespeichert werden

### Priorität 2 (Wichtig):

4. **Umgekehrte Fragen integrieren**
   - Sicherstellen, dass umgekehrte Fragen korrekt in die Berechnung einbezogen werden

5. **Berechnung auf dem Server**
   - Score-Berechnung auch auf dem Server implementieren (für Konsistenz und Geschwindigkeit)

6. **Datenbank-Migrationen**
   - Tabellen in der Datenbank anlegen

### Priorität 3 (Weniger dringend):

7. **Diagramm-Bibliotheken prüfen**
   - Nur falls komplexere Diagramme benötigt werden

8. **Animationen hinzufügen**
   - Diagramm könnte beim Laden animiert werden (bessere Benutzererfahrung)

9. **Performance-Optimierung**
   - Berechnungen könnten zwischengespeichert werden (bei vielen Antworten)

---

**Erstellt:** Dezember 2024  
**Letzte Aktualisierung:** Dezember 2024
