# EduSuite — Agent Context

## Project pitch
EduSuite is a multi-tenant SaaS platform for education institution management (schools, colleges, universities, coaching institutes). It is designed so each tenant only pays for and sees the modules it enables. The first target market is India, with structure to expand internationally later.

## Problem statement
Small and mid-sized education institutes run on disconnected tools — spreadsheets, WhatsApp groups, manual attendance registers, and ad-hoc fee tracking. EduSuite gives them one shared, secure, modular platform with per-tenant data isolation and role-based access.

## Target users
- **Institute admins** — configure the institute, manage users, enable modules.
- **Teachers/staff** — mark attendance, manage grades, post notices.
- **Parents/students** — view attendance, fees, notices via a community app.

## Tech stack
- Java 26, Spring Boot 4.1.0, Spring Modulith 2.1.0
- PostgreSQL with Row-Level Security (RLS) for tenant isolation
- Hibernate multi-tenancy via `TenantContext` + `TenantIdentifierFilter`
- Keycloak as OAuth2 / OIDC identity provider
- Redis (caching / sessions)
- Testcontainers 1.21.4 for integration tests
- Docker Compose for local dev
- Maven for builds

## Architecture summary
- Modular monolith using Spring Modulith; modules expose internal APIs and domain events.
- Every tenant-scoped entity extends `TenantScopedEntity`; RLS enforces isolation in Postgres.
- A platform-level `tenant` table lives outside RLS and maps subdomains/ids to tenants.
- JWT `tenant_id` claim drives the tenant context; never from client input.

## Current milestone status
**Milestone 0 — Foundations (complete)**

## Built so far
- Java 26 + Spring Boot 4.1.0 + Spring Modulith 2.1.0 scaffold.
- `Tenant` entity + repository (Ticket 2 in `08_AI_AGENT_EXECUTION_PLAN.md`).
- Postgres RLS wiring: `TenantContext`, `TenantIdentifierFilter`, `CurrentTenantIdentifierResolverImpl`, `TenantAwareConnectionProvider`.
- Restricted `edusuite_app` Postgres role so RLS is actually enforced.
- `academic_year` dummy tenant-scoped table + `TenantIsolationIT` cross-tenant test.
- Keycloak realm export with `tenant_id` claim mapper and test users.
- Spring Modulith module skeleton with ping/pong modules, cross-module event, and a verification test.
- Audit logging aspect wired end-to-end.
- Docker Compose for Postgres, Redis, Keycloak.
- GitHub Actions CI workflow (build/test on PR).

## Not built yet (Milestone 0)
_Empty. All Milestone 0 scope is complete except deferred work:_
- Cloud / Terraform infra (deferred until Milestone 2).

## Up next
**Milestone 1** — Attendance vertical slice: tenant self-signup wizard, academic structure (course/section), student import, attendance marking, and basic reports.

## Security/tenancy guardrails for agents
- Do not derive tenant from headers, query params, path, or body. Only from the authenticated JWT's `tenant_id` claim via `TenantIdentifierFilter`.
- Every new tenant-scoped table must enable and `FORCE ROW LEVEL SECURITY`, using `NULLIF(current_setting('app.current_tenant_id'), '')::uuid` for comparison. Copy `V3__create_academic_year_with_rls.sql` exactly.
- Every new tenant-scoped entity must extend `TenantScopedEntity`.
- The platform `tenant` table is intentionally outside RLS; tenant-scoped tables must reference it with `tenant_id`.
- RLS is silently bypassed for superusers and table owners; the app must connect via the restricted `edusuite_app` role.
- Add a cross-tenant isolation test for each new tenant-scoped table — do not assume the existing `academic_year` test covers it.
- Review any changes to `com.edusuite.platform.tenant` and `com.edusuite.platform.security` personally; do not casually refactor them.

## Key docs
- `/README.md` — local setup, what's included, next steps.
- `/docs/00_README_START_HERE.md` — onboarding and ground rules.
- `/docs/03_TECHNICAL_ARCHITECTURE.md` — architecture, module boundaries, tenancy.
- `/docs/04_DATA_MODEL.md` — entities and isolation strategy.
- `/docs/05_ROADMAP_MILESTONES.md` — milestone definitions.
- `/docs/06_DEVOPS_SECURITY_TESTING.md` — CI/CD, security, testing, audit.
- `/docs/08_AI_AGENT_EXECUTION_PLAN.md` — ticket backlog and agent workflow.
- `/DECISIONS.md` — architectural decision log.
- `/BACKLOG.md` — current ticket checklist.
