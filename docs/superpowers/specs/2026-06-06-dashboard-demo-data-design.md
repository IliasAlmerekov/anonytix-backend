# Dashboard Demo Data Design

## Ziel

Eine Flyway-Migration stellt realistische, aber eindeutig synthetische
Dashboard-Daten für 2024, 2025 und 2026 bereit. Das React-Dashboard liest diese
Daten ausschließlich über die bestehenden Spring-Boot-Endpunkte aus
PostgreSQL.

## Datenumfang

- Eine veröffentlichte Mitarbeiterbefragung mit fünf Bewertungsfragen und
  einer Freitextfrage.
- Je eine Kampagne für 2024, 2025 und 2026.
- Vier Abteilungen: Softwareentwicklung, Vertrieb, Marketing und Personal.
- Pro Jahr mehrere Erhebungsmonate und mindestens sechs freigegebene
  Rückmeldungen je Abteilung und Monat.
- Zu jeder Rückmeldung Bewertungsantworten, anonymisierter Freitext, eine
  synthetische KI-Analyse und ein Analyse-Finding.
- Unterschiedliche Werte je Jahr, Monat und Abteilung, damit KPI-Karten,
  Monatsverläufe, Jahresvergleich, Stimmungen und Heatmap sichtbar variieren.

## Sicherheit

Die Inhalte enthalten keine echten Personen, Kontaktdaten oder
personenbezogenen Angaben. Alle IDs sind deterministisch. V2 bleibt
unverändert; V3 ergänzt nur Demo-Daten und kann von Flyway genau einmal
ausgeführt werden.

## Verifikation

Der Migrationstest prüft:

- Flyway-Schemaversion 3.
- Kampagnen und freigegebene Rückmeldungen für 2024, 2025 und 2026.
- Mindestens fünf Rückmeldungen pro Abteilung und Jahr.
- Vorhandene Antworten, KI-Analysen und Findings.

Die Dashboard-Integrationstests und ein lokaler HTTP-Aufruf prüfen anschließend
die Jahresfilter.
