# anonytix-backend

## OpenAI-Analyse

Das Docker-Image startet standardmäßig mit dem Spring-Profil `openai`.
Für das Deployment muss deshalb nur dieser geheime Wert gesetzt werden:

```text
OPENAI_API_KEY=...
```

Optional kann das Modell über `OPENAI_MODEL` geändert werden. Standard ist
`gpt-4o-mini`. Die Analyse verwendet strukturierte JSON-Ausgaben, speichert das
Ergebnis in PostgreSQL und setzt die Abgabe anschließend auf Moderationsprüfung.

Lokal bleibt ohne Profil der deterministische Demo-Analyzer aktiv. OpenAI kann
lokal so gestartet werden:

```bash
SPRING_PROFILES_ACTIVE=openai OPENAI_API_KEY=... ./mvnw spring-boot:run
```

Vor dem API-Aufruf entfernt das Backend offensichtliche E-Mail-Adressen,
Telefonnummern, Anreden mit Namen, Links und Benutzernamen. Das reduziert das
Risiko, ist aber keine Garantie für vollständige Anonymität. Ergebnisse müssen
vor der Freigabe weiterhin moderiert werden.
