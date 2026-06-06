package de.anonytix.dashboard.repository;

import de.anonytix.dashboard.dto.DashboardTypes.ActionItem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<DashboardContext> findContext(UUID companyId, UUID campaignId) {
        return jdbcTemplate.query(
                """
                SELECT c.id AS company_id, c.name AS company_name, c.minimum_group_size,
                       ca.id AS campaign_id, ca.name AS campaign_name,
                       ca.starts_at, ca.ends_at
                FROM companies c
                JOIN campaigns ca ON ca.company_id = c.id
                WHERE c.id = ? AND ca.id = ?
                """,
                (resultSet, rowNumber) -> new DashboardContext(
                        resultSet.getObject("company_id", UUID.class),
                        resultSet.getString("company_name"),
                        resultSet.getInt("minimum_group_size"),
                        resultSet.getObject("campaign_id", UUID.class),
                        resultSet.getString("campaign_name"),
                        resultSet.getTimestamp("starts_at").toInstant(),
                        resultSet.getTimestamp("ends_at").toInstant()),
                companyId,
                campaignId).stream().findFirst();
    }

    public Optional<DepartmentInfo> findDepartment(UUID companyId, UUID departmentId) {
        return jdbcTemplate.query(
                """
                SELECT id, name
                FROM departments
                WHERE company_id = ? AND id = ? AND active = true
                """,
                (resultSet, rowNumber) -> new DepartmentInfo(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("name")),
                companyId,
                departmentId).stream().findFirst();
    }

    public int sampleSize(UUID companyId, UUID campaignId, UUID departmentId) {
        String departmentFilter =
                departmentId == null ? "" : " AND fs.department_id = ?\n";
        Object[] parameters = departmentId == null
                ? new Object[] {companyId, campaignId}
                : new Object[] {companyId, campaignId, departmentId};
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM feedback_submissions fs
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED'
                """ + departmentFilter,
                Integer.class,
                parameters);
        return count == null ? 0 : count;
    }

    public Double overallSatisfaction(
            UUID companyId,
            UUID campaignId,
            UUID departmentId) {
        String departmentFilter =
                departmentId == null ? "" : " AND fs.department_id = ?\n";
        Object[] parameters = departmentId == null
                ? new Object[] {companyId, campaignId}
                : new Object[] {companyId, campaignId, departmentId};
        return jdbcTemplate.queryForObject(
                """
                SELECT CAST(AVG(a.numeric_value) AS double precision)
                FROM feedback_submissions fs
                JOIN answers a ON a.submission_id = fs.id
                JOIN questions q ON q.id = a.question_id
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED' AND q.type = 'RATING'
                """ + departmentFilter,
                Double.class,
                parameters);
    }

    public List<CategoryAggregate> categoryScores(
            UUID companyId,
            UUID campaignId,
            UUID departmentId) {
        String departmentFilter =
                departmentId == null ? "" : " AND fs.department_id = ?\n";
        Object[] parameters = departmentId == null
                ? new Object[] {companyId, campaignId}
                : new Object[] {companyId, campaignId, departmentId};
        return jdbcTemplate.query(
                """
                SELECT q.category,
                       CAST(AVG(a.numeric_value) AS double precision) AS score,
                       count(DISTINCT fs.id) AS sample_size
                FROM feedback_submissions fs
                JOIN answers a ON a.submission_id = fs.id
                JOIN questions q ON q.id = a.question_id
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED' AND q.type = 'RATING'
                """ + departmentFilter + """
                GROUP BY q.category
                ORDER BY q.category
                """,
                (resultSet, rowNumber) -> new CategoryAggregate(
                        resultSet.getString("category"),
                        resultSet.getDouble("score"),
                        resultSet.getInt("sample_size")),
                parameters);
    }

    public List<SentimentAggregate> sentiments(UUID companyId, UUID campaignId) {
        return jdbcTemplate.query(
                """
                SELECT aa.overall_sentiment AS sentiment, count(*) AS amount
                FROM feedback_submissions fs
                JOIN ai_analyses aa ON aa.submission_id = fs.id
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED'
                GROUP BY aa.overall_sentiment
                """,
                (resultSet, rowNumber) -> new SentimentAggregate(
                        resultSet.getString("sentiment"),
                        resultSet.getInt("amount")),
                companyId,
                campaignId);
    }

    public List<DepartmentAggregate> departments(UUID companyId, UUID campaignId) {
        return jdbcTemplate.query(
                """
                SELECT d.id, d.name, count(fs.id) AS sample_size
                FROM departments d
                LEFT JOIN feedback_submissions fs
                  ON fs.department_id = d.id
                 AND fs.company_id = d.company_id
                 AND fs.campaign_id = ?
                 AND fs.status = 'APPROVED'
                WHERE d.company_id = ? AND d.active = true
                GROUP BY d.id, d.name
                ORDER BY d.name
                """,
                (resultSet, rowNumber) -> new DepartmentAggregate(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("name"),
                        resultSet.getInt("sample_size")),
                campaignId,
                companyId);
    }

    public List<TopicAggregate> topTopics(
            UUID companyId,
            UUID campaignId,
            UUID departmentId) {
        String departmentFilter =
                departmentId == null ? "" : " AND fs.department_id = ?\n";
        Object[] parameters = departmentId == null
                ? new Object[] {companyId, campaignId}
                : new Object[] {companyId, campaignId, departmentId};
        return jdbcTemplate.query(
                """
                SELECT af.category, af.label, af.sentiment, af.priority,
                       count(*) AS mentions
                FROM feedback_submissions fs
                JOIN ai_analyses aa ON aa.submission_id = fs.id
                JOIN analysis_findings af ON af.analysis_id = aa.id
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED'
                """ + departmentFilter + """
                GROUP BY af.category, af.label, af.sentiment, af.priority
                ORDER BY mentions DESC, af.label
                LIMIT 5
                """,
                (resultSet, rowNumber) -> new TopicAggregate(
                        resultSet.getString("category"),
                        resultSet.getString("label"),
                        resultSet.getString("sentiment"),
                        resultSet.getString("priority"),
                        resultSet.getInt("mentions")),
                parameters);
    }

    public List<TrendAggregate> trend(
            UUID companyId,
            UUID campaignId,
            UUID departmentId) {
        String departmentFilter =
                departmentId == null ? "" : " AND fs.department_id = ?\n";
        Object[] parameters = departmentId == null
                ? new Object[] {companyId, campaignId}
                : new Object[] {companyId, campaignId, departmentId};
        return jdbcTemplate.query(
                """
                SELECT to_char(date_trunc('month', fs.submitted_at), 'YYYY-MM') AS period,
                       CAST(AVG(a.numeric_value) AS double precision) AS score,
                       count(DISTINCT fs.id) AS sample_size
                FROM feedback_submissions fs
                JOIN answers a ON a.submission_id = fs.id
                JOIN questions q ON q.id = a.question_id
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED' AND q.type = 'RATING'
                """ + departmentFilter + """
                GROUP BY date_trunc('month', fs.submitted_at)
                ORDER BY date_trunc('month', fs.submitted_at)
                """,
                (resultSet, rowNumber) -> new TrendAggregate(
                        resultSet.getString("period"),
                        resultSet.getDouble("score"),
                        resultSet.getInt("sample_size")),
                parameters);
    }

    public List<ActionItem> actionItems(UUID companyId, UUID campaignId) {
        return jdbcTemplate.query(
                """
                SELECT id, title, description, category, priority, status, source
                FROM action_items
                WHERE company_id = ? AND (campaign_id = ? OR campaign_id IS NULL)
                ORDER BY
                  CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END,
                  created_at DESC
                LIMIT 10
                """,
                (resultSet, rowNumber) -> new ActionItem(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("category"),
                        resultSet.getString("priority"),
                        resultSet.getString("status"),
                        resultSet.getString("source")),
                companyId,
                campaignId);
    }

    public Instant latestAnalysisAt(UUID companyId, UUID campaignId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT max(aa.created_at)
                FROM feedback_submissions fs
                JOIN ai_analyses aa ON aa.submission_id = fs.id
                WHERE fs.company_id = ? AND fs.campaign_id = ?
                  AND fs.status = 'APPROVED'
                """,
                (resultSet, rowNumber) -> {
                    var timestamp = resultSet.getTimestamp(1);
                    return timestamp == null ? null : timestamp.toInstant();
                },
                companyId,
                campaignId);
    }

    public record DashboardContext(
            UUID companyId,
            String companyName,
            int minimumGroupSize,
            UUID campaignId,
            String campaignName,
            Instant startsAt,
            Instant endsAt) {
    }

    public record DepartmentInfo(UUID id, String name) {
    }

    public record CategoryAggregate(String category, double score, int sampleSize) {
    }

    public record SentimentAggregate(String sentiment, int count) {
    }

    public record DepartmentAggregate(UUID id, String name, int sampleSize) {
    }

    public record TopicAggregate(
            String category,
            String label,
            String sentiment,
            String priority,
            int mentions) {
    }

    public record TrendAggregate(String period, double score, int sampleSize) {
    }
}
