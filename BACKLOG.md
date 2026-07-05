# Backlog

Live ticket checklist for EduSuite. Keep this current at the end of each agent session. Detailed spec lives in `/docs/05_ROADMAP_MILESTONES.md` and `/docs/08_AI_AGENT_EXECUTION_PLAN.md`.

## Milestone 0 — Foundations

- [x] Repo scaffold: Spring Boot 3 project (Java 21), Spring Modulith, base package structure, `docker-compose.yml`, CI workflow.
- [x] `Tenant` entity + Flyway migration + `TenantRepository`.
- [x] RLS policy + Postgres session variable wiring + `TenantContext` filter.
- [x] Cross-tenant isolation test (`TenantIsolationIT`) against real Postgres via Testcontainers.
- [ ] Keycloak reproducibility: verified import/setup script, Spring Security OAuth2 resource-server config, one working login → JWT → authenticated request flow.
- [ ] Spring Modulith skeleton: two modules + one cross-module event + Modulith verification test.
- [ ] Audit logging aspect wired to one entity end-to-end.
- [ ] `CONTEXT.md`, `DECISIONS.md`, `BACKLOG.md` initialized and committed.

**Exit criterion:** an engineer can log in, hit one tenant-scoped API, see an audit log entry, and the whole thing deploys via a single pipeline run.

## Milestone 1 — Walking Skeleton / Single-Module Pilot (Attendance)

- [ ] Tenant self-signup + setup wizard (institute name, board, academic year, first admin).
- [ ] Core academic structure: `AcademicYear`, `Course`, `Section` entities + CRUD APIs + migrations.
- [ ] `Person` + `Student` entities + CRUD API + CSV bulk-import endpoint with validation report.
- [ ] RBAC: `Role`/`UserRole`/permission check annotations wired to real endpoints (Admin vs Teacher access).
- [ ] `AttendanceRecord` entity + mark-attendance API (single + bulk-for-section) + attendance report per section per date range.
- [ ] Frontend: setup wizard screen.
- [ ] Frontend: Admin screens for sections + student list/import.
- [ ] Frontend: Attendance marking screen (web-first or mobile-PWA-first — record decision in `DECISIONS.md`).
- [ ] Module-toggle mechanism (`TenantModuleSubscription` table + `@RequiresModule` guard + frontend nav filtering).
- [ ] End-to-end Playwright test: signup → wizard → create section → add students → mark attendance → view report.

**Exit criterion:** a design-partner institute can create sections, add students, and have a teacher mark attendance for a real week.

## Milestone 2 — MVP

- [ ] Fees & Billing module (fee structure, online payment collection, receipts).
- [ ] Notice board + basic notifications (email + one SMS/WhatsApp provider).
- [ ] Community mobile PWA for Parent (attendance, fee status, notices).
- [ ] Module toggle mechanism live for Fees / Attendance extras.
- [ ] Platform billing (Starter/Growth/Scale) wired to a payment gateway.
- [ ] Basic in-app onboarding (first-run checklist, contextual tooltips).
- [ ] Landing page + trial signup flow + Chatwoot support widget.

## Milestone 3+ — Depth & Retention / Scale

- [ ] Examination & Gradebook + report card generation.
- [ ] Timetable module.
- [ ] Admissions/Enquiry module.
- [ ] Teacher-facing mobile PWA additions.
- [ ] Library, Transport, Hostel modules.
- [ ] Metered add-on billing.
- [ ] HR/Payroll (basic).
- [ ] Multi-campus support.
