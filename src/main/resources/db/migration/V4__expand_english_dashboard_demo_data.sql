-- Expanded synthetic English demo data.
-- Every statement and score in this migration is fictional.

UPDATE departments
SET name = CASE code
        WHEN 'DEV' THEN 'Engineering'
        WHEN 'SALES' THEN 'Sales'
        WHEN 'MARKETING' THEN 'Marketing'
        WHEN 'HR' THEN 'People & Culture'
        ELSE name
    END,
    updated_at = '2026-06-06T00:00:00Z'
WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
  AND code IN ('DEV', 'SALES', 'MARKETING', 'HR');

INSERT INTO departments(
    id, company_id, name, code, active, created_at, updated_at)
VALUES
    (
        '0a000000-0000-4000-8000-000000000001',
        '10729623-735e-4382-854f-33e3450bdac7',
        'Operations',
        'OPS',
        true,
        '2026-06-06T00:00:00Z',
        '2026-06-06T00:00:00Z'
    ),
    (
        '0a000000-0000-4000-8000-000000000002',
        '10729623-735e-4382-854f-33e3450bdac7',
        'Customer Success',
        'CS',
        true,
        '2026-06-06T00:00:00Z',
        '2026-06-06T00:00:00Z'
    )
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    code = EXCLUDED.code,
    active = true,
    updated_at = EXCLUDED.updated_at;

UPDATE surveys
SET title = 'Employee Experience Survey',
    description = 'Anonymous pulse survey about the current employee experience.',
    updated_at = '2026-06-06T00:00:00Z'
WHERE id = '4e24a5f0-2f07-4b89-9cd6-861e59dc156e';

UPDATE questions
SET text = CASE id
        WHEN '16bf1ef0-cff2-41ce-8320-bf45edb3e580'
            THEN 'How satisfied are you overall?'
        WHEN '5c94ce90-5326-4cc6-a7ce-75caed5999a7'
            THEN 'How would you rate leadership?'
        WHEN '217342c1-e60b-434b-bdf2-36cc4eb049d1'
            THEN 'How manageable is your workload?'
        WHEN '229dc118-0df8-43e0-86eb-4ec01cf5ea39'
            THEN 'How effective is communication?'
        WHEN '9b98e98f-1cf4-45da-b760-8c2bad2c0288'
            THEN 'How would you rate teamwork?'
        WHEN 'b41e5ce7-d901-47ae-b6c8-2ef272b19060'
            THEN 'What should improve?'
        ELSE text
    END,
    help_text = CASE
        WHEN id = 'b41e5ce7-d901-47ae-b6c8-2ef272b19060'
            THEN 'Do not include names or other identifying details.'
        ELSE help_text
    END,
    updated_at = '2026-06-06T00:00:00Z'
WHERE survey_id = '4e24a5f0-2f07-4b89-9cd6-861e59dc156e';

INSERT INTO questions(
    id, survey_id, text, type, category, source, required, position,
    minimum_value, maximum_value, analyze_with_ai, active, created_at, updated_at)
VALUES
    (
        '41000000-0000-4000-8000-000000000001',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'How valued and recognized do you feel?',
        'RATING', 'RECOGNITION', 'STANDARD', true, 6, 1, 5, false, true,
        '2026-06-06T00:00:00Z', '2026-06-06T00:00:00Z'
    ),
    (
        '41000000-0000-4000-8000-000000000002',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'How efficient are your team processes?',
        'RATING', 'PROCESSES', 'STANDARD', true, 7, 1, 5, false, true,
        '2026-06-06T00:00:00Z', '2026-06-06T00:00:00Z'
    ),
    (
        '41000000-0000-4000-8000-000000000003',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'How satisfied are you with your growth opportunities?',
        'RATING', 'GROWTH', 'STANDARD', true, 8, 1, 5, false, true,
        '2026-06-06T00:00:00Z', '2026-06-06T00:00:00Z'
    ),
    (
        '41000000-0000-4000-8000-000000000004',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'How supportive is your work environment?',
        'RATING', 'WORK_ENVIRONMENT', 'STANDARD', true, 9, 1, 5, false, true,
        '2026-06-06T00:00:00Z', '2026-06-06T00:00:00Z'
    )
