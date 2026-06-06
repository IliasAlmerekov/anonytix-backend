package de.anonytix.moderation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import de.anonytix.support.AbstractIntegrationTest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class ModerationApiTest extends AbstractIntegrationTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID DEPARTMENT_ID = UUID.randomUUID();
    private static final UUID SURVEY_ID = UUID.randomUUID();
    private static final UUID CAMPAIGN_ID = UUID.randomUUID();
    private static final UUID SUBMISSION_ID = UUID.randomUUID();
    private static final UUID ANALYSIS_ID = UUID.randomUUID();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedModerationCase() {
        cleanDatabase(jdbcTemplate);
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
                """
                INSERT INTO companies(id, name, slug, minimum_group_size, created_at, updated_at)
                VALUES (?, 'Demo GmbH', 'demo-moderation', 5, ?, ?)
                """,
                COMPANY_ID, now, now);
        jdbcTemplate.update(
                """
                INSERT INTO departments(id, company_id, name, code, active, created_at, updated_at)
                VALUES (?, ?, 'Entwicklung', 'DEV', true, ?, ?)
                """,
                DEPARTMENT_ID, COMPANY_ID, now, now);
        jdbcTemplate.update(
                """
                INSERT INTO surveys(
                    id, company_id, title, type, status, created_at, updated_at)
                VALUES (?, ?, 'Pulse', 'PULSE', 'PUBLISHED', ?, ?)
                """,
                SURVEY_ID, COMPANY_ID, now, now);
        jdbcTemplate.update(
                """
                INSERT INTO campaigns(
                    id, company_id, survey_id, name, status, starts_at, ends_at,
                    created_at, updated_at)
                VALUES (?, ?, ?, 'Juni', 'ACTIVE', ?, ?, ?, ?)
                """,
                CAMPAIGN_ID, COMPANY_ID, SURVEY_ID,
                now, Timestamp.from(Instant.now().plusSeconds(86400)), now, now);
        jdbcTemplate.update(
                """
                INSERT INTO feedback_submissions(
                    id, company_id, campaign_id, department_id, status, submitted_at)
                VALUES (?, ?, ?, ?, 'REVIEW_PENDING', ?)
                """,
                SUBMISSION_ID, COMPANY_ID, CAMPAIGN_ID, DEPARTMENT_ID, now);
        jdbcTemplate.update(
                """
                INSERT INTO ai_analyses(
                    id, submission_id, overall_sentiment, summary, pii_detected,
                    risk_level, model, created_at)
                VALUES (?, ?, 'NEGATIVE', 'Hohe Arbeitsbelastung erkannt.', false,
                    'HIGH', 'demo-analysis-v1', ?)
                """,
                ANALYSIS_ID, SUBMISSION_ID, now);
        jdbcTemplate.update(
                """
                INSERT INTO analysis_findings(
                    id, analysis_id, category, label, sentiment, priority, score,
                    anonymized_text)
                VALUES (?, ?, 'WORKLOAD', 'Hohe Arbeitsbelastung', 'NEGATIVE',
                    'HIGH', 0.92, 'Die Arbeitslast wird als zu hoch beschrieben.')
                """,
                UUID.randomUUID(), ANALYSIS_ID);
    }

    @Test
    void listsDetailsAndApprovesModerationCase() {
        ResponseEntity<JsonNode> queue = restTemplate.getForEntity(
                "/api/v1/platform/moderation/submissions?status=REVIEW_PENDING",
                JsonNode.class);
        assertThat(queue.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(queue.getBody()).hasSize(1);
        assertThat(queue.getBody().get(0).path("riskLevel").asText())
                .isEqualTo("HIGH");

        ResponseEntity<JsonNode> detail = restTemplate.getForEntity(
                "/api/v1/platform/moderation/submissions/{submissionId}",
                JsonNode.class,
                SUBMISSION_ID);
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detail.getBody().path("findings")).hasSize(1);
        assertThat(detail.getBody().path("findings").get(0)
                .path("anonymizedText").asText())
                .contains("Arbeitslast");

        ResponseEntity<JsonNode> approved = restTemplate.postForEntity(
                "/api/v1/platform/moderation/submissions/{submissionId}/approve",
                null,
                JsonNode.class,
                SUBMISSION_ID);
        assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approved.getBody().path("status").asText()).isEqualTo("APPROVED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM moderation_reviews", Integer.class)).isEqualTo(1);
    }

    @Test
    void rejectsModerationCaseWithReason() {
        ResponseEntity<JsonNode> rejected = restTemplate.postForEntity(
                "/api/v1/platform/moderation/submissions/{submissionId}/reject",
                Map.of("reason", "Enthält noch identifizierende Angaben."),
                JsonNode.class,
                SUBMISSION_ID);

        assertThat(rejected.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rejected.getBody().path("status").asText()).isEqualTo("REJECTED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reason FROM moderation_reviews", String.class))
                .contains("identifizierende");
    }
}
