INSERT INTO companies(
    id, name, slug, minimum_group_size, created_at, updated_at)
VALUES (
    '10729623-735e-4382-854f-33e3450bdac7',
    'Anonytix Demo GmbH',
    'anonytix-demo',
    5,
    '2026-06-01T00:00:00Z',
    '2026-06-01T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO departments(
    id, company_id, name, code, active, created_at, updated_at)
VALUES
    (
        'ac38af63-dc5a-416e-b5d7-c237535ec37b',
        '10729623-735e-4382-854f-33e3450bdac7',
        'Softwareentwicklung',
        'DEV',
        true,
        '2026-06-01T00:00:00Z',
        '2026-06-01T00:00:00Z'
    ),
    (
        '6a095a71-c076-4f35-98bd-4ec01937be84',
        '10729623-735e-4382-854f-33e3450bdac7',
        'Vertrieb',
        'SALES',
        true,
        '2026-06-01T00:00:00Z',
        '2026-06-01T00:00:00Z'
    ),
    (
        'fedc9e7d-d917-4626-ab79-ec69c583814e',
        '10729623-735e-4382-854f-33e3450bdac7',
        'Marketing',
        'MARKETING',
        true,
        '2026-06-01T00:00:00Z',
        '2026-06-01T00:00:00Z'
    ),
    (
        'd2f7524e-6e27-48dc-83ea-4ba9bfe716ca',
        '10729623-735e-4382-854f-33e3450bdac7',
        'Personal',
        'HR',
        true,
        '2026-06-01T00:00:00Z',
        '2026-06-01T00:00:00Z'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO surveys(
    id, company_id, title, description, type, status, template_key,
    published_at, created_at, updated_at)
VALUES (
    '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
    '10729623-735e-4382-854f-33e3450bdac7',
    'Mitarbeiterbefragung',
    'Anonyme Pulsbefragung zur aktuellen Arbeitssituation.',
    'PULSE',
    'PUBLISHED',
    'MVP_PULSE',
    '2026-06-01T00:00:00Z',
    '2026-06-01T00:00:00Z',
    '2026-06-01T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO questions(
    id, survey_id, text, type, category, source, required, position,
    minimum_value, maximum_value, analyze_with_ai, active, created_at, updated_at)
VALUES
    (
        '16bf1ef0-cff2-41ce-8320-bf45edb3e580',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Wie zufrieden bist du insgesamt?',
        'RATING', 'OVERALL', 'STANDARD', true, 1, 1, 5, false, true,
        '2026-06-01T00:00:00Z', '2026-06-01T00:00:00Z'
    ),
    (
        '5c94ce90-5326-4cc6-a7ce-75caed5999a7',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Wie bewertest du die Führung?',
        'RATING', 'LEADERSHIP', 'STANDARD', true, 2, 1, 5, false, true,
        '2026-06-01T00:00:00Z', '2026-06-01T00:00:00Z'
    ),
    (
        '217342c1-e60b-434b-bdf2-36cc4eb049d1',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Wie bewertest du deine Arbeitsbelastung?',
        'RATING', 'WORKLOAD', 'STANDARD', true, 3, 1, 5, false, true,
        '2026-06-01T00:00:00Z', '2026-06-01T00:00:00Z'
    ),
    (
        '229dc118-0df8-43e0-86eb-4ec01cf5ea39',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Wie gut funktioniert die Kommunikation?',
        'RATING', 'COMMUNICATION', 'STANDARD', true, 4, 1, 5, false, true,
        '2026-06-01T00:00:00Z', '2026-06-01T00:00:00Z'
    ),
    (
        '9b98e98f-1cf4-45da-b760-8c2bad2c0288',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Wie bewertest du den Teamzusammenhalt?',
        'RATING', 'TEAMWORK', 'STANDARD', true, 5, 1, 5, false, true,
        '2026-06-01T00:00:00Z', '2026-06-01T00:00:00Z'
    ),
    (
        'b41e5ce7-d901-47ae-b6c8-2ef272b19060',
        '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
        'Was sollte sich konkret verbessern?',
        'TEXT', 'GENERAL', 'STANDARD', false, 6, NULL, NULL, true, true,
        '2026-06-01T00:00:00Z', '2026-06-01T00:00:00Z'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO campaigns(
    id, company_id, survey_id, name, status, starts_at, ends_at,
    created_at, updated_at)
VALUES (
    '93b6108f-f005-4f4b-8ce9-952fa0a7ddc4',
    '10729623-735e-4382-854f-33e3450bdac7',
    '4e24a5f0-2f07-4b89-9cd6-861e59dc156e',
    'Mitarbeiterbefragung Juni 2026',
    'ACTIVE',
    '2026-06-01T00:00:00Z',
    '2026-06-30T21:59:59Z',
    '2026-06-01T00:00:00Z',
    '2026-06-01T00:00:00Z')
ON CONFLICT (id) DO NOTHING;