ON CONFLICT (id) DO NOTHING;

UPDATE questions
SET position = 10,
    updated_at = '2026-06-06T00:00:00Z'
WHERE id = 'b41e5ce7-d901-47ae-b6c8-2ef272b19060';

UPDATE campaigns
SET name = CASE id
        WHEN '24000000-0000-4000-8000-000000000001'
            THEN 'Employee Experience Survey 2024'
        WHEN '25000000-0000-4000-8000-000000000001'
            THEN 'Employee Experience Survey 2025'
        WHEN '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'
            THEN 'Employee Experience Survey 2026'
        ELSE name
    END,
    updated_at = '2026-06-06T00:00:00Z'
WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7';

UPDATE answers a
SET text_value = CASE
        WHEN overall.numeric_value >= 4.0 THEN
            'Collaboration is strong and priorities are usually clear. '
            || 'The team should continue sharing progress openly.'
        WHEN overall.numeric_value >= 3.2 THEN
            'The employee experience is stable, but communication and '
            || 'planning could be more consistent.'
        ELSE
            'Workload, recognition and decision-making need improvement. '
            || 'Clearer priorities would reduce daily pressure.'
    END
FROM answers overall
WHERE a.submission_id = overall.submission_id
  AND a.question_id = 'b41e5ce7-d901-47ae-b6c8-2ef272b19060'
  AND overall.question_id = '16bf1ef0-cff2-41ce-8320-bf45edb3e580';

UPDATE ai_analyses aa
SET summary = CASE
        WHEN aa.overall_sentiment = 'POSITIVE' THEN
            'Positive feedback from ' || d.name
            || ' highlights collaboration and improving employee experience.'
        WHEN aa.overall_sentiment = 'NEUTRAL' THEN
            'Mixed feedback from ' || d.name
            || ' shows opportunities in communication and planning.'
        ELSE
            'Critical feedback from ' || d.name
            || ' highlights workload, recognition and unclear priorities.'
    END,
    model = 'synthetic-english-demo-v2'
FROM feedback_submissions fs
JOIN departments d ON d.id = fs.department_id
WHERE aa.submission_id = fs.id
  AND fs.company_id = '10729623-735e-4382-854f-33e3450bdac7'
  AND aa.model = 'synthetic-demo-analysis-v1';

UPDATE analysis_findings
SET label = CASE category
        WHEN 'WORKLOAD' THEN 'High workload'
        WHEN 'COMMUNICATION' THEN 'Communication needs improvement'
        WHEN 'TEAMWORK' THEN 'Strong team collaboration'
        ELSE label
    END,
    anonymized_text = CASE category
        WHEN 'WORKLOAD' THEN
            'The anonymized feedback indicates sustained workload pressure.'
        WHEN 'COMMUNICATION' THEN
            'The anonymized feedback recommends clearer communication.'
        WHEN 'TEAMWORK' THEN
            'The anonymized feedback highlights effective collaboration.'
        ELSE anonymized_text
    END
WHERE analysis_id IN (
    SELECT aa.id
    FROM ai_analyses aa
    JOIN feedback_submissions fs ON fs.id = aa.submission_id
    WHERE fs.company_id = '10729623-735e-4382-854f-33e3450bdac7'
);

UPDATE moderation_reviews mr
SET reason = 'Automatically approved synthetic demo feedback.'
WHERE mr.submission_id IN (
    SELECT id
    FROM feedback_submissions
    WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7'
);

