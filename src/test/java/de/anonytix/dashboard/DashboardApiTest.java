package de.anonytix.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import de.anonytix.support.AbstractIntegrationTest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class DashboardApiTest extends AbstractIntegrationTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID VISIBLE_DEPARTMENT_ID = UUID.randomUUID();
    private static final UUID HIDDEN_DEPARTMENT_ID = UUID.randomUUID();
    private static final UUID SURVEY_ID = UUID.randomUUID();
    private static final UUID QUESTION_ID = UUID.randomUUID();
    private static final UUID CAMPAIGN_ID = UUID.randomUUID();
    private static final UUID HISTORICAL_CAMPAIGN_ID = UUID.randomUUID();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDashboardData() {
        cleanDatabase(jdbcTemplate);
        Timestamp now = Timestamp.from(Instant.parse("2026-06-06T12:00:00Z"));
        jdbcTemplate.update(
                """
                INSERT INTO companies(id, name, slug, minimum_group_size, created_at, updated_at)
                VALUES (?, 'Anonytix Demo GmbH', 'dashboard-demo', 5, ?, ?)
                """,
                COMPANY_ID, now, now);
        insertDepartment(VISIBLE_DEPARTMENT_ID, "Entwicklung", "DEV", now);
        insertDepartment(HIDDEN_DEPARTMENT_ID, "Personal", "HR", now);
        jdbcTemplate.update(
                """
                INSERT INTO surveys(
                    id, company_id, title, type, status, created_at, updated_at)
                VALUES (?, ?, 'Pulse', 'PULSE', 'PUBLISHED', ?, ?)
                """,
                SURVEY_ID, COMPANY_ID, now, now);
        jdbcTemplate.update(
                """
                INSERT INTO questions(
                    id, survey_id, category, text, type, source, required, position,
                    minimum_value, maximum_value, created_at, updated_at)
                VALUES (?, ?, 'WORKLOAD', 'Wie zufrieden bist du?', 'RATING',
                    'STANDARD', true, 1, 1, 5, ?, ?)
                """,
                QUESTION_ID, SURVEY_ID, now, now);
        jdbcTemplate.update(
                """
                INSERT INTO campaigns(
                    id, company_id, survey_id, name, status, starts_at, ends_at,
                    created_at, updated_at)
                VALUES (?, ?, ?, 'Juni 2026', 'ACTIVE', ?, ?, ?, ?)
                """,
                CAMPAIGN_ID, COMPANY_ID, SURVEY_ID,
                now, Timestamp.from(Instant.parse("2026-06-30T22:00:00Z")), now, now);
        jdbcTemplate.update(
                """
                INSERT INTO campaigns(
                    id, company_id, survey_id, name, status, starts_at, ends_at,
                    created_at, updated_at)
                VALUES (?, ?, ?, 'Juni 2025', 'CLOSED', ?, ?, ?, ?)
                """,
                HISTORICAL_CAMPAIGN_ID,
                COMPANY_ID,
                SURVEY_ID,
                Timestamp.from(Instant.parse("2025-06-01T00:00:00Z")),
                Timestamp.from(Instant.parse("2025-06-30T22:00:00Z")),
                Timestamp.from(Instant.parse("2025-06-01T00:00:00Z")),
                Timestamp.from(Instant.parse("2025-06-30T22:00:00Z")));
        insertApprovedSubmission(
                HISTORICAL_CAMPAIGN_ID,
                VISIBLE_DEPARTMENT_ID,
                3,
                "NEGATIVE",
                "Historische Arbeitsbelastung",
                "WORKLOAD",
                "HIGH",
                Timestamp.from(Instant.parse("2025-06-15T12:00:00Z")));

        for (int index = 0; index < 5; index++) {
            insertApprovedSubmission(
                    VISIBLE_DEPARTMENT_ID,
                    4,
                    "POSITIVE",
                    "Guter Teamzusammenhalt",
                    "TEAMWORK",
                    "LOW",
                    now);
        }
        for (int index = 0; index < 4; index++) {
            insertApprovedSubmission(
                    HIDDEN_DEPARTMENT_ID,
                    2,
                    "NEGATIVE",
                    "Hohe Arbeitsbelastung",
                    "WORKLOAD",
                    "HIGH",
                    now);
        }
    }

    @Test
    void returnsApprovedAggregatesAndSuppressesSmallDepartments() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/v1/companies/{companyId}/dashboard/overview?campaignId={campaignId}",
                JsonNode.class,
                COMPANY_ID,
                CAMPAIGN_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body.path("sampleSize").asInt()).isEqualTo(9);
        assertThat(body.path("minimumGroupSize").asInt()).isEqualTo(5);
        assertThat(body.path("years")).extracting(JsonNode::asInt)
                .containsExactly(2026);
        assertThat(body.path("sentimentDistribution")).hasSize(3);
        JsonNode hidden = findDepartment(body, HIDDEN_DEPARTMENT_ID);
        assertThat(hidden.path("suppressed").asBoolean()).isTrue();
        assertThat(hidden.path("suppressionReason").asText())
                .isEqualTo("MINIMUM_GROUP_SIZE_NOT_REACHED");
        assertThat(hidden.path("scores").isNull()).isTrue();
        JsonNode visible = findDepartment(body, VISIBLE_DEPARTMENT_ID);
        assertThat(visible.path("suppressed").asBoolean()).isFalse();
        assertThat(visible.path("scores").path("WORKLOAD").asDouble()).isEqualTo(4.0);
    }

    @Test
    void suppressesDepartmentDrilldownBelowMinimumGroupSize() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                """
                /api/v1/companies/{companyId}/dashboard/departments/{departmentId}\
                ?campaignId={campaignId}\
                """,
                JsonNode.class,
                COMPANY_ID,
                HIDDEN_DEPARTMENT_ID,
                CAMPAIGN_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("visible").asBoolean()).isFalse();
        assertThat(response.getBody().path("overallSatisfaction").isNull()).isTrue();
        assertThat(response.getBody().path("topTopics")).isEmpty();
    }

    @Test
    void reloadsDashboardForSelectedYearWithoutCampaignId() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/v1/companies/{companyId}/dashboard/overview?year=2025",
                JsonNode.class,
                COMPANY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body.path("campaign").path("id").asText())
                .isEqualTo(HISTORICAL_CAMPAIGN_ID.toString());
        assertThat(body.path("selectedYear").asInt()).isEqualTo(2025);
        assertThat(body.path("sampleSize").asInt()).isEqualTo(1);
        assertThat(body.path("years")).extracting(JsonNode::asInt)
                .containsExactly(2025, 2026);
        assertThat(body.path("feedbackByMonth")).hasSize(1);
        assertThat(body.path("feedbackByMonth").get(0).path("period").asText())
                .isEqualTo("2025-06");
        assertThat(body.path("feedbackByMonth").get(0).path("negative").asInt())
                .isEqualTo(1);
        assertThat(body.path("satisfactionByYear")).hasSize(12);
        assertThat(body.path("aiHighlights")).isNotEmpty();
    }

    private JsonNode findDepartment(JsonNode body, UUID departmentId) {
        for (JsonNode department : body.path("departmentHeatmap")) {
            if (department.path("departmentId").asText().equals(departmentId.toString())) {
                return department;
            }
        }
        throw new AssertionError("Abteilung fehlt im Dashboard");
    }

    private void insertDepartment(UUID id, String name, String code, Timestamp now) {
        jdbcTemplate.update(
                """
                INSERT INTO departments(id, company_id, name, code, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, true, ?, ?)
                """,
                id, COMPANY_ID, name, code, now, now);
    }

    private void insertApprovedSubmission(
            UUID departmentId,
            int rating,
            String sentiment,
            String topic,
            String category,
            String priority,
            Timestamp submittedAt) {
        insertApprovedSubmission(
                CAMPAIGN_ID,
                departmentId,
                rating,
                sentiment,
                topic,
                category,
                priority,
                submittedAt);
    }

    private void insertApprovedSubmission(
            UUID campaignId,
            UUID departmentId,
            int rating,
            String sentiment,
            String topic,
            String category,
            String priority,
            Timestamp submittedAt) {
        UUID submissionId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO feedback_submissions(
                    id, company_id, campaign_id, department_id, status, submitted_at)
                VALUES (?, ?, ?, ?, 'APPROVED', ?)
                """,
                submissionId, COMPANY_ID, campaignId, departmentId, submittedAt);
        jdbcTemplate.update(
                """
                INSERT INTO answers(
                    id, submission_id, question_id, numeric_value)
                VALUES (?, ?, ?, ?)
                """,
                UUID.randomUUID(), submissionId, QUESTION_ID, rating);
        jdbcTemplate.update(
                """
                INSERT INTO ai_analyses(
                    id, submission_id, overall_sentiment, summary, pii_detected,
                    risk_level, model, created_at)
                VALUES (?, ?, ?, ?, false, ?, 'demo-analysis-v1', ?)
                """,
                analysisId, submissionId, sentiment, topic,
                priority.equals("HIGH") ? "HIGH" : "LOW", submittedAt);
        jdbcTemplate.update(
                """
                INSERT INTO analysis_findings(
                    id, analysis_id, category, label, sentiment, priority, score,
                    anonymized_text)
                VALUES (?, ?, ?, ?, ?, ?, 0.90, ?)
                """,
                UUID.randomUUID(), analysisId, category, topic, sentiment, priority, topic);
    }
}
