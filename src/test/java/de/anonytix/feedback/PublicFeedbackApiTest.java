package de.anonytix.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import de.anonytix.support.AbstractIntegrationTest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class PublicFeedbackApiTest extends AbstractIntegrationTest {

    private static final UUID COMPANY_ID =
            UUID.fromString("10729623-735e-4382-854f-33e3450bdac7");
    private static final UUID DEPARTMENT_ID =
            UUID.fromString("ac38af63-dc5a-416e-b5d7-c237535ec37b");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanAndSeedCompany() {
        cleanDatabase(jdbcTemplate);
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
                """
                INSERT INTO companies(id, name, slug, minimum_group_size, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                COMPANY_ID, "Anonytix Demo GmbH", "anonytix-demo", 5, now, now);
        jdbcTemplate.update(
                """
                INSERT INTO departments(id, company_id, name, code, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                DEPARTMENT_ID, COMPANY_ID, "Softwareentwicklung", "DEV", true, now, now);
    }

    @Test
    void loadsGeneralAndDepartmentSpecificForm() {
        Fixture fixture = createActiveInvitation();

        ResponseEntity<JsonNode> general = restTemplate.getForEntity(
                "/api/v1/public/invitations/{token}/form",
                JsonNode.class,
                fixture.token());

        assertThat(general.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(general.getBody().path("selectedDepartmentId").isNull()).isTrue();
        assertThat(general.getBody().path("departments")).hasSize(1);
        assertThat(general.getBody().path("questions")).hasSize(7);

        ResponseEntity<JsonNode> selected = restTemplate.getForEntity(
                "/api/v1/public/invitations/{token}/form?departmentId={departmentId}",
                JsonNode.class,
                fixture.token(),
                DEPARTMENT_ID);

        assertThat(selected.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(selected.getBody().path("selectedDepartmentId").asText())
                .isEqualTo(DEPARTMENT_ID.toString());
        assertThat(selected.getBody().path("questions")).hasSize(8);
    }

    @Test
    void submitsFeedbackAndConsumesInvitationWithoutLinkingItToSubmission() {
        Fixture fixture = createActiveInvitation();
        JsonNode form = restTemplate.getForObject(
                "/api/v1/public/invitations/{token}/form?departmentId={departmentId}",
                JsonNode.class,
                fixture.token(),
                DEPARTMENT_ID);
        List<Map<String, Object>> answers = new java.util.ArrayList<>();
        form.path("questions").forEach(question -> {
            if (!question.path("required").asBoolean()
                    && !question.path("id").asText()
                    .equals(fixture.departmentQuestionId().toString())) {
                return;
            }
            Map<String, Object> answer = new java.util.HashMap<>();
            answer.put("questionId", question.path("id").asText());
            answer.put("numericValue", null);
            answer.put("textValue", null);
            answer.put("booleanValue", null);
            answer.put("selectedOptionIds", List.of());
            switch (question.path("type").asText()) {
                case "RATING" -> answer.put("numericValue", 4);
                case "SINGLE_CHOICE", "MULTI_CHOICE" -> answer.put(
                        "selectedOptionIds",
                        List.of(question.path("options").get(0).path("id").asText()));
                case "BOOLEAN" -> answer.put("booleanValue", true);
                case "TEXT" -> answer.put("textValue", "Anonymes Testfeedback");
                default -> throw new IllegalStateException("Unbekannter Fragetyp");
            }
            answers.add(answer);
        });

        Map<String, Object> request = Map.of(
                "departmentId", DEPARTMENT_ID.toString(),
                "answers", answers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/v1/public/invitations/{token}/submissions",
                request,
                JsonNode.class,
                fixture.token());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().path("status").asText()).isEqualTo("RECEIVED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM feedback_submissions", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM invitations", String.class)).isEqualTo("USED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM feedback_submissions", String.class))
                .isEqualTo("REVIEW_PENDING");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ai_analyses", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM analysis_findings", Integer.class))
                .isGreaterThanOrEqualTo(1);
        assertThat(jdbcTemplate.queryForList(
                """
                SELECT column_name FROM information_schema.columns
                WHERE table_name = 'feedback_submissions'
                """,
                String.class)).doesNotContain("invitation_id");

        ResponseEntity<JsonNode> reused = restTemplate.postForEntity(
                "/api/v1/public/invitations/{token}/submissions",
                request,
                JsonNode.class,
                fixture.token());
        assertThat(reused.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private Fixture createActiveInvitation() {
        JsonNode survey = restTemplate.postForObject(
                "/api/v1/companies/{companyId}/surveys",
                Map.of(
                        "title", "Mitarbeiterbefragung Juni 2026",
                        "description", "Dein Feedback wird anonym ausgewertet.",
                        "type", "PULSE",
                        "templateKey", "EMPLOYEE_SATISFACTION"),
                JsonNode.class,
                COMPANY_ID);
        UUID surveyId = UUID.fromString(survey.path("id").asText());
        UUID ratingQuestionId = UUID.fromString(
                survey.path("questions").get(0).path("id").asText());

        java.util.Map<String, Object> departmentQuestionRequest = new java.util.HashMap<>();
        departmentQuestionRequest.put(
                "text",
                "Wie zufrieden bist du mit unseren Entwicklungsprozessen?");
        departmentQuestionRequest.put("helpText", null);
        departmentQuestionRequest.put("type", "RATING");
        departmentQuestionRequest.put("category", "PROCESSES");
        departmentQuestionRequest.put("required", false);
        departmentQuestionRequest.put("minimumValue", 1);
        departmentQuestionRequest.put("maximumValue", 5);
        departmentQuestionRequest.put("maximumLength", null);
        departmentQuestionRequest.put("analyzeWithAi", false);
        departmentQuestionRequest.put("departmentIds", List.of(DEPARTMENT_ID));
        departmentQuestionRequest.put("options", List.of());
        ResponseEntity<JsonNode> departmentQuestionResponse = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/surveys/{surveyId}/questions",
                departmentQuestionRequest,
                JsonNode.class,
                COMPANY_ID,
                surveyId);
        assertThat(departmentQuestionResponse.getStatusCode())
                .as("Department question response: %s", departmentQuestionResponse.getBody())
                .isEqualTo(HttpStatus.CREATED);
        JsonNode departmentQuestion = departmentQuestionResponse.getBody();
        assertThat(departmentQuestion).isNotNull();
        UUID departmentQuestionId =
                UUID.fromString(departmentQuestion.path("id").asText());
        restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/surveys/{surveyId}/publish",
                null,
                JsonNode.class,
                COMPANY_ID,
                surveyId);

        Instant startsAt = Instant.now().minus(1, ChronoUnit.HOURS);
        JsonNode campaign = restTemplate.postForObject(
                "/api/v1/companies/{companyId}/campaigns",
                Map.of(
                        "surveyId", surveyId.toString(),
                        "name", "Mitarbeiterbefragung Juni 2026",
                        "startsAt", startsAt.toString(),
                        "endsAt", startsAt.plus(14, ChronoUnit.DAYS).toString()),
                JsonNode.class,
                COMPANY_ID);
        UUID campaignId = UUID.fromString(campaign.path("id").asText());
        restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/campaigns/{campaignId}/activate",
                null,
                JsonNode.class,
                COMPANY_ID,
                campaignId);
        JsonNode invitation = restTemplate.postForObject(
                "/api/v1/companies/{companyId}/campaigns/{campaignId}/invitations",
                Map.of("expiresAt", startsAt.plus(13, ChronoUnit.DAYS).toString()),
                JsonNode.class,
                COMPANY_ID,
                campaignId);
        return new Fixture(
                invitation.path("token").asText(),
                ratingQuestionId,
                departmentQuestionId);
    }

    private record Fixture(
            String token,
            UUID ratingQuestionId,
            UUID departmentQuestionId) {
    }
}