UPDATE action_items
SET title = CASE id
        WHEN 'a1000000-0000-4000-8000-000000000001'
            THEN 'Review engineering capacity planning'
        WHEN 'a1000000-0000-4000-8000-000000000002'
            THEN 'Introduce a weekly sales alignment'
        WHEN 'a1000000-0000-4000-8000-000000000003'
            THEN 'Review leadership feedback with People & Culture'
        ELSE title
    END,
    description = CASE id
        WHEN 'a1000000-0000-4000-8000-000000000001'
            THEN 'Prioritize workload, on-call duties and parallel projects.'
        WHEN 'a1000000-0000-4000-8000-000000000002'
            THEN 'Align priorities, handovers and open customer requests.'
        WHEN 'a1000000-0000-4000-8000-000000000003'
            THEN 'Cluster recurring concerns and agree on measurable actions.'
        ELSE description
    END,
    updated_at = '2026-06-06T00:00:00Z'
WHERE company_id = '10729623-735e-4382-854f-33e3450bdac7';

CREATE TEMP TABLE expanded_dashboard_feedback ON COMMIT DROP AS
WITH campaign_months(year, month, campaign_id, year_score) AS (
    SELECT
        year,
        month,
        CASE year
            WHEN 2024 THEN '24000000-0000-4000-8000-000000000001'::uuid
            WHEN 2025 THEN '25000000-0000-4000-8000-000000000001'::uuid
            ELSE '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'::uuid
        END,
        CASE year
            WHEN 2024 THEN 3.05
            WHEN 2025 THEN 3.42
            ELSE 3.72
        END
    FROM (
        SELECT 2024 AS year, generate_series(1, 12) AS month
        UNION ALL
        SELECT 2025 AS year, generate_series(1, 12) AS month
        UNION ALL
        SELECT 2026 AS year, generate_series(1, 6) AS month
    ) periods
),
department_values(
    department_id, department_code, department_name, department_score) AS (
    VALUES
        (
            'ac38af63-dc5a-416e-b5d7-c237535ec37b'::uuid,
            'DEV', 'Engineering', 0.18
        ),
        (
            '6a095a71-c076-4f35-98bd-4ec01937be84'::uuid,
            'SALES', 'Sales', -0.24
        ),
        (
            'fedc9e7d-d917-4626-ab79-ec69c583814e'::uuid,
            'MARKETING', 'Marketing', 0.25
        ),
        (
            'd2f7524e-6e27-48dc-83ea-4ba9bfe716ca'::uuid,
            'HR', 'People & Culture', -0.32
        ),
        (
            '0a000000-0000-4000-8000-000000000001'::uuid,
            'OPS', 'Operations', -0.12
        ),
        (
            '0a000000-0000-4000-8000-000000000002'::uuid,
            'CS', 'Customer Success', 0.08
        )
),
raw_feedback AS (
    SELECT
        cm.year,
        cm.month,
        cm.campaign_id,
        dv.department_id,
        dv.department_code,
        dv.department_name,
        response_number,
        make_timestamptz(
            cm.year,
            cm.month,
            8 + response_number,
            8 + response_number,
            0,
            0,
            'UTC') AS submitted_at,
        cm.year_score
            + dv.department_score
            + ((cm.month - 6.5) * 0.025)
            + ((response_number - 4.5) * 0.14)
            + CASE
                WHEN cm.month IN (2, 3) THEN -0.18
                WHEN cm.month IN (5, 6) THEN 0.10
                WHEN cm.month IN (10, 11) THEN 0.18
                ELSE 0
              END AS raw_score
    FROM campaign_months cm
    CROSS JOIN department_values dv
    CROSS JOIN generate_series(1, 8) AS response_number
)
SELECT
    md5(
        'anonytix-expanded-submission-'
        || year || '-' || month || '-'
        || department_code || '-' || response_number)::uuid AS submission_id,
    md5(
        'anonytix-expanded-analysis-'
        || year || '-' || month || '-'
        || department_code || '-' || response_number)::uuid AS analysis_id,
    year,
    month,
    campaign_id,
    department_id,
    department_code,
    department_name,
    response_number,
    submitted_at,
    round(greatest(1.0, least(5.0, raw_score))::numeric, 1) AS overall_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'HR' THEN -0.35
            WHEN 'OPS' THEN -0.15
            WHEN 'DEV' THEN 0.15
            ELSE 0
        END))::numeric, 1) AS leadership_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'DEV' THEN -0.55
            WHEN 'SALES' THEN -0.40
            WHEN 'CS' THEN -0.30
            ELSE -0.12
        END))::numeric, 1) AS workload_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'SALES' THEN -0.25
            WHEN 'OPS' THEN -0.20
            WHEN 'MARKETING' THEN 0.20
            ELSE 0
        END))::numeric, 1) AS communication_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'DEV' THEN 0.35
            WHEN 'MARKETING' THEN 0.25
            WHEN 'HR' THEN 0.15
            ELSE 0
        END))::numeric, 1) AS teamwork_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'HR' THEN -0.45
            WHEN 'SALES' THEN -0.20
            ELSE -0.05
        END))::numeric, 1) AS recognition_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'OPS' THEN -0.50
            WHEN 'CS' THEN -0.20
            WHEN 'DEV' THEN -0.10
            ELSE 0.05
        END))::numeric, 1) AS processes_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'SALES' THEN -0.35
            WHEN 'CS' THEN -0.25
            WHEN 'MARKETING' THEN 0.20
            ELSE 0
        END))::numeric, 1) AS growth_score,
    round(greatest(1.0, least(
        5.0,
        raw_score + CASE department_code
            WHEN 'OPS' THEN -0.25
            WHEN 'DEV' THEN 0.10
            ELSE 0
        END))::numeric, 1) AS environment_score
