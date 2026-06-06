# Frontend-Mockdaten

Die JSON-Dateien entsprechen den geplanten Responses des Spring-Boot-Backends.

## Dateien

- `API.md`: vollständiger API-Vertrag für das Frontend
- `openapi.yaml`: OpenAPI-Vertrag für Swagger und Client-Generierung
- `api-endpoints.json`: maschinenlesbare Liste aller MVP-Endpunkte
- `public-form.json`: Formular für einen gültigen Einladungslink
- `dashboard-overview.json`: Hauptdashboard mit KPIs und Charts
- `department-dashboard.json`: Drilldown einer Abteilung
- `submission-response.json`: Antwort nach erfolgreichem Absenden
- `error-response.json`: einheitliches Fehlerformat
- `requests/`: Beispiel-Requests für Formulare, Surveys und Kampagnen

## Direkter Import in React/Vite

```ts
import dashboard from "../../frontend-mocks/dashboard-overview.json";
```

## Laden über `fetch`

Kopiert die JSON-Dateien in den `public/mocks`-Ordner des Frontends:

```ts
const response = await fetch("/mocks/dashboard-overview.json");
const dashboard = await response.json();
```

Später wird nur die URL ersetzt:

```ts
const response = await fetch("/api/company/dashboard/overview");
```
