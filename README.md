# Anonytix Backend

Spring Boot backend for Anonytix, an anonymous employee feedback and workforce
insights platform.

The backend manages configurable surveys, invitation-based feedback collection,
AI-assisted anonymization and analysis, moderation, and aggregated company
dashboards. It is currently optimized for an MVP without authentication.

## Core Features

- Company departments and department-specific reporting
- Configurable pulse and exit surveys
- Standard and company-defined survey questions
- General invitation links with department selection
- Anonymous feedback submission
- Optional OpenAI-based analysis with structured output
- Local pre-redaction of common personal information
- Human moderation before feedback reaches the dashboard
- Aggregated company and department dashboards
- Minimum group-size protection for department results
- PostgreSQL schema and demo data managed by Flyway

## Technology Stack

- Java 21
- Spring Boot 3.5
- Spring Modulith
- Spring Data JPA
- Spring AI
- PostgreSQL 16
- Flyway
- MapStruct
- Maven
- Testcontainers
- Docker

## Architecture

The application is a modular monolith. Each top-level package represents a
business module with its own domain, services, repositories, DTOs, mappers, and
web layer where required.

```text
src/main/java/de/anonytix/
├── analysis/       AI analysis and anonymized findings
├── campaign/       Survey campaigns and invitation links
├── company/        Company and department management
├── dashboard/      Aggregated dashboard queries
├── feedback/       Public forms and feedback submissions
├── moderation/     Review and approval workflow
├── shared/         Shared configuration and API errors
└── survey/         Surveys, questions, and templates
```

Main feedback workflow:

```text
Invitation link
    -> employee selects a department
    -> backend returns the matching survey form
    -> employee submits feedback
    -> sensitive patterns are locally redacted
    -> feedback is analyzed by the configured AI provider
    -> analysis enters moderation
    -> approved results are included in aggregated dashboards
```

## Prerequisites

- Java 21
- Docker Desktop or another Docker-compatible runtime
- An OpenAI API key only when using the OpenAI profile

The Maven Wrapper is included, so a global Maven installation is not required.

## Quick Start

Start PostgreSQL:

```bash
docker compose up -d
```

Start the backend with the deterministic demo analyzer:

```bash
./mvnw spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/actuator/health
```

The `local` Spring profile can start the Docker Compose database automatically:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Environment Variables

Copy `.env.example` as a reference. Do not commit real secrets.

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/anonytix` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `anonytix` | Database username |
| `DB_PASSWORD` | `anonytix` | Database password |
| `FRONTEND_URL` | `https://anonytix.app` | Public frontend URL |
| `CORS_ORIGINS` | Localhost and `anonytix.app` | Allowed browser origins |
| `AI_PROVIDER` | `demo` | AI gateway used without the OpenAI profile |
| `OPENAI_API_KEY` | None | OpenAI API secret |
| `OPENAI_MODEL` | `gpt-4o-mini` | OpenAI chat model |
| `OPENAI_TEMPERATURE` | `0.1` | Analysis output variability |
| `OPENAI_MAX_COMPLETION_TOKENS` | `1200` | Maximum analysis output size |
| `OPENAI_RETRY_MAX_ATTEMPTS` | `3` | Retry attempts for transient failures |

## OpenAI Analysis

The default local configuration uses a deterministic demo analyzer and does not
send data to an external AI provider.

To run with OpenAI:

```bash
SPRING_PROFILES_ACTIVE=openai \
OPENAI_API_KEY=your-secret-key \
./mvnw spring-boot:run
```

The Docker image activates the `openai` profile by default. A hosted deployment
therefore requires at least:

```text
OPENAI_API_KEY=your-secret-key
```

The OpenAI integration:

- removes common email addresses, phone numbers, names with titles, links, and
  usernames before sending feedback
- treats submitted feedback as data rather than model instructions
- requests strict structured JSON output
- validates sentiments, priorities, scores, categories, and text lengths
- stores the analysis and anonymized findings separately from the submission
- marks failed analyses as `ANALYSIS_FAILED`
- sends successful analyses to the moderation workflow

Local redaction and AI rewriting reduce privacy risks, but they cannot guarantee
complete anonymity. Human moderation remains required before release.

## Database and Demo Data

Flyway owns the complete database schema and applies migrations automatically
when the application starts.

```text
V1  Core schema
V2  Initial single-tenant MVP data
V3  Dashboard history for 2024-2026
V4  Expanded English dashboard demo data
```

The current demo dataset contains:

