CREATE TABLE app_user (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email varchar(254) NOT NULL UNIQUE,
    password_hash varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE candidate_profile (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL UNIQUE REFERENCES app_user (id) ON DELETE CASCADE,
    full_name varchar(160) NOT NULL DEFAULT '',
    target_role varchar(160) NOT NULL DEFAULT '',
    current_profession varchar(160),
    city varchar(80),
    state varchar(40),
    professional_email varchar(254),
    phone varchar(40),
    linkedin_url varchar(300),
    github_url varchar(300),
    portfolio_url varchar(300),
    other_professional_links varchar(500),
    work_mode varchar(20),
    target_opportunity varchar(500),
    include_age boolean NOT NULL DEFAULT false,
    age integer,
    career_change boolean NOT NULL DEFAULT false,
    desired_profession varchar(160),
    career_change_reason varchar(1000),
    photo_path varchar(400),
    photo_approved boolean NOT NULL DEFAULT false,
    ai_consent_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT age_range CHECK (age IS NULL OR (age BETWEEN 14 AND 100))
);

CREATE TABLE experience (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    company varchar(160) NOT NULL,
    role_title varchar(160) NOT NULL,
    start_year integer NOT NULL,
    start_month integer,
    end_year integer,
    end_month integer,
    current_job boolean NOT NULL DEFAULT false,
    project_label varchar(240),
    responsibilities text,
    tools text,
    results text,
    notes text,
    sort_order integer NOT NULL DEFAULT 0,
    detail_preference varchar(20) NOT NULL DEFAULT 'AUTO',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT experience_month_start CHECK (start_month IS NULL OR start_month BETWEEN 1 AND 12),
    CONSTRAINT experience_month_end CHECK (end_month IS NULL OR end_month BETWEEN 1 AND 12),
    CONSTRAINT experience_year_start CHECK (start_year BETWEEN 1950 AND 2100),
    CONSTRAINT experience_year_end CHECK (end_year IS NULL OR end_year BETWEEN 1950 AND 2100)
);

CREATE INDEX idx_experience_user ON experience (user_id, sort_order);

CREATE TABLE education (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    level varchar(40) NOT NULL,
    course varchar(160),
    institution varchar(160),
    status varchar(20) NOT NULL,
    start_year integer,
    end_year integer,
    sort_order integer NOT NULL DEFAULT 0
);

CREATE INDEX idx_education_user ON education (user_id, sort_order);

CREATE TABLE certification (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name varchar(200) NOT NULL,
    issuer varchar(160),
    year integer,
    sort_order integer NOT NULL DEFAULT 0
);

CREATE INDEX idx_certification_user ON certification (user_id, sort_order);

CREATE TABLE skill (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name varchar(80) NOT NULL,
    category varchar(30) NOT NULL,
    confirmed boolean NOT NULL DEFAULT false,
    origin varchar(20) NOT NULL DEFAULT 'USER',
    sort_order integer NOT NULL DEFAULT 0,
    CONSTRAINT skill_unique UNIQUE (user_id, category, name)
);

CREATE INDEX idx_skill_user ON skill (user_id, sort_order);

CREATE TABLE language_skill (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name varchar(40) NOT NULL,
    comprehension varchar(20),
    reading varchar(20),
    writing varchar(20),
    conversation varchar(20),
    CONSTRAINT language_unique UNIQUE (user_id, name)
);

CREATE TABLE project_item (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name varchar(160) NOT NULL,
    description text,
    url varchar(300),
    personal boolean NOT NULL DEFAULT true,
    sort_order integer NOT NULL DEFAULT 0
);

CREATE INDEX idx_project_user ON project_item (user_id, sort_order);

CREATE TABLE resume (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    title varchar(160) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'DRAFT',
    target_role varchar(160),
    summary_draft text,
    summary_approved text,
    template_code varchar(40) NOT NULL DEFAULT 'REFERENCIA_A4',
    include_photo boolean NOT NULL DEFAULT false,
    approved_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_resume_user ON resume (user_id, updated_at DESC);

CREATE TABLE resume_version (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    resume_id uuid NOT NULL REFERENCES resume (id) ON DELETE CASCADE,
    version_number integer NOT NULL,
    snapshot jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT resume_version_unique UNIQUE (resume_id, version_number)
);

CREATE TABLE interview (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    resume_id uuid REFERENCES resume (id) ON DELETE SET NULL,
    status varchar(20) NOT NULL DEFAULT 'IN_PROGRESS',
    stage varchar(40) NOT NULL,
    question_key varchar(80),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_interview_user ON interview (user_id, updated_at DESC);

CREATE TABLE interview_message (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    interview_id uuid NOT NULL REFERENCES interview (id) ON DELETE CASCADE,
    role varchar(20) NOT NULL,
    stage varchar(40) NOT NULL,
    question_key varchar(80),
    content text NOT NULL,
    skipped boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_message_interview ON interview_message (interview_id, created_at);

CREATE TABLE job_posting (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    title varchar(160) NOT NULL,
    company varchar(160),
    description text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_job_user ON job_posting (user_id, created_at DESC);

CREATE TABLE job_match (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    resume_id uuid NOT NULL REFERENCES resume (id) ON DELETE CASCADE,
    job_id uuid NOT NULL REFERENCES job_posting (id) ON DELETE CASCADE,
    demonstrated jsonb NOT NULL,
    related jsonb NOT NULL,
    missing jsonb NOT NULL,
    keywords jsonb NOT NULL,
    suggestions text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE audit_event (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid,
    action varchar(60) NOT NULL,
    resource_type varchar(40),
    resource_id uuid,
    metadata jsonb,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_user ON audit_event (user_id, created_at DESC);

CREATE TABLE ai_invocation (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid,
    purpose varchar(40) NOT NULL,
    model varchar(80) NOT NULL,
    status varchar(20) NOT NULL,
    latency_ms integer,
    input_chars integer,
    output_chars integer,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_invocation_created ON ai_invocation (created_at);
