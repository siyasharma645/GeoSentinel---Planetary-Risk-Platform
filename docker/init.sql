CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'CITIZEN',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(200),
    ip_address VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS disasters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(100) UNIQUE,
    type VARCHAR(50) NOT NULL,
    sub_type VARCHAR(50),
    title VARCHAR(500),
    location_name VARCHAR(255),
    country_code VARCHAR(3),
    lat NUMERIC(9,6),
    lon NUMERIC(9,6),
    severity NUMERIC(4,2),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    population_affected BIGINT,
    impact_radius_km NUMERIC(10,2),
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    last_updated TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source VARCHAR(100),
    source_url TEXT,
    raw_data JSONB
);
CREATE INDEX IF NOT EXISTS idx_dis_type    ON disasters(type);
CREATE INDEX IF NOT EXISTS idx_dis_status  ON disasters(status);
CREATE INDEX IF NOT EXISTS idx_dis_country ON disasters(country_code);
CREATE INDEX IF NOT EXISTS idx_dis_coords  ON disasters(lat, lon);

CREATE TABLE IF NOT EXISTS disaster_timeline (
    id BIGSERIAL PRIMARY KEY,
    disaster_id UUID NOT NULL REFERENCES disasters(id) ON DELETE CASCADE,
    event_type VARCHAR(50),
    description TEXT,
    severity NUMERIC(4,2),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS climate_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_type VARCHAR(50) NOT NULL,
    region VARCHAR(100),
    country_code VARCHAR(3),
    lat NUMERIC(9,6),
    lon NUMERIC(9,6),
    value NUMERIC(12,4) NOT NULL,
    unit VARCHAR(30),
    anomaly NUMERIC(8,4),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_clm_type    ON climate_metrics(metric_type);
CREATE INDEX IF NOT EXISTS idx_clm_country ON climate_metrics(country_code);

CREATE TABLE IF NOT EXISTS location_risk_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_name VARCHAR(255) NOT NULL,
    country_code VARCHAR(3),
    lat NUMERIC(9,6) NOT NULL,
    lon NUMERIC(9,6) NOT NULL,
    overall_risk INTEGER NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    summary TEXT,
    report_json JSONB NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_rpt_coords  ON location_risk_reports(lat, lon);
CREATE INDEX IF NOT EXISTS idx_rpt_country ON location_risk_reports(country_code);

CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_type VARCHAR(50) NOT NULL,
    level VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    country_code VARCHAR(3),
    region VARCHAR(200),
    lat NUMERIC(9,6),
    lon NUMERIC(9,6),
    source_id UUID,
    source_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    metadata JSONB
);
CREATE INDEX IF NOT EXISTS idx_alr_level   ON alerts(level);
CREATE INDEX IF NOT EXISTS idx_alr_status  ON alerts(status);
CREATE INDEX IF NOT EXISTS idx_alr_country ON alerts(country_code);
CREATE INDEX IF NOT EXISTS idx_alr_created ON alerts(created_at DESC);

-- Default admin (password: Admin@12345)
INSERT INTO users (email, password, first_name, last_name, role)
VALUES ('admin@geosentinel.io',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCb7gHQGFnhWVm5Lh2TJXVW',
        'System','Admin','GOVERNMENT')
ON CONFLICT (email) DO NOTHING;
