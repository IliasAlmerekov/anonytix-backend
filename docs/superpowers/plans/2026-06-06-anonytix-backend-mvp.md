# Anonytix Backend MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a working Spring Boot backend for dynamic department-specific surveys, anonymous feedback submission, AI-assisted analysis, moderation, and aggregated dashboards matching `frontend-mocks/openapi.yaml`.

**Architecture:** The application is a Spring Modulith with domain-aligned top-level modules: `company`, `survey`, `campaign`, `feedback`, `analysis`, `moderation`, and `dashboard`. PostgreSQL is the single database, Flyway owns the schema, public feedback is token-based, and company dashboards only expose approved aggregates above the configured minimum group size. AI access is hidden behind a gateway with a deterministic demo implementation and an optional OpenAI-backed implementation.

**Tech Stack:** Java 21, Maven, Spring Boot 3.5.11, Spring Modulith 1.4.8, Spring AI 1.1.6, Spring Data JPA, PostgreSQL, Flyway, MapStruct 1.6.3, Bean Validation, Testcontainers, JUnit 5, AssertJ, Docker Compose.

---

## File Structure

```text
pom.xml
compose.yaml
.env.example
src/main/java/de/anonytix/
├── AnonytixApplication.java
├── shared/
│   ├── config/CorsConfiguration.java
│   ├── domain/BaseEntity.java
│   ├── error/ApiError.java
│   ├── error/ApiExceptionHandler.java
│   └── mapper/MappingConfig.java
├── company/
│   ├── package-info.java
│   ├── domain/{Company,Department}.java
│   ├── dto/*.java
│   ├── mapper/DepartmentMapper.java
│   ├── repository/*.java
│   ├── service/DepartmentService.java
│   └── web/DepartmentController.java
├── survey/
│   ├── package-info.java
│   ├── domain/{Survey,Question,QuestionOption,QuestionDepartment}.java
│   ├── dto/*.java
│   ├── mapper/*.java
│   ├── repository/*.java
│   ├── service/{SurveyService,SurveyTemplateService}.java
│   └── web/SurveyController.java
├── campaign/
│   ├── package-info.java
│   ├── domain/{Campaign,Invitation}.java
│   ├── dto/*.java
│   ├── mapper/*.java
│   ├── repository/*.java
│   ├── service/{CampaignService,InvitationService,TokenService}.java
│   └── web/CampaignController.java
├── feedback/
│   ├── package-info.java
│   ├── domain/{FeedbackSubmission,Answer,AnswerSelectedOption}.java
│   ├── dto/*.java
│   ├── event/FeedbackSubmitted.java
│   ├── repository/*.java
│   ├── service/{PublicFormService,FeedbackSubmissionService}.java
│   └── web/PublicFeedbackController.java
├── analysis/
│   ├── package-info.java
│   ├── domain/{AiAnalysis,AnalysisFinding}.java
│   ├── gateway/FeedbackAnalysisGateway.java
│   ├── gateway/{DemoFeedbackAnalysisGateway,OpenAiFeedbackAnalysisGateway}.java
│   ├── service/FeedbackAnalysisService.java
│   └── repository/*.java
├── moderation/
│   ├── package-info.java
│   ├── domain/ModerationReview.java
│   ├── dto/*.java
│   ├── repository/ModerationReviewRepository.java
│   ├── service/ModerationService.java
│   └── web/ModerationController.java
└── dashboard/
    ├── package-info.java
    ├── dto/*.java
    ├── repository/DashboardQueryRepository.java
    ├── service/DashboardService.java
    └── web/DashboardController.java
src/main/resources/
├── application.yml
├── application-local.yml
└── db/migration/
    ├── V1__create_core_schema.sql
    └── V2__insert_demo_data.sql
src/test/java/de/anonytix/
├── ArchitectureTest.java
├── company/DepartmentApiTest.java
├── survey/SurveyApiTest.java
├── campaign/CampaignApiTest.java
├── feedback/PublicFeedbackApiTest.java
├── analysis/FeedbackAnalysisServiceTest.java
├── moderation/ModerationApiTest.java
└── dashboard/DashboardApiTest.java
```

## Task 1: Bootstrap and Infrastructure

