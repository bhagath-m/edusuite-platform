# 08 — AI-Agent Execution Plan (Solo Dev + Multiple Free-Tier Agents)

This document exists because your situation is specific: one experienced backend engineer, doing UI at "manageable" level, delegating most implementation to multiple AI coding agents across tools (no paid subscriptions), likely across many disconnected sessions. That changes how work should be packaged. Read this before opening any agent session.

## 1. Ground rules for working with stateless/rotating agents
- **Every agent session starts cold.** It doesn't remember yesterday's session, and a different tool/agent might pick up the next ticket. So the repo itself must carry all context — not your memory, not chat history.
- **One ticket = one agent session = one PR.** Keep tickets small enough (a few hours of work) that a single session can finish, run tests, and stop cleanly. Large multi-day tickets are where agents drift and produce unreviewable diffs.
- **You personally review, and stay hands-on for:** multi-tenancy/RLS code, auth/security code, billing/payment code, and anything touching the module-boundary rules in doc 03. These are the places where a subtle agent mistake is expensive (a tenant data leak, a billing bug) — treat agent output here as a draft you audit line-by-line, not a merge-on-green-CI situation. Everything else (CRUD endpoints, UI screens, tests, docs) can be more trust-and-verify-via-CI.
- **CI is your safety net, not a formality.** Since you can't manually re-review every agent's full context and reasoning, a strong automated test suite (doc 06) is what actually catches regressions. Do not relax the "tests must pass" gate to move faster — it's the opposite of faster with agent-written code.

## 2. The "context pack" every agent must read first
Keep these files at the repo root, and put this exact instruction in your agent's system prompt / first message every time:

> "Before writing any code, read `/docs/00...07` (product spec) and `/CONTEXT.md`, `/DECISIONS.md`, and `/BACKLOG.md` in this repo. Only work on the single ticket I specify. Do not modify code outside the files needed for that ticket. Follow the module-boundary rules in `03_TECHNICAL_ARCHITECTURE.md` strictly."

Files to maintain (create at repo init):
- **`/docs/*`** — the 8 documents from this planning set, committed into the repo itself (not just kept in chat) so every agent session can read them directly.
- **`CONTEXT.md`** — a living, short (under 1 page) summary: current milestone, what's built so far, what's intentionally not built yet, and any deviations already made from the docs (agents will occasionally suggest reasonable deviations — record them here immediately so the next agent doesn't "fix" it back).
- **`DECISIONS.md`** — an append-only log of architecture decisions made during actual implementation (lightweight ADR format: date, decision, why, alternatives considered). This prevents two different agents from re-litigating the same choice differently.
- **`BACKLOG.md`** — the ticket list below, with status (todo/in-progress/done) kept current. This is your kanban board since you likely don't have a paid PM tool — a plain markdown checklist is enough at this scale.

## 3. Local-first environment (avoid cloud cost until you need it)
Run everything via Docker Compose on your machine through Milestone 0 and Milestone 1:
- PostgreSQL container
- Redis container
- Keycloak container (dev mode)
- The Spring Boot app (run directly via IDE/CLI, not containerized yet, for fast iteration)
- Chatwoot: skip self-hosting for now — its free/community cloud-lite options or simply deferring the support-widget ticket entirely until Milestone 2 is fine; don't spend agent time on this during Milestone 0/1.
- Skip AWS/Terraform entirely until you have a design-partner institute ready for a staging environment (Milestone 2). Building cloud infra before you need it is exactly the kind of premature investment your original message wanted to avoid.

A `docker-compose.yml` covering Postgres + Redis + Keycloak, plus a `README.md` with "clone, run `docker compose up`, run the Spring Boot app, hit `/actuator/health`" should be **Ticket #1** — verify a fresh clone works end-to-end before anything else.

## 4. Milestone 0 backlog (foundations) — ticket-sized
1. Repo scaffold: Spring Boot 3 project (Java 21), Spring Modulith dependency, base package structure per doc 03 §1, `docker-compose.yml`, CI workflow (build+test on PR).
2. `Tenant` entity + Flyway migration + repository, with `tenant_id` pattern established (doc 04 §1).
3. RLS policy + Postgres session variable wiring + `TenantContext` filter — **you review this ticket personally**, then write the "attempt cross-tenant access, assert failure" test suite (doc 06 §4) yourself or have an agent draft it and you verify it actually fails correctly against a deliberately broken implementation first (mutation-test your own safety net).
4. Keycloak integration: realm/client setup script (Terraform or Keycloak's own export/import JSON, checked into repo so it's reproducible), Spring Security OAuth2 resource-server config, one working login → JWT → authenticated request flow.
5. Base module skeleton: two dummy modules (`ping` and `pong`) proving Spring Modulith boundary rules + one cross-module event, with a Spring Modulith verification test in CI.
6. Audit logging aspect wired to one dummy entity end-to-end (doc 06 §3).
7. `CONTEXT.md`, `DECISIONS.md`, `BACKLOG.md` initialized and committed.

**Exit check (do this yourself, not via agent):** clone the repo fresh, `docker compose up`, run the app, log in, hit one tenant-scoped endpoint, confirm an audit row was written, confirm the cross-tenant test suite fails on a deliberately-broken build and passes on the real one.

## 5. Milestone 1 backlog (Attendance vertical slice) — ticket-sized
Given you chose Attendance first:
8. Core academic structure: `AcademicYear`, `Course`, `Section` entities + CRUD APIs + migrations (doc 04 §3).
9. `Person` + `Student` entities + CRUD API + CSV bulk-import endpoint with a validation report response (doc 02 §"Student Information System").
10. RBAC: `Role`/`UserRole`/permission check annotations wired to real endpoints from tickets 8–9 (Admin vs Teacher access differences).
11. `AttendanceRecord` entity + mark-attendance API (single + bulk-for-section) + basic "attendance report per section per date range" query API.
12. Frontend: setup wizard screen (institute name, board, academic year, first admin) — hits the APIs from ticket 8.
13. Frontend: Admin screens for sections + student list/import (tickets 8–9).
14. Frontend: Attendance marking screen (ticket 11) — decide now whether this is web-first or mobile-PWA-first for your design partner's actual workflow, and note the decision in `DECISIONS.md`.
15. Module-toggle mechanism (`TenantModuleSubscription` table + `@RequiresModule` guard + frontend nav filtering) — implement now even though Attendance is core, so the *pattern* exists before you add the second module in Milestone 2. This is worth doing slightly early because retrofitting the toggle pattern after 3 modules exist is more work than building it once, correctly, alongside the first optional-feeling module.
16. End-to-end Playwright test: signup → wizard → create section → add students → mark attendance → view report.

**Exit check:** have one real (or friendly pilot) institute use this for one real week, per the roadmap's Milestone 1 exit criterion — this is a human/product checkpoint, not something an agent can verify for you.

## 6. Practical tips for your specific setup
- Since different agent tools may have different context-window limits, keep `CONTEXT.md` and `BACKLOG.md` short and current — trim completed detail out regularly rather than letting them grow indefinitely; the docs in `/docs` are the durable spec, `CONTEXT.md` is just "where are we right now."
- When an agent's output disagrees with a doc (e.g., it picks a different library), don't silently accept or silently revert — record the decision in `DECISIONS.md` either way, so it's consistent going forward.
- Since you have strong backend judgment, consider using your best free-tier agent for tickets 3–4 and 11 (the trickiest correctness-wise) and reserve less capable/free agents for repetitive CRUD/UI tickets (8, 9, 12, 13) where the pattern is copy-paste-adjacent once ticket 1–7 establish it.
