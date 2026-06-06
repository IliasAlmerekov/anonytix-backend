# Dashboard Demo Data Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** PostgreSQL über Flyway mit realistischen Dashboard-Demodaten für 2024, 2025 und 2026 befüllen.

**Architecture:** Eine neue additive Migration V3 verwendet die bereits durch V2 angelegten Firmen-, Abteilungs-, Umfrage- und Fragedaten. Deterministische UUIDs und mengenbasierte SQL-Statements erzeugen Kampagnen, freigegebene Rückmeldungen, Antworten und synthetische Analyseergebnisse ohne neue Tabellen.

**Tech Stack:** PostgreSQL 16, Flyway, Spring Boot 3.5, JdbcTemplate, JUnit 5, Testcontainers

---

### Task 1: Migrationserwartungen absichern

**Files:**
- Modify: `src/test/java/de/anonytix/DatabaseMigrationTest.java`

- [ ] Ergänze einen Test, der drei Jahre, ausreichende Gruppengrößen sowie Antworten und Analyseergebnisse erwartet.
- [ ] Führe `./mvnw -Dtest=DatabaseMigrationTest test` aus und bestätige, dass der neue Test wegen der fehlenden V3-Daten fehlschlägt.

### Task 2: V3-Demodaten erzeugen

**Files:**
- Create: `src/main/resources/db/migration/V3__seed_dashboard_demo_data.sql`

- [ ] Lege Kampagnen für 2024 und 2025 an und verwende die vorhandene Kampagne für 2026.
- [ ] Erzeuge mit `generate_series` deterministische Rückmeldungen für vier Abteilungen und mehrere Monate.
- [ ] Erzeuge fünf Bewertungsantworten und eine anonymisierte Freitextantwort je Rückmeldung.
- [ ] Erzeuge eine synthetische Analyse und ein Finding je Rückmeldung.
- [ ] Führe den Migrationstest erneut aus und bestätige Grün.

### Task 3: Gesamtsystem verifizieren

**Files:**
- Verify: `src/test/java/de/anonytix/dashboard/DashboardApiTest.java`

- [ ] Führe `./mvnw test` aus.
- [ ] Baue den JAR mit `./mvnw -DskipTests package`.
- [ ] Starte das Backend gegen PostgreSQL und prüfe Dashboard-Aufrufe für `year=2024`, `year=2025` und `year=2026`.
- [ ] Prüfe `git diff --check` und lasse alle Änderungen uncommitted.
