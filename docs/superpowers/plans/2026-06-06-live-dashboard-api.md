# Live Dashboard API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the React dashboard JSON mocks with database-backed APIs whose campaign and year selections trigger new HTTP requests.

**Architecture:** Keep the existing dashboard endpoints and extend them with optional `campaignId` and `year` query parameters. The backend resolves a suitable campaign when none is supplied and calculates monthly, yearly, KPI, sentiment, AI, department, and trend data directly from approved submissions. The frontend keeps its existing charts but reloads the overview whenever the main campaign or year selection changes.

**Tech Stack:** Spring Boot 3.5, Spring MVC, JdbcTemplate, PostgreSQL, Flyway, React 19, TypeScript, Vite, Recharts, Vitest.

---

### Task 1: Optional Dashboard Filters

**Files:**
- Modify: `src/test/java/de/anonytix/dashboard/DashboardApiTest.java`
- Modify: `src/main/java/de/anonytix/dashboard/web/DashboardController.java`
- Modify: `src/main/java/de/anonytix/dashboard/service/DashboardService.java`
- Modify: `src/main/java/de/anonytix/dashboard/repository/DashboardQueryRepository.java`

- [ ] Add an integration test proving that `/dashboard/overview?year=2025` selects data from 2025 and works without `campaignId`.
- [ ] Run `./mvnw -Dtest=DashboardApiTest test` and verify the test fails.
- [ ] Make both query parameters optional and resolve the latest matching campaign.
- [ ] Apply the selected year to all snapshot aggregations.
- [ ] Run `./mvnw -Dtest=DashboardApiTest test` and verify the test passes.

### Task 2: Chart Data Contract

**Files:**
- Modify: `src/test/java/de/anonytix/dashboard/DashboardApiTest.java`
- Modify: `src/main/java/de/anonytix/dashboard/dto/DashboardOverviewResponse.java`
- Modify: `src/main/java/de/anonytix/dashboard/dto/DashboardTypes.java`
- Modify: `src/main/java/de/anonytix/dashboard/service/DashboardService.java`
- Modify: `src/main/java/de/anonytix/dashboard/repository/DashboardQueryRepository.java`

- [ ] Add assertions for `selectedYear`, `years`, `feedbackByMonth`, `satisfactionByYear`, and `aiHighlights`.
- [ ] Add PostgreSQL queries for available years, monthly sentiment counts, and company-wide monthly satisfaction by year.
- [ ] Map analysis findings into stable AI highlight DTOs.
- [ ] Keep all anonymity suppression rules unchanged.
- [ ] Run the dashboard integration tests.

### Task 3: React Live API Calls

**Files:**
- Modify: `src/lib/config.ts`
- Modify: `src/lib/api.ts`
- Modify: `src/lib/types.ts`
- Modify: `src/pages/DashboardPage.tsx`
- Modify: `src/pages/DepartmentPage.tsx`
- Modify: `src/lib/__tests__/api.test.ts`
- Create: `.env.example`

- [ ] Add tests for URL construction with optional `campaignId` and `year`.
- [ ] Keep non-dashboard mocks configurable, but make dashboard mocks opt-in.
- [ ] Add `listCampaigns()` using the existing campaign endpoint.
- [ ] Reload dashboard data whenever the campaign or main year dropdown changes.
- [ ] Pass the active filters into department drill-down URLs and API calls.
- [ ] Display API failures instead of an endless loading state.

### Task 4: Verification

**Files:**
- Modify only when verification exposes a defect.

- [ ] Run `./mvnw test`.
- [ ] Run `npm test`.
- [ ] Run `npm run build`.
- [ ] Start PostgreSQL and the backend.
- [ ] Call the filtered dashboard URLs and verify real JSON responses.
- [ ] Confirm that all changes remain uncommitted.