- six departments
- English surveys, questions, findings, and action items
- historical data for 2024, 2025, and 2026
- positive, neutral, and negative feedback
- nine rating categories
- enough department responses to demonstrate privacy thresholds
- data for KPI cards, heatmaps, trends, sentiment charts, topics, and AI
  highlights

Demo company ID:

```text
10729623-735e-4382-854f-33e3450bdac7
```

Do not edit an already applied Flyway migration. Add a new migration with the
next version number instead.

## API Overview

All business APIs use the `/api/v1` prefix.

### Departments

```text
GET    /api/v1/companies/{companyId}/departments
POST   /api/v1/companies/{companyId}/departments
PATCH  /api/v1/companies/{companyId}/departments/{departmentId}
```

### Surveys

```text
GET     /api/v1/companies/{companyId}/surveys
POST    /api/v1/companies/{companyId}/surveys
GET     /api/v1/companies/{companyId}/surveys/{surveyId}
PATCH   /api/v1/companies/{companyId}/surveys/{surveyId}
POST    /api/v1/companies/{companyId}/surveys/{surveyId}/questions
PATCH   /api/v1/companies/{companyId}/surveys/{surveyId}/questions/{questionId}
DELETE  /api/v1/companies/{companyId}/surveys/{surveyId}/questions/{questionId}
PUT     /api/v1/companies/{companyId}/surveys/{surveyId}/questions/order
POST    /api/v1/companies/{companyId}/surveys/{surveyId}/publish
```

### Campaigns and Invitations

```text
GET   /api/v1/companies/{companyId}/campaigns
POST  /api/v1/companies/{companyId}/campaigns
POST  /api/v1/companies/{companyId}/campaigns/{campaignId}/activate
POST  /api/v1/companies/{companyId}/campaigns/{campaignId}/invitations
```

### Public Feedback

```text
GET   /api/v1/public/invitations/{token}/form
POST  /api/v1/public/invitations/{token}/submissions
```

The form endpoint accepts an optional `departmentId` query parameter to load
department-specific questions.

### Moderation

```text
GET   /api/v1/platform/moderation/submissions
GET   /api/v1/platform/moderation/submissions/{submissionId}
POST  /api/v1/platform/moderation/submissions/{submissionId}/approve
POST  /api/v1/platform/moderation/submissions/{submissionId}/reject
```

The moderation list accepts an optional `status` query parameter. It defaults
to `REVIEW_PENDING`.

### Dashboards

```text
GET  /api/v1/companies/{companyId}/dashboard/overview
GET  /api/v1/companies/{companyId}/dashboard/departments/{departmentId}
```

Dashboard endpoints support optional `campaignId` and `year` query parameters.
Changing either parameter triggers a new database-backed aggregation.

Example:

```text
GET /api/v1/companies/10729623-735e-4382-854f-33e3450bdac7/dashboard/overview?year=2025
```

The frontend API contract and example payloads are available in
`frontend-mocks/`.

## Tests

Tests use PostgreSQL Testcontainers. Docker must be running.

Run the complete test suite:

```bash
./mvnw test
```

Run only migration tests:

```bash
./mvnw -Dtest=DatabaseMigrationTest test
```

Build the application without running tests:

```bash
./mvnw -DskipTests package
```

## Docker

Build the production image:

```bash
docker build -t anonytix-backend .
```

Run it against a reachable PostgreSQL database:

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/anonytix \
  -e DB_USERNAME=anonytix \
  -e DB_PASSWORD=anonytix \
  -e OPENAI_API_KEY=your-secret-key \
  anonytix-backend
```

## Deployment

For a hosted environment such as DigitalOcean App Platform:

1. Deploy the repository as a Dockerfile-based web service.
2. Attach a managed PostgreSQL database.
3. Configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
4. Store `OPENAI_API_KEY` as an encrypted secret.
5. Set `CORS_ORIGINS=https://anonytix.app`.
6. Set `FRONTEND_URL=https://anonytix.app`.
7. Expose the port provided through the platform's `PORT` variable.
8. Verify `/actuator/health` after deployment.

Flyway migrations run automatically during application startup.

## Current MVP Limitations

- Authentication and authorization are not implemented yet.
- The MVP uses one seeded company context.
- Billing and tenant onboarding are not implemented.
- AI results still require human moderation.
- Privacy measures reduce identification risk but do not provide a legal or
  technical guarantee of complete anonymity.
- Demo data is synthetic and must not be presented as real employee feedback.

## Repository Notes

- Product requirements: `PRODUCT.md`
- Frontend API examples: `frontend-mocks/`
- Database migrations: `src/main/resources/db/migration/`
- Application configuration: `src/main/resources/application.yml`
- OpenAI profile: `src/main/resources/application-openai.yml`
