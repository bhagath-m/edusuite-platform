-- CRITICAL SECURITY NOTE, READ BEFORE CHANGING ANYTHING BELOW:
-- PostgreSQL RLS policies are silently SKIPPED for:
--   1. Superuser roles, always, no exception.
--   2. The table owner, UNLESS the table has `FORCE ROW LEVEL SECURITY` set.
-- Both traps are easy to hit accidentally: if your Spring Boot app connects using the same
-- Postgres user that ran the Flyway migrations (commonly the table owner), RLS will appear to
-- "not work" in a way that's easy to miss in dev (since dev data often only has one tenant) and
-- catastrophic in prod (cross-tenant data returned).
--
-- This migration creates a dedicated, restricted role for the application to connect as, and
-- forces RLS to apply even to the owner as a second safeguard. Configure your datasource
-- (application.yml / environment secrets) to connect as `edusuite_app`, NEVER as the migration
-- /owner role, in every environment including local dev.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'edusuite_app') THEN
        CREATE ROLE edusuite_app WITH LOGIN PASSWORD 'change_me_in_every_environment' NOSUPERUSER NOBYPASSRLS;
    END IF;
END
$$;

DO $$
BEGIN
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO edusuite_app', current_database());
END
$$;

GRANT USAGE ON SCHEMA public TO edusuite_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO edusuite_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO edusuite_app;

-- The tenant table has no RLS (see V1) but edusuite_app should only be able to SELECT it in
-- practice via platform-admin-only application code paths - that's an application-layer
-- authorization concern (see SecurityConfig / role checks), not something RLS can express here
-- since this table has no tenant_id to key a policy on.
