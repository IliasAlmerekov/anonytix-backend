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

    @Test
    void seedsDashboardDemoDataForThreeYears() {
        List<Integer> years = jdbcTemplate.queryForList(
                """
                SELECT DISTINCT EXTRACT(YEAR FROM submitted_at)::integer
                FROM feedback_submissions
                WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
                  AND status = 'APPROVED'
                ORDER BY 1
                """,
                Integer.class);
        Integer sufficientlyLargeDepartmentYears = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM (
                    SELECT department_id,
                           EXTRACT(YEAR FROM submitted_at)::integer AS year
                    FROM feedback_submissions
                    WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
                      AND status = 'APPROVED'
                    GROUP BY department_id, EXTRACT(YEAR FROM submitted_at)
                    HAVING count(*) >= 5
                ) grouped_feedback
                """,
                Integer.class);
        Integer submissions = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM feedback_submissions",
                Integer.class);
        Integer answers = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM answers",
                Integer.class);
        Integer analyses = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ai_analyses",
                Integer.class);
        Integer findings = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM analysis_findings",
                Integer.class);

        assertThat(years).containsExactly(2024, 2025, 2026);
        assertThat(sufficientlyLargeDepartmentYears).isEqualTo(18);
        assertThat(submissions).isGreaterThanOrEqualTo(288);
        assertThat(answers).isGreaterThanOrEqualTo(submissions * 6);
        assertThat(analyses).isEqualTo(submissions);
        assertThat(findings).isGreaterThanOrEqualTo(submissions);
    }

    @Test
    void seedsExpandedEnglishDashboardDataForSixDepartments() {
        List<String> departments = jdbcTemplate.queryForList(
                """
                SELECT name
                FROM departments
                WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
                  AND active = true
                ORDER BY name
                """,
                String.class);
        Integer sufficientlyLargeDepartmentYears = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM (
                    SELECT department_id,
                           EXTRACT(YEAR FROM submitted_at)::integer AS year
                    FROM feedback_submissions
                    WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
                      AND status = 'APPROVED'
                    GROUP BY department_id, EXTRACT(YEAR FROM submitted_at)
                    HAVING count(*) >= 5
                ) grouped_feedback
                """,
                Integer.class);
        Integer coveredMonths = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM (
                    SELECT DISTINCT date_trunc('month', submitted_at)
                    FROM feedback_submissions
                    WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
                      AND status = 'APPROVED'
                      AND submitted_at < '2026-07-01T00:00:00Z'
                ) months
                """,
                Integer.class);
        Integer yearsWithMixedSentiment = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM (
                    SELECT EXTRACT(YEAR FROM fs.submitted_at)::integer AS year
                    FROM feedback_submissions fs
                    JOIN ai_analyses aa ON aa.submission_id = fs.id
                    WHERE fs.company_id = '10729623-735e-4382-854f-33e3450bdac7'
                      AND fs.status = 'APPROVED'
                    GROUP BY EXTRACT(YEAR FROM fs.submitted_at)
                    HAVING count(*) FILTER (
                               WHERE aa.overall_sentiment = 'POSITIVE') > 0
                       AND count(*) FILTER (
                               WHERE aa.overall_sentiment = 'NEGATIVE') > 0
                ) mixed_years
                """,
                Integer.class);
        Integer ratingCategories = jdbcTemplate.queryForObject(
                """
                SELECT count(DISTINCT category)
                FROM questions
                WHERE survey_id = '4e24a5f0-2f07-4b89-9cd6-861e59dc156e'
                  AND type = 'RATING'
                  AND active = true
                """,
                Integer.class);
        Integer submissions = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM feedback_submissions
                WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
                """,
                Integer.class);

        assertThat(departments).containsExactly(
                "Customer Success",
                "Engineering",
                "Marketing",
                "Operations",
                "People & Culture",
                "Sales");
        assertThat(sufficientlyLargeDepartmentYears).isEqualTo(18);
        assertThat(coveredMonths).isEqualTo(30);
        assertThat(yearsWithMixedSentiment).isEqualTo(3);
        assertThat(ratingCategories).isGreaterThanOrEqualTo(9);
        assertThat(submissions).isGreaterThanOrEqualTo(1_300);
    }
}
