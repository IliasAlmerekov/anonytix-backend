# Frontend Mock Data Design

## Ziel

Das React-Frontend soll sofort mit realistischen Anonytix-Daten entwickelt werden
können. Die Mock-Daten verwenden bereits die Feldnamen und Strukturen, die das
spätere Spring-Boot-Backend als JSON liefert.

## Umfang

Für den Hackathon wird eine einzelne Datei angelegt:

```text
frontend-mocks/mockData.ts
```

Sie enthält:

- TypeScript-Typen für Formular, Dashboard und Abteilungen
- ein dynamisches Mitarbeiterformular
- allgemeine und abteilungsspezifische Fragen
- KPI-Karten
- Sentiment-Verteilung
- Kategorie-Bewertungen
- Zeitreihen
- Abteilungs-Heatmap
- KI-Themen
- Handlungsempfehlungen
- asynchrone Mock-Funktionen für Laden und Absenden

## API-Konventionen

- JSON-Felder verwenden `camelCase`.
- IDs sind UUID-Strings.
- Zeitangaben sind ISO-8601-Strings.
- Prozentwerte liegen zwischen `0` und `100`.
- Bewertungen liegen zwischen `1` und `5`.
- Das Frontend greift über Mock-Funktionen auf die Daten zu und importiert
  Fixture-Objekte nicht direkt in Komponenten.

## Geplante Mock-Funktionen

```typescript
getPublicSurveyForm(token)
submitFeedback(token, request)
getDashboardOverview()
getDepartmentDashboard(departmentId)
```

Alle Funktionen liefern Promises, damit sie später ohne Änderungen an den
React-Komponenten durch echte HTTP-Aufrufe ersetzt werden können.

## Abgrenzung

Nicht enthalten sind Login, Entwurfsspeicherung, Abrechnung, Mitarbeiterkonten,
Microsoft-Integrationen und ein Mock-HTTP-Server.