FROM raw_feedback;

INSERT INTO feedback_submissions(
    id, company_id, campaign_id, department_id, status, submitted_at,
    raw_data_delete_at)
SELECT
    submission_id,
    '10729623-735e-4382-854f-33e3450bdac7',
    campaign_id,
    department_id,
    'APPROVED',
    submitted_at,
    submitted_at + INTERVAL '90 days'
FROM expanded_dashboard_feedback
ON CONFLICT (id) DO NOTHING;

INSERT INTO answers(
    id, submission_id, question_id, numeric_value, text_value, boolean_value)
SELECT
    md5(
        'anonytix-expanded-answer-'
        || feedback.submission_id || '-' || question.question_id)::uuid,
    feedback.submission_id,
    question.question_id,
    question.numeric_value,
    question.text_value,
    NULL
FROM expanded_dashboard_feedback feedback
CROSS JOIN LATERAL (
    VALUES
        (
            '16bf1ef0-cff2-41ce-8320-bf45edb3e580'::uuid,
            feedback.overall_score,
            NULL::text
        ),
        (
            '5c94ce90-5326-4cc6-a7ce-75caed5999a7'::uuid,
            feedback.leadership_score,
            NULL::text
        ),
        (
            '217342c1-e60b-434b-bdf2-36cc4eb049d1'::uuid,
            feedback.workload_score,
            NULL::text
        ),
        (
            '229dc118-0df8-43e0-86eb-4ec01cf5ea39'::uuid,
            feedback.communication_score,
            NULL::text
        ),
        (
            '9b98e98f-1cf4-45da-b760-8c2bad2c0288'::uuid,
            feedback.teamwork_score,
            NULL::text
        ),
        (
            '41000000-0000-4000-8000-000000000001'::uuid,
            feedback.recognition_score,
            NULL::text
        ),
        (
            '41000000-0000-4000-8000-000000000002'::uuid,
            feedback.processes_score,
            NULL::text
        ),
        (
            '41000000-0000-4000-8000-000000000003'::uuid,
            feedback.growth_score,
            NULL::text
        ),
        (
            '41000000-0000-4000-8000-000000000004'::uuid,
            feedback.environment_score,
            NULL::text
        ),
        (
            'b41e5ce7-d901-47ae-b6c8-2ef272b19060'::uuid,
            NULL::numeric,
            CASE
                WHEN feedback.overall_score >= 4.0 THEN
                    'Collaboration is strong and progress is visible. '
                    || 'The team should keep sharing priorities openly.'
                WHEN feedback.overall_score >= 3.2 THEN
                    'The experience is mixed. Communication and career '
                    || 'development should become more consistent.'
                ELSE
                    'Workload and unclear decisions create pressure. '
                    || 'Recognition and planning need immediate improvement.'
            END
        )
) AS question(question_id, numeric_value, text_value)
ON CONFLICT (submission_id, question_id) DO NOTHING;

