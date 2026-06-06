package de.anonytix;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DatabaseMigrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsTheCompleteMvpSchema() {
        List<String> tableNames = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """,
                String.class);

        assertThat(tableNames).contains(
                "companies",
                "departments",
                "surveys",
                "questions",
                "question_options",
                "question_departments",
                "campaigns",
                "invitations",
                "feedback_submissions",
                "answers",
                "answer_selected_options",
                "ai_analyses",
                "analysis_findings",
                "moderation_reviews",
                "action_items");
    }

    @Test
    void seedsTheSingleTenantMvpDashboardContext() {
        Integer companies = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM companies
                WHERE id = '10729623-735e-4382-854f-33e3450bdac7'
                """,
                Integer.class);
        Integer campaigns = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM campaigns
                WHERE id = '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'
                  AND status = 'ACTIVE'
                """,
                Integer.class);

        assertThat(companies).isEqualTo(1);
        assertThat(campaigns).isEqualTo(1);
    }
}
