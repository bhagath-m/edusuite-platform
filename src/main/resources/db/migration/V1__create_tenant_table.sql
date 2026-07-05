-- Platform-level table. Deliberately has NO tenant_id and NO row-level security: this table
-- IS the list of tenants, so it can't be scoped by one.
CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- for gen_random_uuid()

CREATE TABLE tenant (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NOT NULL,
    subdomain        VARCHAR(100) NOT NULL UNIQUE,
    institute_type   VARCHAR(30)  NOT NULL,
    plan_tier        VARCHAR(30)  NOT NULL DEFAULT 'STARTER',
    billing_status   VARCHAR(30)  NOT NULL DEFAULT 'TRIAL',
    locale           VARCHAR(20)  NOT NULL DEFAULT 'en-IN',
    currency         VARCHAR(10)  NOT NULL DEFAULT 'INR',
    timezone         VARCHAR(50)  NOT NULL DEFAULT 'Asia/Kolkata',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE tenant IS 'Platform-level: defines tenants. No RLS on this table by design.';
