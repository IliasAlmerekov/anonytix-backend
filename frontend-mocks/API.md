# Anonytix Frontend API

## Grundlage

```text
Base URL: http://localhost:8080/api/v1
Content-Type: application/json
```

Für das Hackathon-MVP gibt es noch keinen Login. Unternehmensendpunkte
enthalten deshalb die `companyId` im Pfad. Das Frontend sollte die Base URL über
eine Umgebungsvariable konfigurieren:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Öffentliche Feedback-API

### Formular laden

```http
GET /public/invitations/{token}/form
```

Response: [`public-form.json`](./public-form.json)

Mögliche Fehler:

- `404 INVITATION_NOT_FOUND`
- `410 INVITATION_EXPIRED`
- `409 INVITATION_ALREADY_USED`

### Feedback absenden

```http
POST /public/invitations/{token}/submissions
```

Request: [`requests/submit-feedback.json`](./requests/submit-feedback.json)  
Response `201`: [`submission-response.json`](./submission-response.json)

Pro Antwort darf abhängig vom Fragetyp nur das passende Wertefeld gesetzt sein:

| Fragetyp | Feld |
|---|---|
| `RATING` | `numericValue` |
| `TEXT` | `textValue` |
| `BOOLEAN` | `booleanValue` |
| `SINGLE_CHOICE` | genau eine ID in `selectedOptionIds` |
| `MULTI_CHOICE` | mehrere IDs in `selectedOptionIds` |

## Abteilungen

```http
GET   /companies/{companyId}/departments
POST  /companies/{companyId}/departments
PATCH /companies/{companyId}/departments/{departmentId}
```

Erstellen:

```json
{
  "name": "Softwareentwicklung",
  "code": "DEV"
}
```

Bearbeiten oder deaktivieren:

```json
{
  "name": "Produktentwicklung",
  "active": true
}
```

## Umfragen und Formular-Builder

```http
GET   /companies/{companyId}/surveys
POST  /companies/{companyId}/surveys
GET   /companies/{companyId}/surveys/{surveyId}
PATCH /companies/{companyId}/surveys/{surveyId}
POST  /companies/{companyId}/surveys/{surveyId}/publish
```

Umfrage erstellen: [`requests/create-survey.json`](./requests/create-survey.json)

Unterstützte Vorlagen:

- `EMPLOYEE_SATISFACTION`
- `EXIT_INTERVIEW`

Status:

- `DRAFT`
- `PUBLISHED`
- `ARCHIVED`

Nur `DRAFT` darf bearbeitet werden.

### Fragen

```http
POST   /companies/{companyId}/surveys/{surveyId}/questions
PATCH  /companies/{companyId}/surveys/{surveyId}/questions/{questionId}
DELETE /companies/{companyId}/surveys/{surveyId}/questions/{questionId}
PUT    /companies/{companyId}/surveys/{surveyId}/questions/order
```

Frage erstellen: [`requests/create-question.json`](./requests/create-question.json)

Reihenfolge ändern:

```json
{
  "questionIds": [
    "84b9c571-a11c-4fd4-84e5-76ec5f6055f0",
    "39f96f91-9ad4-4884-97fd-9dd2603fe409"
  ]
}
```

Ohne `departmentIds` gilt eine Frage für alle Abteilungen. `DELETE` deaktiviert
die Frage; vorhandene Antworten werden nicht gelöscht.

## Kampagnen und Einladungen

```http
GET  /companies/{companyId}/campaigns
POST /companies/{companyId}/campaigns
POST /companies/{companyId}/campaigns/{campaignId}/activate
POST /companies/{companyId}/campaigns/{campaignId}/invitations
```

Kampagne erstellen: [`requests/create-campaign.json`](./requests/create-campaign.json)  
Einladungen erzeugen: [`requests/generate-invitations.json`](./requests/generate-invitations.json)

Die Einladungsantwort enthält die Klartext-Tokens genau einmal:

```json
{
  "campaignId": "93b6108f-f005-4f4b-8ce9-952fa0a7ddc4",
  "invitations": [
    {
      "departmentId": "ac38af63-dc5a-416e-b5d7-c237535ec37b",
      "url": "http://localhost:5173/feedback/a-random-token",
      "expiresAt": "2026-07-14T21:59:59Z"
    }
  ]
}
```

Das Backend speichert anschließend nur Token-Hashes.

## Dashboard

### Übersicht

```http
GET /companies/{companyId}/dashboard/overview?campaignId={campaignId}
```

Response: [`dashboard-overview.json`](./dashboard-overview.json)

### Abteilungs-Drilldown

```http
GET /companies/{companyId}/dashboard/departments/{departmentId}?campaignId={campaignId}
```

Response: [`department-dashboard.json`](./department-dashboard.json)

Ist die Mindestgruppengröße nicht erreicht, liefert das Backend:

```json
{
  "department": {
    "id": "d2f7524e-6e27-48dc-83ea-4ba9bfe716ca",
    "name": "Personal"
  },
  "sampleSize": 4,
  "minimumGroupSize": 5,
  "visible": false,
  "suppressionReason": "MINIMUM_GROUP_SIZE_NOT_REACHED"
}
```

## Interne Moderation

Diese Ansicht gehört zu Anonytix und ist nicht Teil des Firmen-Dashboards.

```http
GET  /platform/moderation/submissions?status=REVIEW_PENDING
GET  /platform/moderation/submissions/{submissionId}
POST /platform/moderation/submissions/{submissionId}/approve
POST /platform/moderation/submissions/{submissionId}/reject
```

Ablehnen:

```json
{
  "reason": "Der Text enthält weiterhin identifizierende Angaben."
}
```

Das Firmen-Frontend darf keine Endpunkte für originale Freitexte oder einzelne
Mitarbeiterantworten erhalten.

## Einheitliches Fehlerformat

Response-Beispiel: [`error-response.json`](./error-response.json)

Wichtige Codes:

| HTTP | Code | Bedeutung |
|---:|---|---|
| 400 | `VALIDATION_FAILED` | Request oder Formularantwort ungültig |
| 404 | `RESOURCE_NOT_FOUND` | Datensatz existiert nicht |
| 404 | `INVITATION_NOT_FOUND` | Token unbekannt |
| 409 | `INVITATION_ALREADY_USED` | Link wurde bereits verwendet |
| 409 | `SURVEY_ALREADY_PUBLISHED` | Veröffentlichte Umfrage ist gesperrt |
| 410 | `INVITATION_EXPIRED` | Link ist abgelaufen |
| 422 | `MINIMUM_GROUP_SIZE_NOT_REACHED` | Statistik darf nicht angezeigt werden |
| 500 | `INTERNAL_ERROR` | Unerwarteter Backendfehler |

## Maschinenlesbare Übersicht

Alle Endpunkte stehen zusätzlich in
[`api-endpoints.json`](./api-endpoints.json).