**Files:**
- Create: `pom.xml`
- Create: `compose.yaml`
- Create: `.env.example`
- Create: `.gitignore`
- Create: `src/main/java/de/anonytix/AnonytixApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Test: `src/test/java/de/anonytix/AnonytixApplicationTest.java`

- [ ] **Step 1: Write the failing context test**

```java
package de.anonytix;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AnonytixApplicationTest {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run: `mvn -Dtest=AnonytixApplicationTest test`

Expected: FAIL because no Maven project or application class exists.

- [ ] **Step 3: Add the Maven build**

Use:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.11</version>
</parent>
```

Add starters for web, validation, data-jpa, actuator, Flyway, PostgreSQL,
Spring Modulith, Spring AI OpenAI, MapStruct, tests, Modulith tests, and
Testcontainers PostgreSQL. Import:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.modulith</groupId>
      <artifactId>spring-modulith-bom</artifactId>
      <version>1.4.8</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.1.6</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Configure MapStruct 1.6.3 through `maven-compiler-plugin` annotation processor
paths and Java release 21.

- [ ] **Step 4: Add application and local infrastructure configuration**

`compose.yaml` runs PostgreSQL with database `anonytix`, user `anonytix`, a
health check, named volume, and port `${POSTGRES_PORT:-5432}:5432`.

`application.yml` must set:

```yaml
spring:
  application:
    name: anonytix-backend
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/anonytix}
    username: ${DB_USERNAME:anonytix}
    password: ${DB_PASSWORD:anonytix}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