INSERT INTO ai_analyses(
    id, submission_id, overall_sentiment, summary, pii_detected,
    risk_level, model, created_at)
SELECT
    analysis_id,
    submission_id,
    CASE
        WHEN overall_score >= 3.8 THEN 'POSITIVE'
        WHEN overall_score >= 3.0 THEN 'NEUTRAL'
        ELSE 'NEGATIVE'
    END,
    CASE
        WHEN overall_score >= 3.8 THEN
            'Positive feedback from ' || department_name
            || ' highlights collaboration and improving employee experience.'
        WHEN overall_score >= 3.0 THEN
            'Mixed feedback from ' || department_name
            || ' shows opportunities in communication and development.'
        ELSE
            'Critical feedback from ' || department_name
            || ' highlights workload, recognition and unclear priorities.'
    END,
    false,
    CASE
        WHEN overall_score < 2.7 THEN 'HIGH'
        WHEN overall_score < 3.4 THEN 'MEDIUM'
        ELSE 'LOW'
    END,
    'synthetic-english-demo-v2',
    submitted_at + INTERVAL '2 minutes'
FROM expanded_dashboard_feedback
ON CONFLICT (submission_id) DO NOTHING;

INSERT INTO analysis_findings(
    id, analysis_id, category, label, sentiment, priority, score,
    anonymized_text)
SELECT
    md5(
        'anonytix-expanded-finding-primary-' || feedback.analysis_id)::uuid,
    feedback.analysis_id,
    finding.category,
    finding.label,
    finding.sentiment,
    finding.priority,
    round((0.72 + feedback.response_number * 0.025)::numeric, 2),
    finding.anonymized_text
FROM expanded_dashboard_feedback feedback
CROSS JOIN LATERAL (
    SELECT
        CASE
            WHEN feedback.workload_score <= least(
                feedback.communication_score,
                feedback.recognition_score,
                feedback.processes_score,
                feedback.growth_score)
                THEN 'WORKLOAD'
            WHEN feedback.recognition_score <= least(
                feedback.communication_score,
                feedback.processes_score,
                feedback.growth_score)
                THEN 'RECOGNITION'
            WHEN feedback.processes_score <= least(
                feedback.communication_score,
                feedback.growth_score)
                THEN 'PROCESSES'
            WHEN feedback.growth_score <= feedback.communication_score
                THEN 'GROWTH'
            ELSE 'COMMUNICATION'
        END AS category
) selected
CROSS JOIN LATERAL (
    SELECT
        selected.category,
        CASE selected.category
            WHEN 'WORKLOAD' THEN 'High workload'
            WHEN 'RECOGNITION' THEN 'Insufficient recognition'
            WHEN 'PROCESSES' THEN 'Inefficient processes'
            WHEN 'GROWTH' THEN 'Limited growth opportunities'
            ELSE 'Communication needs improvement'
        END AS label,
        CASE
            WHEN feedback.overall_score >= 3.8 THEN 'POSITIVE'
            WHEN feedback.overall_score >= 3.0 THEN 'NEUTRAL'
            ELSE 'NEGATIVE'
        END AS sentiment,
        CASE
            WHEN feedback.overall_score < 2.7 THEN 'HIGH'
            WHEN feedback.overall_score < 3.4 THEN 'MEDIUM'
            ELSE 'LOW'
        END AS priority,
        CASE selected.category
            WHEN 'WORKLOAD' THEN
                'The anonymized feedback indicates sustained workload pressure.'
            WHEN 'RECOGNITION' THEN
                'The anonymized feedback requests more visible recognition.'
            WHEN 'PROCESSES' THEN
                'The anonymized feedback highlights avoidable process friction.'
            WHEN 'GROWTH' THEN
                'The anonymized feedback asks for clearer development paths.'
            ELSE
                'The anonymized feedback recommends clearer communication.'
        END AS anonymized_text
) finding
ON CONFLICT (id) DO NOTHING;

