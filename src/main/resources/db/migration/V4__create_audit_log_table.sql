-- Audit log table. Every row is tenant-scoped and protected by RLS so a tenant can only
-- read its own audit history.

CREATE TABLE audit_log (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id),
    actor         VARCHAR(255) NOT NULL,
    action        VARCHAR(100) NOT NULL,
    entity_type   VARCHAR(100),
    entity_id     VARCHAR(255),
    before_json   TEXT,
    after_json    TEXT,
    client_ip     VARCHAR(64),
    user_agent    TEXT,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_tenant_id ON audit_log (tenant_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at);

ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_audit_log ON audit_log
    USING (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid);

COMMENT ON POLICY tenant_isolation_audit_log ON audit_log IS
    'Standard tenant isolation policy for the audit_log table.';
