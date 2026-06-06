CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    minimum_group_size INTEGER NOT NULL DEFAULT 5 CHECK (minimum_group_size >= 3),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE departments (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(160) NOT NULL,
    code VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_departments_company_code UNIQUE (company_id, code)
);

CREATE INDEX idx_departments_company ON departments(company_id);

CREATE TABLE surveys (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    title VARCHAR(240) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PULSE', 'EXIT')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    template_key VARCHAR(80),
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_surveys_company ON surveys(company_id);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL REFERENCES surveys(id),
    text VARCHAR(1000) NOT NULL,
    help_text VARCHAR(1000),
    type VARCHAR(30) NOT NULL CHECK (type IN ('RATING', 'SINGLE_CHOICE', 'MULTI_CHOICE', 'BOOLEAN', 'TEXT')),
    category VARCHAR(80) NOT NULL,
    source VARCHAR(20) NOT NULL CHECK (source IN ('STANDARD', 'CUSTOM')),
    required BOOLEAN NOT NULL DEFAULT FALSE,
    position INTEGER NOT NULL CHECK (position > 0),
    minimum_value INTEGER,
    maximum_value INTEGER,
    maximum_length INTEGER,
    analyze_with_ai BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_questions_rating_range CHECK (
        type <> 'RATING'
        OR (minimum_value IS NOT NULL AND maximum_value IS NOT NULL AND minimum_value < maximum_value)
    )
);

CREATE INDEX idx_questions_survey ON questions(survey_id);

CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    label VARCHAR(300) NOT NULL,
    value VARCHAR(100) NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    CONSTRAINT uk_question_options_value UNIQUE (question_id, value)
);

CREATE INDEX idx_question_options_question ON question_options(question_id);

CREATE TABLE question_departments (
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES departments(id),
    PRIMARY KEY (question_id, department_id)
);

CREATE INDEX idx_question_departments_department ON question_departments(department_id);

CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    survey_id UUID NOT NULL REFERENCES surveys(id),
    name VARCHAR(240) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED', 'ACTIVE', 'CLOSED')),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_campaign_period CHECK (ends_at > starts_at)
);

CREATE INDEX idx_campaigns_company ON campaigns(company_id);
CREATE INDEX idx_campaigns_survey ON campaigns(survey_id);

CREATE TABLE invitations (
    id UUID PRIMARY KEY,
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'USED', 'REVOKED')),
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_invitations_campaign ON invitations(campaign_id);

CREATE TABLE feedback_submissions (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    department_id UUID NOT NULL REFERENCES departments(id),
    status VARCHAR(30) NOT NULL CHECK (
        status IN ('RECEIVED', 'ANALYZING', 'REVIEW_PENDING', 'APPROVED', 'REJECTED', 'ANALYSIS_FAILED')
    ),
    submitted_at TIMESTAMPTZ NOT NULL,
    raw_data_delete_at TIMESTAMPTZ
);

CREATE INDEX idx_feedback_company ON feedback_submissions(company_id);
CREATE INDEX idx_feedback_campaign ON feedback_submissions(campaign_id);
CREATE INDEX idx_feedback_department ON feedback_submissions(department_id);
CREATE INDEX idx_feedback_status ON feedback_submissions(status);

CREATE TABLE answers (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES feedback_submissions(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    numeric_value NUMERIC(10, 2),
    text_value TEXT,
    boolean_value BOOLEAN,
    CONSTRAINT uk_answers_submission_question UNIQUE (submission_id, question_id)
);

CREATE INDEX idx_answers_submission ON answers(submission_id);
CREATE INDEX idx_answers_question ON answers(question_id);

CREATE TABLE answer_selected_options (
    answer_id UUID NOT NULL REFERENCES answers(id) ON DELETE CASCADE,
    option_id UUID NOT NULL REFERENCES question_options(id),
    PRIMARY KEY (answer_id, option_id)
);

CREATE TABLE ai_analyses (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL UNIQUE REFERENCES feedback_submissions(id) ON DELETE CASCADE,
    overall_sentiment VARCHAR(20) NOT NULL CHECK (overall_sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    summary TEXT NOT NULL,
    pii_detected BOOLEAN NOT NULL DEFAULT FALSE,
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    model VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE analysis_findings (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES ai_analyses(id) ON DELETE CASCADE,
    category VARCHAR(80) NOT NULL,
    label VARCHAR(300) NOT NULL,
    sentiment VARCHAR(20) NOT NULL CHECK (sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    score NUMERIC(5, 2),
    anonymized_text TEXT
);

CREATE INDEX idx_analysis_findings_analysis ON analysis_findings(analysis_id);
CREATE INDEX idx_analysis_findings_category ON analysis_findings(category);

CREATE TABLE moderation_reviews (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES feedback_submissions(id),
    decision VARCHAR(20) NOT NULL CHECK (decision IN ('APPROVED', 'REJECTED')),
    reason TEXT,
    reviewed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_moderation_reviews_submission ON moderation_reviews(submission_id);

CREATE TABLE action_items (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    campaign_id UUID REFERENCES campaigns(id),
    department_id UUID REFERENCES departments(id),
    title VARCHAR(300) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(80) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'DONE')),
    source VARCHAR(20) NOT NULL CHECK (source IN ('AI', 'MANUAL')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_action_items_company ON action_items(company_id);
CREATE INDEX idx_action_items_campaign ON action_items(campaign_id);
