-- Synthetic demo data for the hackathon dashboard.
-- No row in this migration represents a real person or a real employee statement.

INSERT INTO campaigns(
    id, company_id, survey_id, name, status, starts_at, ends_at,
    created_at, updated_at)
VALUES
    (
        '24000000-0000-4000-8000-000000000001',
        '10729623-735e-4382-854f-33e3450bdac7',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Mitarbeiterbefragung 2024',
        'CLOSED',
        '2024-01-01T00:00:00Z',
        '2024-12-31T23:59:59Z',
        '2024-01-01T00:00:00Z',
        '2024-12-31T23:59:59Z'
    ),
    (
        '25000000-0000-4000-8000-000000000001',
        '10729623-735e-4382-854f-33e3450bdac7',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Mitarbeiterbefragung 2025',
        'CLOSED',
        '2025-01-01T00:00:00Z',
        '2025-12-31T23:59:59Z',
        '2025-01-01T00:00:00Z',
        '2025-12-31T23:59:59Z'
    )
ON CONFLICT (id) DO NOTHING;

UPDATE campaigns
SET name = 'Mitarbeiterbefragung 2026',
    starts_at = '2026-01-01T00:00:00Z',
    ends_at = '2026-12-31T23:59:59Z',
    updated_at = '2026-06-06T00:00:00Z'
WHERE id = '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4';

