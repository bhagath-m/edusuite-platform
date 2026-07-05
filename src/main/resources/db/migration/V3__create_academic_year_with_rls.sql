-- Template migration: copy this pattern for EVERY future tenant-scoped table.
-- The three things every such table needs are marked below: (1) tenant_id column and FK,
-- (2) ENABLE ROW LEVEL SECURITY, (3) FORCE ROW LEVEL SECURITY + the policy itself.

CREATE TABLE academic_year (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenant(id),   -- (1)
    label       VARCHAR(20) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    current     BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_academic_year_tenant_id ON academic_year (tenant_id);

ALTER TABLE academic_year ENABLE ROW LEVEL SECURITY;   -- (2)
ALTER TABLE academic_year FORCE ROW LEVEL SECURITY;    -- (3a) - applies RLS even to table owner

CREATE POLICY tenant_isolation_academic_year ON academic_year
    USING (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid);
    -- (3b) USING governs which existing rows are visible/updatable/deletable.
    --      WITH CHECK governs which rows can be INSERTed/UPDATEd INTO (prevents a bug from
    --      writing a row stamped with a *different* tenant's id than the current session's).
    -- NULLIF(...,'')::uuid: if app.current_tenant was never set (see
    -- TenantAwareConnectionProvider - the NO_TENANT sentinel maps to empty string), this
    -- evaluates to NULL, and `tenant_id = NULL` is never true in SQL - so an unset tenant
    -- context sees ZERO rows on this table. Fail closed.

COMMENT ON POLICY tenant_isolation_academic_year ON academic_year IS
    'Copy this exact policy shape for every new tenant-scoped table. Do not hand-roll a
     different comparison expression - consistency here is what makes the isolation test
     suite (TenantIsolationIT) meaningfully cover every table with one shared test pattern.';
