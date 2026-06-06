package de.anonytix.survey;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import de.anonytix.support.AbstractIntegrationTest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class SurveyApiTest extends AbstractIntegrationTest {

    private static final UUID COMPANY_ID =
            UUID.fromString("10729623-735e-4382-854f-33e3450bdac7");
    private static final UUID DEPARTMENT_ID =
            UUID.fromString("ac38af63-dc5a-416e-b5d7-c237535ec37b");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpCompany() {
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
    void createsSurveyFromStandardTemplate() {
        ResponseEntity<JsonNode> response = createSurvey();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.path("status").asText()).isEqualTo("DRAFT");
        assertThat(body.path("questions")).hasSize(7);
        List<String> categories = StreamSupport
                .stream(body.path("questions").spliterator(), false)
                .map(question -> question.path("category").asText())
                .toList();
        assertThat(categories).contains(
                "OVERALL_SATISFACTION",
                "LEADERSHIP",
                "WORKLOAD",
                "COMMUNICATION",
                "RECOMMENDATION",
                "POSITIVE_FEEDBACK",
                "IMPROVEMENT");
    }

    @Test
    void addsDepartmentQuestionAndChangesQuestionOrder() {
        UUID surveyId = createdSurveyId();
        Map<String, Object> request = new HashMap<>();
        request.put("text", "Wie zufrieden bist du mit unseren Entwicklungsprozessen?");
        request.put("type", "RATING");
        request.put("category", "PROCESSES");
        request.put("required", false);
        request.put("minimumValue", 1);
        request.put("maximumValue", 5);
        request.put("analyzeWithAi", false);
        request.put("departmentIds", List.of(DEPARTMENT_ID));
        request.put("options", List.of());

        ResponseEntity<JsonNode> created = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/surveys/{surveyId}/questions",
                request,
                JsonNode.class,
                COMPANY_ID,
                surveyId);

        assertThat(created.getStatusCode())
                .as("API response: %s", created.getBody())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().path("departmentIds").get(0).asText())
                .isEqualTo(DEPARTMENT_ID.toString());

        JsonNode survey = restTemplate.getForObject(
                "/api/v1/companies/{companyId}/surveys/{surveyId}",
                JsonNode.class,
                COMPANY_ID,
                surveyId);
        List<String> questionIds = new ArrayList<>();
        survey.path("questions").forEach(question -> questionIds.add(question.path("id").asText()));
        java.util.Collections.reverse(questionIds);

        ResponseEntity<Void> reordered = restTemplate.exchange(
                "/api/v1/companies/{companyId}/surveys/{surveyId}/questions/order",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("questionIds", questionIds)),
                Void.class,
                COMPANY_ID,
                surveyId);

        assertThat(reordered.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        JsonNode reloaded = restTemplate.getForObject(
                "/api/v1/companies/{companyId}/surveys/{surveyId}",
                JsonNode.class,
                COMPANY_ID,
                surveyId);
        assertThat(reloaded.path("questions").get(0).path("id").asText())
                .isEqualTo(questionIds.get(0));
    }

    @Test
    void publishedSurveyCannotBeChanged() {
        UUID surveyId = createdSurveyId();

        ResponseEntity<JsonNode> published = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/surveys/{surveyId}/publish",
                null,
                JsonNode.class,
                COMPANY_ID,
                surveyId);

        assertThat(published.getStatusCode())
                .as("API response: %s", published.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(published.getBody().path("status").asText()).isEqualTo("PUBLISHED");

        ResponseEntity<JsonNode> changed = restTemplate.exchange(
                "/api/v1/companies/{companyId}/surveys/{surveyId}",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("title", "Nicht mehr änderbar")),
                JsonNode.class,
                COMPANY_ID,
                surveyId);

        assertThat(changed.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(changed.getBody().path("code").asText())
                .isEqualTo("SURVEY_ALREADY_PUBLISHED");
    }

    private ResponseEntity<JsonNode> createSurvey() {
        return restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/surveys",
                Map.of(
                        "title", "Mitarbeiterbefragung Juni 2026",
                        "description", "Monatliche anonyme Pulsbefragung",
                        "type", "PULSE",
                        "templateKey", "EMPLOYEE_SATISFACTION"),
                JsonNode.class,
                COMPANY_ID);
    }

    private UUID createdSurveyId() {
        ResponseEntity<JsonNode> response = createSurvey();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return UUID.fromString(response.getBody().path("id").asText());
    }
}
