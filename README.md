# edusuite-platform — Milestone 0 starter (tenancy + security core)

This is the hand-written, security-critical foundation referenced in
`08_AI_AGENT_EXECUTION_PLAN.md` (tickets 1–4). Everything in `com.edusuite.platform.tenant` and
`com.edusuite.platform.security` should be treated as **reviewed, intentional, and not to be
casually refactored by an agent** — see the header comments in each file for why. Everything
else in the broader project (new modules, new entities, frontend) is fair game for agent-driven
development following the pattern established here.

## What's included
- Postgres Row-Level Security-based tenant isolation, wired through Hibernate's official
  multi-tenancy SPI (`CurrentTenantIdentifierResolverImpl` + `TenantAwareConnectionProvider`).
- A servlet filter (`TenantIdentifierFilter`) that derives the tenant strictly from the
  authenticated JWT's `tenant_id` claim — never from client input.
- A restricted, non-superuser Postgres role (`edusuite_app`) that RLS actually applies to
  (critical: RLS is silently bypassed for superusers and table owners unless configured
  otherwise — see the comment block in `V2__create_restricted_app_role.sql`).
- A release-blocking isolation test (`TenantIsolationIT`) that runs against a real Postgres via
  Testcontainers, proving Tenant B genuinely cannot read Tenant A's rows.
- A Keycloak realm export with a `tenant_id` custom claim mapper and two test users
  (`admin@tenant-a.test` / `admin@tenant-b.test`, password `password123`) already scoped to two
  different tenant ids, for exercising isolation manually.

## Running it locally
```bash
docker compose up -d
```
This starts Postgres (5432), Redis (6379), and Keycloak (8081, importing the realm automatically).

Before starting the Spring Boot app, seed the two test tenants that match the Keycloak test
users' `tenant_id` attributes (Flyway only creates the *schema*, not this dev-only data):

```sql
INSERT INTO tenant (id, name, subdomain, institute_type) VALUES
  ('00000000-0000-0000-0000-000000000001', 'Tenant A School', 'tenant-a', 'SCHOOL'),
  ('00000000-0000-0000-0000-000000000002', 'Tenant B School', 'tenant-b', 'SCHOOL');
```

Then:
```bash
./mvnw spring-boot:run
```

Verify:
```bash
curl http://localhost:8080/actuator/health
```

## Things I could not verify in this environment (verify these first)
I don't have Maven Central access in this sandbox, so this code has not been compiled or run.
Before building on top of it, have your first agent session (or yourself) do a plain
`./mvnw clean verify` and fix anything that doesn't compile. The most likely friction points,
in order of likelihood:
1. **`HibernateMultiTenancyConfig`** — the exact `AvailableSettings` constant names for wiring a
   custom `MultiTenantConnectionProvider`/`CurrentTenantIdentifierResolver` have been stable
   since Hibernate 5, but confirm against the Hibernate 6.5.x docs (the version Spring Boot
   3.3.4 pulls in) since this is the one non-trivial SPI integration in the codebase.
2. Keycloak 25's exact realm-export JSON schema occasionally adds/removes optional fields
   between versions — if `--import-realm` fails silently, check the Keycloak container logs;
   worst case, import this same JSON manually via the Keycloak admin console instead.
3. Flyway needs to run its migrations (including the `CREATE ROLE` in V2) as a role with enough
   privileges to do so — for local dev the default `postgres` superuser via docker-compose is
   fine; document a separate, narrower migration role before this ever touches a real cloud DB.

## What to build next
Follow tickets 5–7 in `08_AI_AGENT_EXECUTION_PLAN.md` (module skeleton, audit logging) using
this tenancy layer as the foundation, then proceed to the Milestone 1 (Attendance) backlog.
Every new tenant-scoped table should copy the exact pattern in
`V3__create_academic_year_with_rls.sql` (enable + FORCE row level security, the
`NULLIF(current_setting(...), '')::uuid` comparison) and every new tenant-scoped entity should
extend `TenantScopedEntity`. When in doubt, add a test to `TenantIsolationIT`'s pattern for the
new table rather than assuming the existing test "covers" it — it only covers `academic_year`.