CREATE TEMP TABLE demo_dashboard_feedback ON COMMIT DROP AS
WITH campaign_months(year, month, campaign_id, year_score) AS (
    VALUES
        (2024, 2, '24000000-0000-4000-8000-000000000001'::uuid, 3.00),
        (2024, 5, '24000000-0000-4000-8000-000000000001'::uuid, 3.00),
        (2024, 8, '24000000-0000-4000-8000-000000000001'::uuid, 3.00),
        (2024, 11, '24000000-0000-4000-8000-000000000001'::uuid, 3.00),
        (2025, 2, '25000000-0000-4000-8000-000000000001'::uuid, 3.45),
        (2025, 5, '25000000-0000-4000-8000-000000000001'::uuid, 3.45),
        (2025, 8, '25000000-0000-4000-8000-000000000001'::uuid, 3.45),
        (2025, 11, '25000000-0000-4000-8000-000000000001'::uuid, 3.45),
        (2026, 1, '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'::uuid, 3.90),
        (2026, 3, '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'::uuid, 3.90),
        (2026, 5, '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'::uuid, 3.90),
        (2026, 6, '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4'::uuid, 3.90)
),
department_values(department_id, department_code, department_name, department_score) AS (
    VALUES
        (
            'ac38af63-dc5a-416e-b5d7-c237535ec37b'::uuid,
            'DEV',
            'Softwareentwicklung',
            0.35
        ),
        (
            '6a095a71-c076-4f35-98bd-4ec01937be84'::uuid,
            'SALES',
            'Vertrieb',
            -0.20
        ),
        (
            'fedc9e7d-d917-4626-ab79-ec69c583814e'::uuid,
            'MARKETING',
            'Marketing',
            0.10
        ),
        (
            'd2f7524e-6e27-48dc-83ea-4ba9bfe716ca'::uuid,
            'HR',
            'Personal',
            -0.35
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
            9 + response_number,
            0,
            0,
            'UTC') AS submitted_at,
        cm.year_score
            + dv.department_score
            + CASE cm.month
                WHEN 1 THEN -0.15
                WHEN 2 THEN -0.20
                WHEN 3 THEN -0.05
                WHEN 5 THEN 0.05
                WHEN 6 THEN 0.15
                WHEN 8 THEN 0.20
                WHEN 11 THEN 0.35
                ELSE 0
              END
            + ((response_number - 3.5) * 0.08) AS raw_score
    FROM campaign_months cm
    CROSS JOIN department_values dv
    CROSS JOIN generate_series(1, 6) AS response_number
)
SELECT
    md5(
        'anonytix-demo-submission-'
        || year || '-' || month || '-'
        || department_code || '-' || response_number)::uuid AS submission_id,
    md5(
        'anonytix-demo-analysis-'
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
    round(greatest(
        1.0,
        least(
            5.0,
            raw_score
                + CASE department_code
                    WHEN 'HR' THEN -0.25
                    WHEN 'DEV' THEN 0.15
                    ELSE 0
                  END))::numeric, 1) AS leadership_score,
    round(greatest(
        1.0,
        least(
            5.0,
            raw_score
                + CASE department_code
                    WHEN 'DEV' THEN -0.45
                    WHEN 'SALES' THEN -0.30
                    ELSE -0.10
                  END))::numeric, 1) AS workload_score,
    round(greatest(
        1.0,
        least(
            5.0,
            raw_score
                + CASE department_code
                    WHEN 'SALES' THEN -0.20
                    WHEN 'MARKETING' THEN 0.20
                    ELSE 0
                  END))::numeric, 1) AS communication_score,
    round(greatest(
        1.0,
        least(
            5.0,
            raw_score
                + CASE department_code
                    WHEN 'DEV' THEN 0.30
                    WHEN 'HR' THEN 0.10
                    ELSE 0
                  END))::numeric, 1) AS teamwork_score
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
FROM demo_dashboard_feedback
ON CONFLICT (id) DO NOTHING;

INSERT INTO answers(
    id, submission_id, question_id, numeric_value, text_value, boolean_value)
SELECT
    md5(
        'anonytix-demo-answer-'
        || feedback.submission_id
        || '-'
        || question.question_id)::uuid,
    feedback.submission_id,
    question.question_id,
    question.numeric_value,
    question.text_value,
    NULL
FROM demo_dashboard_feedback feedback
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
            'b41e5ce7-d901-47ae-b6c8-2ef272b19060'::uuid,
            NULL::numeric,
            CASE
                WHEN feedback.overall_score >= 4.0 THEN
                    'Die Zusammenarbeit in der Abteilung funktioniert gut. '
                    || 'Klare Prioritäten und regelmäßiger Austausch sollten '
                    || 'beibehalten werden.'
                WHEN feedback.overall_score >= 3.2 THEN
                    'Die Arbeitssituation ist insgesamt solide. Verbesserungen '
                    || 'bei Kommunikation und Planbarkeit wären hilfreich.'
                ELSE
                    'Arbeitsbelastung und Abstimmung sollten verbessert werden. '
                    || 'Verbindliche Prioritäten würden den Arbeitsalltag entlasten.'
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
        WHEN overall_score >= 3.1 THEN 'NEUTRAL'
        ELSE 'NEGATIVE'
    END,
    CASE
        WHEN overall_score >= 3.8 THEN
            'Überwiegend positive Rückmeldung aus dem Bereich '
            || department_name
            || ' mit stabiler Zusammenarbeit und guter Entwicklung.'
        WHEN overall_score >= 3.1 THEN
            'Ausgewogene Rückmeldung aus dem Bereich '
            || department_name
            || ' mit Verbesserungspotenzial bei Abstimmung und Planbarkeit.'
        ELSE
            'Kritische Rückmeldung aus dem Bereich '
            || department_name
            || ' mit Hinweisen auf Belastung und unklare Prioritäten.'
    END,
    false,
    CASE
        WHEN overall_score < 2.8 THEN 'HIGH'
        WHEN overall_score < 3.4 THEN 'MEDIUM'
        ELSE 'LOW'
    END,
    'synthetic-demo-analysis-v1',
    submitted_at + INTERVAL '2 minutes'
FROM demo_dashboard_feedback
ON CONFLICT (submission_id) DO NOTHING;

INSERT INTO analysis_findings(
    id, analysis_id, category, label, sentiment, priority, score,
    anonymized_text)
SELECT
    md5('anonytix-demo-finding-' || analysis_id)::uuid,
    analysis_id,
    CASE
        WHEN workload_score < 3.2 THEN 'WORKLOAD'
        WHEN communication_score < 3.5 THEN 'COMMUNICATION'
        ELSE 'TEAMWORK'
    END,
    CASE
        WHEN workload_score < 3.2 THEN 'Hohe Arbeitsbelastung'
        WHEN communication_score < 3.5 THEN 'Abstimmung verbessern'
        ELSE 'Starker Teamzusammenhalt'
    END,
    CASE
        WHEN overall_score >= 3.8 THEN 'POSITIVE'
        WHEN overall_score >= 3.1 THEN 'NEUTRAL'
        ELSE 'NEGATIVE'
    END,
    CASE
        WHEN overall_score < 2.8 THEN 'HIGH'
        WHEN overall_score < 3.4 THEN 'MEDIUM'
        ELSE 'LOW'
    END,
    round((0.72 + response_number * 0.03)::numeric, 2),
    CASE
        WHEN workload_score < 3.2 THEN
            'Die anonymisierte Rückmeldung weist auf eine hohe Belastung hin.'
        WHEN communication_score < 3.5 THEN
            'Die anonymisierte Rückmeldung empfiehlt klarere Abstimmungen.'
        ELSE
            'Die anonymisierte Rückmeldung hebt die Zusammenarbeit positiv hervor.'
    END
FROM demo_dashboard_feedback
ON CONFLICT (id) DO NOTHING;

INSERT INTO moderation_reviews(
    id, submission_id, decision, reason, reviewed_at)
SELECT
    md5('anonytix-demo-review-' || submission_id)::uuid,
    submission_id,
    'APPROVED',
    'Automatisch freigegebene synthetische Demo-Rückmeldung.',
    submitted_at + INTERVAL '5 minutes'
FROM demo_dashboard_feedback
ON CONFLICT (id) DO NOTHING;

INSERT INTO action_items(
    id, company_id, campaign_id, department_id, title, description,
    category, priority, status, source, created_at, updated_at)
VALUES
    (
        'a1000000-0000-4000-8000-000000000001',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        'ac38af63-dc5a-416e-b5d7-c237535ec37b',
        'Kapazitätsplanung im Entwicklungsteam prüfen',
        'Arbeitslast, Bereitschaften und parallele Projekte gemeinsam priorisieren.',
        'WORKLOAD',
        'HIGH',
        'IN_PROGRESS',
        'AI',
        '2026-06-01T08:00:00Z',
        '2026-06-05T08:00:00Z'
    ),
    (
        'a1000000-0000-4000-8000-000000000002',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        '6a095a71-c076-4f35-98bd-4ec01937be84',
        'Wöchentlichen Vertriebsabgleich einführen',
        'Prioritäten, Übergaben und offene Kundenanfragen transparent abstimmen.',
        'COMMUNICATION',
        'MEDIUM',
        'OPEN',
        'AI',
        '2026-06-02T08:00:00Z',
        '2026-06-02T08:00:00Z'
    ),
    (
        'a1000000-0000-4000-8000-000000000003',
        '10729623-735e-4382-854f-33e3450bdac7',
        '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
        'd2f7524e-6e27-48dc-83ea-4ba9bfe716ca',
        'Führungsfeedback mit HR auswerten',
        'Wiederkehrende Hinweise clustern und konkrete Maßnahmen vereinbaren.',
        'LEADERSHIP',
        'HIGH',
        'OPEN',
        'MANUAL',
        '2026-06-03T08:00:00Z',
        '2026-06-03T08:00:00Z'
    )
ON CONFLICT (id) DO NOTHING;