anonytix:
  frontend-url: ${FRONTEND_URL:http://localhost:5173}
  ai:
    provider: ${AI_PROVIDER:demo}
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- [ ] **Step 5: Run test and verify GREEN**

Run: `mvn -Dtest=AnonytixApplicationTest test`

Expected: PASS with a PostgreSQL Testcontainer supplied by test configuration.

- [ ] **Step 6: Commit**

```bash
git add pom.xml compose.yaml .env.example .gitignore src
git commit -m "build: bootstrap Spring Boot backend"
```

## Task 2: Database Schema and Shared API Infrastructure

**Files:**
- Create: `src/main/resources/db/migration/V1__create_core_schema.sql`
- Create: `src/main/java/de/anonytix/shared/domain/BaseEntity.java`
- Create: `src/main/java/de/anonytix/shared/mapper/MappingConfig.java`
- Create: `src/main/java/de/anonytix/shared/error/ApiError.java`
- Create: `src/main/java/de/anonytix/shared/error/FieldErrorDetail.java`
- Create: `src/main/java/de/anonytix/shared/error/ResourceNotFoundException.java`
- Create: `src/main/java/de/anonytix/shared/error/ConflictException.java`
- Create: `src/main/java/de/anonytix/shared/error/ApiExceptionHandler.java`
- Create: `src/main/java/de/anonytix/shared/config/CorsConfiguration.java`
- Test: `src/test/java/de/anonytix/DatabaseMigrationTest.java`

- [ ] **Step 1: Write a failing Flyway schema test**

The test starts PostgreSQL with Testcontainers, loads the Spring context, and
asserts these tables exist:

```java
assertThat(tableNames).contains(
    "companies", "departments", "surveys", "questions", "question_options",
    "question_departments", "campaigns", "invitations",
    "feedback_submissions", "answers", "answer_selected_options",
    "ai_analyses", "analysis_findings", "moderation_reviews", "action_items"
);
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=DatabaseMigrationTest test`

Expected: FAIL because migration and tables do not exist.

- [ ] **Step 3: Create V1 schema**

Create UUID primary keys, foreign keys, enum checks, indexes for
`company_id`, `campaign_id`, `department_id`, submission status, and unique
constraints for company slug, department code per company, token hash, and one
AI analysis per submission.

Privacy constraint:

```sql
CREATE TABLE feedback_submissions (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    department_id UUID NOT NULL REFERENCES departments(id),
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL,
    raw_data_delete_at TIMESTAMPTZ
);
```

Do not add `invitation_id`, email, name, or account ID to this table.

- [ ] **Step 4: Add common API error handling**

The handler maps validation failures to:

```json
{
  "timestamp": "2026-06-06T12:35:00Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Die Anfrage enthält ungültige Werte.",
  "path": "/api/v1/...",
  "fieldErrors": [
    {"field": "name", "message": "darf nicht leer sein"}
  ]
}
```

- [ ] **Step 5: Run test and verify GREEN**

Run: `mvn -Dtest=DatabaseMigrationTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/db src/main/java/de/anonytix/shared src/test
git commit -m "feat: add database schema and API error contract"
```

## Task 3: Company and Department API

**Files:**
- Create: `src/main/java/de/anonytix/company/package-info.java`
- Create: `src/main/java/de/anonytix/company/domain/Company.java`
- Create: `src/main/java/de/anonytix/company/domain/Department.java`
- Create: `src/main/java/de/anonytix/company/repository/CompanyRepository.java`
- Create: `src/main/java/de/anonytix/company/repository/DepartmentRepository.java`
- Create: `src/main/java/de/anonytix/company/dto/CreateDepartmentRequest.java`
- Create: `src/main/java/de/anonytix/company/dto/UpdateDepartmentRequest.java`
- Create: `src/main/java/de/anonytix/company/dto/DepartmentResponse.java`
- Create: `src/main/java/de/anonytix/company/mapper/DepartmentMapper.java`
- Create: `src/main/java/de/anonytix/company/service/DepartmentService.java`
- Create: `src/main/java/de/anonytix/company/web/DepartmentController.java`
- Test: `src/test/java/de/anonytix/company/DepartmentApiTest.java`

- [ ] **Step 1: Write failing API tests**

Cover:

```text
POST /api/v1/companies/{companyId}/departments -> 201
GET  /api/v1/companies/{companyId}/departments -> 200
PATCH /api/v1/companies/{companyId}/departments/{departmentId} -> 200
POST with duplicate code -> 409
```

Assert the response contract:

```json
{
  "id": "UUID",
  "name": "Softwareentwicklung",
  "code": "DEV",
  "active": true
}
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=DepartmentApiTest test`

Expected: FAIL with 404 because endpoints do not exist.

- [ ] **Step 3: Implement entities, repositories, mapper, service, controller**

Use MapStruct:

```java
@Mapper(config = MappingConfig.class)
public interface DepartmentMapper {
    DepartmentResponse toResponse(Department department);
}
```

Service methods must always query by both company ID and department ID to avoid
cross-company access.

- [ ] **Step 4: Run test and verify GREEN**

Run: `mvn -Dtest=DepartmentApiTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/de/anonytix/company src/test/java/de/anonytix/company
git commit -m "feat: add company department management"
```

## Task 4: Dynamic Survey Builder

**Files:**
- Create: all `survey` module files listed in File Structure
- Test: `src/test/java/de/anonytix/survey/SurveyApiTest.java`

- [ ] **Step 1: Write failing survey API tests**

Cover:

```text
POST /companies/{companyId}/surveys copies standard template questions
POST /surveys/{surveyId}/questions creates a department-specific question
PUT /questions/order changes positions
POST /surveys/{surveyId}/publish changes DRAFT to PUBLISHED
PATCH published survey returns 409 SURVEY_ALREADY_PUBLISHED
```

Assert template creation returns `OVERALL_SATISFACTION`, `LEADERSHIP`,
`WORKLOAD`, `COMMUNICATION`, `RECOMMENDATION`, and two text questions.

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=SurveyApiTest test`

Expected: FAIL with 404.

- [ ] **Step 3: Implement survey domain**

Enums:

```java
enum SurveyType { PULSE, EXIT }
enum SurveyStatus { DRAFT, PUBLISHED, ARCHIVED }
enum QuestionType { RATING, SINGLE_CHOICE, MULTI_CHOICE, BOOLEAN, TEXT }
enum QuestionSource { STANDARD, CUSTOM }
```

`SurveyTemplateService` returns immutable Java template definitions and copies
them into persisted `Question` entities when creating a survey.

- [ ] **Step 4: Implement validation**

Rules:

- RATING requires minimum and maximum.
- TEXT may set `maximumLength` and `analyzeWithAi`.
- Choice questions require options.
- Question department IDs must belong to the same company.
- Only DRAFT surveys may change.
- Deleting a question sets `active=false`.

- [ ] **Step 5: Run test and verify GREEN**

Run: `mvn -Dtest=SurveyApiTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/de/anonytix/survey src/test/java/de/anonytix/survey
git commit -m "feat: add dynamic survey builder"
```

## Task 5: Campaigns and Anonymous Invitations

**Files:**
- Create: all `campaign` module files listed in File Structure
- Test: `src/test/java/de/anonytix/campaign/CampaignApiTest.java`

- [ ] **Step 1: Write failing campaign tests**

Cover:

```text
POST campaign only accepts PUBLISHED survey
POST activate changes SCHEDULED to ACTIVE
POST invitations creates requested count per department
database stores SHA-256 token hash, not clear token
response exposes invitation URL exactly once
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=CampaignApiTest test`

Expected: FAIL with 404.

- [ ] **Step 3: Implement secure token generation**

Use `SecureRandom` with 32 bytes and URL-safe Base64:

```java
String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
String hash = HexFormat.of().formatHex(
    MessageDigest.getInstance("SHA-256").digest(token.getBytes(UTF_8))
);
```

Never log or persist clear tokens.

- [ ] **Step 4: Implement campaign and invitation APIs**

Invitation records contain campaign, department, token hash, status, expiry,
and used timestamp. Validate that departments belong to the campaign company.

- [ ] **Step 5: Run test and verify GREEN**

Run: `mvn -Dtest=CampaignApiTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/de/anonytix/campaign src/test/java/de/anonytix/campaign
git commit -m "feat: add campaigns and anonymous invitations"
```

## Task 6: Public Form and Feedback Submission

**Files:**
- Create: all `feedback` module files listed in File Structure
- Test: `src/test/java/de/anonytix/feedback/PublicFeedbackApiTest.java`

- [ ] **Step 1: Write failing public form tests**

Cover:

```text
GET valid token returns general plus matching department questions
GET expired token returns 410 INVITATION_EXPIRED
GET used token returns 409 INVITATION_ALREADY_USED
POST validates required questions and value type
POST creates submission and answers, then marks invitation USED atomically
POST does not persist invitation ID on feedback submission
```

Compare the successful GET response fields to
`frontend-mocks/public-form.json`.

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=PublicFeedbackApiTest test`

Expected: FAIL with 404.

- [ ] **Step 3: Implement form loading**

Resolve the invitation by SHA-256 hash, verify `ACTIVE`, expiry, campaign
`ACTIVE`, and return active questions that either have no department mapping or
match the invitation department.

- [ ] **Step 4: Implement transactional submission**

Within one `@Transactional` method:

1. Lock invitation row.
2. Revalidate token status.
3. Validate every answer against its question type.
4. Persist submission using company/campaign/department only.
5. Persist answers and selected options.
6. Mark invitation `USED`.
7. Publish `FeedbackSubmitted(submissionId)`.

- [ ] **Step 5: Run test and verify GREEN**

Run: `mvn -Dtest=PublicFeedbackApiTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/de/anonytix/feedback src/test/java/de/anonytix/feedback
git commit -m "feat: add anonymous feedback submission flow"
```

## Task 7: AI Analysis with Demo and OpenAI Providers

**Files:**
- Create: all `analysis` module files listed in File Structure
- Test: `src/test/java/de/anonytix/analysis/FeedbackAnalysisServiceTest.java`

- [ ] **Step 1: Write failing analysis tests**

Cover:

```text
submitted feedback moves RECEIVED -> ANALYZING -> REVIEW_PENDING
text answers produce structured findings
analysis failure moves submission to ANALYSIS_FAILED
original text is not returned by analysis DTOs
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=FeedbackAnalysisServiceTest test`

Expected: FAIL because analysis service does not exist.

- [ ] **Step 3: Define provider-independent result**

```java
public record FeedbackAnalysisResult(
    Sentiment overallSentiment,
    String summary,
    boolean piiDetected,
    RiskLevel riskLevel,
    List<FindingResult> findings
) {}
```

`FeedbackAnalysisGateway` accepts only the required text answers and returns
this record.

- [ ] **Step 4: Implement deterministic demo provider**

Activate when `anonytix.ai.provider=demo`. It categorizes known demo phrases
such as workload, leadership, communication, and teamwork so the full product
works without an API key.

- [ ] **Step 5: Implement OpenAI provider**

Activate when `anonytix.ai.provider=openai`. Use Spring AI `ChatClient` and:

```java
FeedbackAnalysisResult result = chatClient.prompt()
    .system(SYSTEM_PROMPT)
    .user(serializedFeedback)
    .call()
    .entity(FeedbackAnalysisResult.class);
```

The prompt requires removal of names, roles, exact dates, unique incidents, and
identifying writing details. Do not log prompts or raw responses.

- [ ] **Step 6: Run test and verify GREEN**

Run: `mvn -Dtest=FeedbackAnalysisServiceTest test`

Expected: PASS using the demo provider.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/de/anonytix/analysis src/test/java/de/anonytix/analysis
git commit -m "feat: add AI feedback analysis"
```

## Task 8: Moderation API

**Files:**
- Create: all `moderation` module files listed in File Structure
- Test: `src/test/java/de/anonytix/moderation/ModerationApiTest.java`

- [ ] **Step 1: Write failing moderation tests**

Cover:

```text
GET REVIEW_PENDING queue returns summaries
GET detail returns anonymized findings but no original text
POST approve changes submission to APPROVED
POST reject requires reason and changes submission to REJECTED
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=ModerationApiTest test`

Expected: FAIL with 404.

- [ ] **Step 3: Implement moderation service and endpoints**

Only submissions in `REVIEW_PENDING` may be approved or rejected. Persist every
decision in `moderation_reviews`.

- [ ] **Step 4: Run test and verify GREEN**

Run: `mvn -Dtest=ModerationApiTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/de/anonytix/moderation src/test/java/de/anonytix/moderation
git commit -m "feat: add feedback moderation"
```

## Task 9: Dashboard Aggregations

**Files:**
- Create: all `dashboard` module files listed in File Structure
- Test: `src/test/java/de/anonytix/dashboard/DashboardApiTest.java`

- [ ] **Step 1: Write failing dashboard tests**

Seed approved and rejected submissions across departments. Assert:

```text
only APPROVED submissions are included
overall score, sentiment, categories, trends, and topics are calculated
department with sampleSize < minimumGroupSize is suppressed
no raw text or submission ID appears in company dashboard responses
response matches frontend mock field names
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=DashboardApiTest test`

Expected: FAIL with 404.

- [ ] **Step 3: Implement aggregate queries**

Use `NamedParameterJdbcTemplate` for grouped read queries rather than loading
entities into memory. Every department query must apply:

```sql
HAVING COUNT(DISTINCT fs.id) >= :minimumGroupSize
```

Calculate dashboard data on read for the MVP; do not add a metric cache table.

- [ ] **Step 4: Implement response assembly**

Return the exact top-level fields from
`frontend-mocks/dashboard-overview.json` and
`frontend-mocks/department-dashboard.json`.

- [ ] **Step 5: Run test and verify GREEN**

Run: `mvn -Dtest=DashboardApiTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/de/anonytix/dashboard src/test/java/de/anonytix/dashboard
git commit -m "feat: add privacy-safe dashboard aggregates"
```

## Task 10: Modulith Verification, Demo Data, and End-to-End Verification

**Files:**
- Create: `src/test/java/de/anonytix/ArchitectureTest.java`
- Create: `src/main/resources/db/migration/V2__insert_demo_data.sql`
- Modify: `README.md`

- [ ] **Step 1: Write failing architecture test**

```java
class ArchitectureTest {

    @Test
    void verifiesModuleBoundaries() {
        ApplicationModules.of(AnonytixApplication.class).verify();
    }
}
```

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -Dtest=ArchitectureTest test`

Expected: FAIL if a module imports another module's internal package.

- [ ] **Step 3: Expose only required module APIs**

Use public service interfaces or domain events between modules. Keep entities,
repositories, and web DTOs internal to their owning module.

- [ ] **Step 4: Add deterministic demo data**

V2 inserts:

- one demo company,
- four departments,
- one published survey with six questions,
- campaigns from January through June 2026,
- approved aggregate-ready submissions,
- analyses, findings, and action items matching the frontend mocks.

Do not insert employee names, emails, or raw identifying text.

- [ ] **Step 5: Document local startup**

README commands:

```bash
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

Document demo IDs, invitation token, Swagger/OpenAPI location, and AI provider
switch:

```bash
AI_PROVIDER=openai OPENAI_API_KEY=... ./mvnw spring-boot:run
```

- [ ] **Step 6: Run complete verification**

Run:

```bash
./mvnw clean verify
docker compose config
docker compose up -d
curl --fail http://localhost:8080/actuator/health
```

Expected:

- Maven build succeeds.
- All tests pass.
- Compose configuration is valid.
- Health endpoint returns `{"status":"UP"}`.

- [ ] **Step 7: Commit**

```bash
git add README.md src/main/resources/db/migration/V2__insert_demo_data.sql src/test
git commit -m "test: verify modular MVP end to end"
```

