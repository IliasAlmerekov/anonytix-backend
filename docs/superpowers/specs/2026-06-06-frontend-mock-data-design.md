# Frontend Mock Data Design

## Ziel

Das React-Frontend soll sofort mit realistischen Anonytix-Daten entwickelt werden
können. Die Mock-Daten verwenden bereits die Feldnamen und Strukturen, die das
spätere Spring-Boot-Backend als JSON liefert.

## Umfang

Für den Hackathon werden direkt nutzbare JSON-Dateien angelegt:

```text
frontend-mocks/
├── public-form.json
├── dashboard-overview.json
├── department-dashboard.json
└── submission-response.json
```

Sie enthalten:

- ein dynamisches Mitarbeiterformular
- allgemeine und abteilungsspezifische Fragen
- KPI-Karten
- Sentiment-Verteilung
- Kategorie-Bewertungen
- Zeitreihen
- Abteilungs-Heatmap
- KI-Themen
- Handlungsempfehlungen

## API-Konventionen

- JSON-Felder verwenden `camelCase`.
- IDs sind UUID-Strings.
- Zeitangaben sind ISO-8601-Strings.
- Prozentwerte liegen zwischen `0` und `100`.
- Bewertungen liegen zwischen `1` und `5`.
- Jede Datei entspricht einer späteren REST-Response des Spring-Backends.
- Das Frontend kann die JSON-Dateien direkt importieren oder über `fetch` laden.

## Abgrenzung

Nicht enthalten sind Login, Entwurfsspeicherung, Abrechnung, Mitarbeiterkonten,
Microsoft-Integrationen und ein Mock-HTTP-Server.
