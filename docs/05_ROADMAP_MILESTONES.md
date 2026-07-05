# 05 — Roadmap & Milestones

Design principle: **every milestone below is independently demo-able to a real institute and independently sellable** (or at least pilot-able) — not just an internal engineering checkpoint. This is deliberate, so progress feels real and the option to stop-and-assess exists at every stage, per your "small wins, not overwhelm" goal.

## Milestone 0 — Foundations (2–4 weeks)
Not customer-visible, but non-negotiable before feature work, because retrofitting these later is expensive:
- Repo setup, CI pipeline (build/test/lint on every PR), code quality gates.
- Multi-tenancy scaffolding (tenant table, RLS pattern, TenantContext) proven with one dummy table end-to-end.
- Auth via Keycloak wired up (login, JWT issuance, role claim) — one working login flow.
- Base module framework (Spring Modulith skeleton) with two dummy modules to prove the boundary/event pattern.
- Audit logging aspect wired to one entity, end-to-end.
- Cloud environment (dev + staging) provisioned via Terraform; first CI/CD deploy to staging.
- **Exit criterion:** an engineer can log in, hit one tenant-scoped API, see an audit log entry, and the whole thing deploys via a single pipeline run.

## Milestone 1 — Walking Skeleton / Single-Module Pilot (3–5 weeks)
Ship the smallest real thing a real institute could use:
- Tenant self-signup + setup wizard (institute name, board, academic year, first admin).
- Core: Academic structure (courses/sections), User & role management, basic Student Information System (add/import students).
- **One full vertical module: Attendance** (mark, view, basic report) — chosen because it's the highest-frequency daily action and easiest to demo value in one sitting.
- Admin web app only (community app not yet needed if staff can mark attendance from web at this stage — or, if teacher-mobile-first is important to you, swap Attendance's UI target to mobile PWA here instead of web).
- **Exit criterion:** a design-partner institute can create their sections, add students, and have a teacher mark attendance for a real week — measure whether they *actually keep doing it* unprompted.

## Milestone 2 — MVP (6–10 weeks after Milestone 1)
Add just enough breadth to be a believable product, not yet the full module catalog:
- Fees & Billing module (fee structure, online payment collection, receipts) — the second-highest-value daily/weekly workflow.
- Notice board + basic notifications (email + one SMS/WhatsApp provider).
- Community mobile PWA for Parent (view attendance + fee status + notices) — this is often the "wow" moment for a school evaluating the product.
- Module toggle mechanism live (tenant can enable/disable Fees or Attendance-extras from settings).
- Platform billing (our own tier subscription, Starter/Growth/Scale flat pricing — see doc 07) wired to a payment gateway, so this is genuinely sellable, not just a free pilot.
- Basic in-app onboarding (first-run checklist, 5–10 contextual tooltips on the highest-friction screens).
- Landing page + trial signup flow + Chatwoot support widget live.
- **Exit criterion / Go-No-Go Gate A:** 5–10 institutes actively using Attendance + Fees weekly without hand-holding; at least a few willing to pay. If this bar isn't met, pause and diagnose before building more modules — the platform's core loop, not breadth, is the thing being validated here.

## Milestone 3 — Depth & Retention (parallel-track, post Go/No-Go Gate A)
Only proceed here once Gate A is passed:
- Examination & Gradebook + report card generation (board-specific templates for the pilot institutes' actual boards).
- Timetable module.
- Admissions/Enquiry module (helps sell to institutes evaluating for the *next* academic year — strong seasonal sales trigger in India, typically Nov–May).
- Teacher-facing mobile PWA additions (gradebook entry, homework posting).
- Expanded onboarding/help center content based on real support tickets from Milestone 2 cohort.
- **Exit criterion / Gate B:** ≥40% trial-to-paid conversion within 30 days across new signups; support ticket volume per tenant trending down month over month (signals the self-serve onboarding is actually working).

## Milestone 4 — Scale-Readiness & Optional Modules
Only after Gate B:
- Library, Transport, Hostel modules (built to the module-boundary discipline in doc 03 — these are the best proof points that "modules a tenant can skip" actually works end-to-end).
- Metered add-on billing (WhatsApp/SMS credit packs, extra storage) — introduced now, not at MVP, per business doc rationale.
- HR/Payroll (basic).
- Native app packaging (Capacitor) if PWA adoption data shows a real need for app-store presence.
- Multi-campus support for tenants with branches.
- Security/scale hardening: load testing, tightening RPO/RTO, considering read replicas.

## Milestone 5 — International Readiness (only if pursuing non-India markets)
- Full i18n (additional languages beyond English/Hindi as needed per target market).
- Additional payment gateways (Stripe, regional providers), additional currencies.
- Configurable grading/board templates beyond Indian boards.
- Data residency considerations per target country's regulations.

## Milestone 6+ — Second product exploration (hospital management, or others)
Only after this product is either profitable-and-stable-enough to fund a second bet, or the "should we keep building this" gates fail and you're deciding what's next. At that point, doc 03 §5 tells you exactly which modules (Party, Scheduling, Billing, Document Generation, Notification) are extractable as shared platform scaffolding — re-read that section before starting a second product.

## Cadence & team-size note
This roadmap assumes a very small team (1–3 engineers). Milestones are sized to feel like 2–10 week chunks, each ending in something demoable to a real person outside the team — treat every milestone's exit criterion as a mandatory checkpoint, not a formality.
