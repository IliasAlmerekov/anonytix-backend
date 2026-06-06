package de.anonytix.campaign;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import de.anonytix.support.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class CampaignApiTest extends AbstractIntegrationTest {

    private static final UUID COMPANY_ID =
            UUID.fromString("10729623-735e-4382-854f-33e3450bdac7");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpCompany() {
        jdbcTemplate.update("DELETE FROM invitations");
        jdbcTemplate.update("DELETE FROM campaigns");
        jdbcTemplate.update("DELETE FROM question_departments");
        jdbcTemplate.update("DELETE FROM question_options");
        jdbcTemplate.update("DELETE FROM questions");
        jdbcTemplate.update("DELETE FROM surveys");
        jdbcTemplate.update("DELETE FROM departments");
        jdbcTemplate.update("DELETE FROM companies");
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
                """
                INSERT INTO companies(id, name, slug, minimum_group_size, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                COMPANY_ID, "Anonytix Demo GmbH", "anonytix-demo", 5, now, now);
    }

    @Test
    void rejectsCampaignForDraftSurvey() {
        UUID surveyId = createSurvey(false);

        ResponseEntity<JsonNode> response = createCampaign(surveyId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().path("code").asText())
                .isEqualTo("SURVEY_NOT_PUBLISHED");
    }

    @Test
    void createsActivatesAndGeneratesHashedGeneralInvitation() throws Exception {
        UUID surveyId = createSurvey(true);
        ResponseEntity<JsonNode> created = createCampaign(surveyId);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().path("status").asText()).isEqualTo("SCHEDULED");
        UUID campaignId = UUID.fromString(created.getBody().path("id").asText());

        ResponseEntity<JsonNode> activated = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/campaigns/{campaignId}/activate",
                null,
                JsonNode.class,
                COMPANY_ID,
                campaignId);

        assertThat(activated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activated.getBody().path("status").asText()).isEqualTo("ACTIVE");

        Instant expiresAt = Instant.now().plus(14, ChronoUnit.DAYS);
        ResponseEntity<JsonNode> invitation = restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/campaigns/{campaignId}/invitations",
                Map.of("expiresAt", expiresAt.toString()),
                JsonNode.class,
                COMPANY_ID,
                campaignId);

        assertThat(invitation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String token = invitation.getBody().path("token").asText();
        assertThat(token).hasSizeGreaterThanOrEqualTo(40);
        assertThat(invitation.getBody().path("url").asText())
                .isEqualTo("https://anonytix.app/feedback/" + token);

        String storedHash = jdbcTemplate.queryForObject(
                "SELECT token_hash FROM invitations WHERE campaign_id = ?",
                String.class,
                campaignId);
        String expectedHash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(token.getBytes(StandardCharsets.UTF_8)));

        assertThat(storedHash).isEqualTo(expectedHash);
        assertThat(storedHash).doesNotContain(token);
    }

    private UUID createSurvey(boolean publish) {
        JsonNode survey = restTemplate.postForObject(
                "/api/v1/companies/{companyId}/surveys",
                Map.of(
                        "title", "Mitarbeiterbefragung Juni 2026",
                        "description", "Monatliche anonyme Pulsbefragung",
                        "type", "PULSE",
                        "templateKey", "EMPLOYEE_SATISFACTION"),
                JsonNode.class,
                COMPANY_ID);
        UUID surveyId = UUID.fromString(survey.path("id").asText());
        if (publish) {
            restTemplate.postForEntity(
                    "/api/v1/companies/{companyId}/surveys/{surveyId}/publish",
                    null,
                    JsonNode.class,
                    COMPANY_ID,
                    surveyId);
        }
        return surveyId;
    }

    private ResponseEntity<JsonNode> createCampaign(UUID surveyId) {
        Instant startsAt = Instant.now().plus(1, ChronoUnit.HOURS);
        return restTemplate.postForEntity(
                "/api/v1/companies/{companyId}/campaigns",
                Map.of(
                        "surveyId", surveyId.toString(),
                        "name", "Mitarbeiterbefragung Juli 2026",
                        "startsAt", startsAt.toString(),
                        "endsAt", startsAt.plus(14, ChronoUnit.DAYS).toString()),
                JsonNode.class,
                COMPANY_ID);
    }
}
