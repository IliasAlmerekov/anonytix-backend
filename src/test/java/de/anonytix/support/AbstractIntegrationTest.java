package de.anonytix.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16");

    protected void cleanDatabase(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("DELETE FROM action_items");
        jdbcTemplate.update("DELETE FROM moderation_reviews");
        jdbcTemplate.update("DELETE FROM analysis_findings");
        jdbcTemplate.update("DELETE FROM ai_analyses");
        jdbcTemplate.update("DELETE FROM answer_selected_options");
        jdbcTemplate.update("DELETE FROM answers");
        jdbcTemplate.update("DELETE FROM feedback_submissions");
        jdbcTemplate.update("DELETE FROM invitations");
        jdbcTemplate.update("DELETE FROM campaigns");
        jdbcTemplate.update("DELETE FROM question_departments");
        jdbcTemplate.update("DELETE FROM question_options");
        jdbcTemplate.update("DELETE FROM questions");
        jdbcTemplate.update("DELETE FROM surveys");
        jdbcTemplate.update("DELETE FROM departments");
        jdbcTemplate.update("DELETE FROM companies");
    }
}