INSERT INTO analysis_findings(
    id, analysis_id, category, label, sentiment, priority, score,
    anonymized_text)
SELECT
    md5(
        'anonytix-expanded-finding-secondary-' || analysis_id)::uuid,
    analysis_id,
    CASE
        WHEN teamwork_score >= environment_score THEN 'TEAMWORK'
        ELSE 'WORK_ENVIRONMENT'
    END,
    CASE
        WHEN teamwork_score >= environment_score THEN 'Strong team collaboration'
        ELSE 'Supportive work environment'
    END,
    CASE
        WHEN greatest(teamwork_score, environment_score) >= 3.8
            THEN 'POSITIVE'
        WHEN greatest(teamwork_score, environment_score) >= 3.0
            THEN 'NEUTRAL'
        ELSE 'NEGATIVE'
    END,
    CASE
        WHEN greatest(teamwork_score, environment_score) >= 3.8 THEN 'LOW'
        WHEN greatest(teamwork_score, environment_score) >= 3.0 THEN 'MEDIUM'
        ELSE 'HIGH'
    END,
    round((0.66 + response_number * 0.025)::numeric, 2),
    CASE
        WHEN teamwork_score >= environment_score THEN
            'The anonymized feedback highlights collaboration as a strength.'
        ELSE
            'The anonymized feedback values the supportive work environment.'
    END
FROM expanded_dashboard_feedback
ON CONFLICT (id) DO NOTHING;

INSERT INTO moderation_reviews(
    id, submission_id, decision, reason, reviewed_at)
SELECT
    md5('anonytix-expanded-review-' || submission_id)::uuid,
    submission_id,
    'APPROVED',
    'Automatically approved synthetic demo feedback.',
    submitted_at + INTERVAL '5 minutes'
FROM expanded_dashboard_feedback
ON CONFLICT (id) DO NOTHING;

INSERT INTO action_items(
    id, company_id, campaign_id, department_id, title, description,
    category, priority, status, source, created_at, updated_at)
VALUES
    (
        'a2000000-0000-4000-8000-000000000001',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        '0a000000-0000-4000-8000-000000000001',
        'Simplify operational approval flows',
        'Remove duplicate approvals and define clear process owners.',
        'PROCESSES', 'HIGH', 'IN_PROGRESS', 'AI',
        '2026-05-12T08:00:00Z', '2026-06-05T08:00:00Z'
    ),
    (
        'a2000000-0000-4000-8000-000000000002',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        '0a000000-0000-4000-8000-000000000002',
        'Create Customer Success growth paths',
        'Define role expectations, mentoring and promotion criteria.',
        'GROWTH', 'MEDIUM', 'OPEN', 'AI',
        '2026-05-18T08:00:00Z', '2026-05-18T08:00:00Z'
    ),
    (
        'a2000000-0000-4000-8000-000000000003',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        'd2f7524e-6e27-48dc-83ea-4ba9bfe716ca',
        'Launch a recognition routine',
        'Create monthly peer recognition and manager follow-up.',
        'RECOGNITION', 'HIGH', 'OPEN', 'MANUAL',
        '2026-05-24T08:00:00Z', '2026-05-24T08:00:00Z'
    ),
    (
        'a2000000-0000-4000-8000-000000000004',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        'fedc9e7d-d917-4626-ab79-ec69c583814e',
        'Protect focus time for campaign work',
        'Reduce urgent interruptions and agree on weekly priorities.',
        'WORKLOAD', 'MEDIUM', 'DONE', 'AI',
        '2026-04-10T08:00:00Z', '2026-05-28T08:00:00Z'
    )
ON CONFLICT (id) DO NOTHING;
