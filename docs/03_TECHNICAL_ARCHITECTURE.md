# 03 — Technical Architecture

## 1. Architectural style: Modular Monolith
**Decision:** Single deployable Spring Boot application, internally organized into strongly-bounded modules, backed by one primary database using a shared-schema multi-tenancy strategy. **Not microservices** for MVP.

**Why:** Microservices buy you independent scaling and independent deployment at the cost of distributed transactions, service discovery, network reliability handling, and multiplied DevOps surface area — costs that make sense at scale, not for a two-to-few-person team trying to reach MVP fast. A well-structured modular monolith gives ~90% of the maintainability benefit of microservices with ~20% of the operational cost, and can be split into services later **module-by-module** if a specific module (e.g., Notifications, or Document Generation) genuinely needs independent scaling.

**Enforcing modularity inside the monolith** (so it doesn't rot into a "big ball of mud"):
- Use **Spring Modulith** (official Spring project for structuring modular monoliths) to define module boundaries as Java packages, enforce that modules only communicate through explicit public APIs or Spring application events, and get compile-time/test-time verification of boundary violations.
- Each module = one top-level package (e.g., `com.edusuite.attendance`, `com.edusuite.fees`, `com.edusuite.library`) with an internal (`.internal`) package for anything not meant to be called directly by other modules.
- Cross-module interaction rules (ties to functional doc §4):
  - **Core modules** (tenant, user/RBAC, academic structure, notification, audit) expose public service interfaces other modules may call directly.
  - **Optional modules** (library, transport, hostel, etc.) interact with each other only via Spring application events (in-process) — this is what lets a tenant disable a module cleanly and lets us later extract a module into its own microservice without rewriting its consumers (they already only depend on events/interfaces).

## 2. Tech stack
| Layer | Choice | Why |
|---|---|---|
| Backend | Java 21 (LTS) + Spring Boot 3.x | As requested; mature ecosystem, virtual threads (Java 21) help with I/O-heavy multi-tenant workloads |
| Modularity | Spring Modulith | Enforces module boundaries, event-driven inter-module communication, generates module documentation |
| API style | REST (OpenAPI 3 spec generated via springdoc) for MVP; consider GraphQL later only if the mobile app needs flexible querying | REST is simpler to secure, cache, and version for a small team |
| Database | PostgreSQL | Row-Level Security support (useful for tenant isolation), strong JSON support (for tenant-configurable fields), open-source, huge talent pool |
| Caching | Redis | Session cache, rate limiting, feature-flag cache |
| Auth | Keycloak (open-source, self-hosted or managed) for identity/OAuth2/OIDC, JWT-based API auth | Don't build auth/RBAC from scratch — Keycloak supports multi-realm or custom multi-tenant claims, MFA, social login (useful for parent app signup), password policies out of the box |
| Frontend (Admin/Staff Web) | React + TypeScript, Vite, TanStack Query, Tailwind CSS | Modern, fast dev cycle, huge component ecosystem |
| Frontend (Community app) | Same React stack, built as a responsive PWA (Workbox for offline caching); wrap in Capacitor for native app store presence in a later milestone (same codebase, not a rewrite) | Avoids maintaining 3 separate frontends for MVP |
| Marketing site | Next.js (SEO-friendly, static generation) or a simple CMS (see below) | Landing pages need fast SEO-friendly rendering |
| Support/Helpdesk | Chatwoot (open-source, self-hostable customer support/helpdesk + live chat widget) | Don't build ticketing from scratch — embed on landing page and support portal |
| File/document storage | S3-compatible object storage (AWS S3 or MinIO if self-hosting) | For uploads, generated PDFs, templates |
| Document generation | JasperReports or a template engine (Thymeleaf → HTML → PDF via wkhtmltopdf/Playwright) | Report cards, ID cards, certificates |
| Messaging/queue | Spring Events for in-process; RabbitMQ (or AWS SQS) introduced only when async cross-boundary work (e.g., bulk SMS jobs, report generation) needs durability beyond a single process | Avoid Kafka-scale infra until there's a real need |
| SMS/WhatsApp | Pluggable provider interface; start with one India-first provider (e.g., MSG91, Gupshup, or Twilio for WhatsApp Business API) behind an internal `NotificationGateway` interface so providers can be swapped/added without touching business modules | |
| Payments | Razorpay or Cashfree (India-first, UPI support) behind a `PaymentGateway` interface; add Stripe for international tenants later | |
| CI/CD | GitHub Actions | Free tier is generous, integrates with GitHub-hosted code |
| Cloud | AWS (or GCP) — start with a single region (ap-south-1 Mumbai), managed Postgres (RDS), managed Redis (ElastiCache), container hosting via ECS Fargate or a simple managed Kubernetes (EKS) only once team is ready to operate k8s | Prefer managed services over self-ops for a small team |
| Observability | OpenTelemetry + Grafana/Loki/Tempo stack (open-source) or a managed option (Grafana Cloud free tier) | |
| IaC | Terraform | Reproducible infra, version-controlled |

## 3. Multi-tenancy strategy
**Approach: Shared database, shared schema, discriminator column (`tenant_id`) + PostgreSQL Row-Level Security (RLS).**

Why not schema-per-tenant or database-per-tenant: those give stronger isolation but become an operational burden (migrations must run N times, connection pool per tenant, etc.) at the scale we're targeting (hundreds, not tens-of-thousands, of small tenants for the foreseeable future). Shared-schema + RLS gives strong logical isolation with one migration path.

Implementation:
- Every tenant-owned table has a `tenant_id` column (indexed, part of most composite indexes).
- A Postgres RLS policy on each such table restricts rows to `current_setting('app.current_tenant')`.
- A Spring `TenantContext` (backed by `ThreadLocal`/reactive context) is populated from the authenticated JWT's tenant claim at the start of each request via a servlet filter, and sets the Postgres session variable for RLS + is used by a Hibernate multi-tenancy filter as a second line of defense (belt-and-suspenders: even a query bug can't leak cross-tenant data because RLS enforces it at the DB layer too).
- **Escape hatch reserved for the future**: if a large enterprise customer requires dedicated infrastructure (common ask from big institutions/government), the same schema can be deployed as a dedicated single-tenant instance without redesign — this is a valid "some tenants get a different deployment model" pattern, not something to build now.

## 4. Module entitlement (the "toggle modules per tenant" mechanism)
- A `tenant_module_subscription` table: `tenant_id, module_key, enabled, plan_source (included_in_tier | addon), enabled_at`.
- A lightweight **feature-flag style check** (`ModuleAccessGuard`) at three layers:
  1. **API layer**: a Spring interceptor/annotation (`@RequiresModule("LIBRARY")`) rejects requests to disabled-module endpoints with a clear 403 + reason.
  2. **Frontend**: the tenant's enabled-module list is part of the session bootstrap payload; navigation and screens for disabled modules simply don't render (not hidden-but-present).
  3. **Event layer**: modules that are disabled simply don't register their event listeners, so no wasted work happens even internally.
- Consider open-source feature-flag tooling (e.g., **Unleash**, self-hosted) once flag count grows beyond simple module on/off — not needed for MVP, plain DB table + cache is enough.

## 5. Domain modules designed for reuse (hospital management, later)
To honor the "keep some modules generic enough to reuse" goal **without** over-engineering now, only these cross-domain-generic modules get designed with a slightly wider lens (because schools and hospitals both need them):
- **Party/Identity module** (person records, roles, relationships — a "Guardian-of-Student" relationship is structurally the same shape as "Next-of-kin-of-Patient"). Keep the schema generic (`Person`, `PersonRelationship`) but the *business logic* (student-specific fields) lives in the SIS module, not in Party.
- **Scheduling/Timetable module** (resource + time-slot + conflict detection) — same core idea as hospital OPD/OT scheduling.
- **Billing/Invoicing module** — fee structures, installments, receipts are structurally similar to hospital billing.
- **Document Generation module** — templated PDF generation is domain-agnostic.
- **Notification/Communication module** — fully domain-agnostic already.

Everything else (Attendance-for-students, Gradebook, Library, Hostel, Admissions) is **education-specific and should not be generalized** — trying to make "Attendance" generic enough for both a classroom and a hospital ward is exactly the kind of premature abstraction to avoid. If the hospital product happens, expect to build hospital-specific modules fresh, reusing only the five modules above plus the multi-tenancy/auth/billing platform scaffolding.

## 6. API design principles
- REST, resource-oriented, versioned via URL path (`/api/v1/...`).
- OpenAPI spec auto-generated (springdoc-openapi) and published — enables early frontend/backend parallel work and future partner integrations.
- Consistent envelope for errors (RFC 7807 Problem Details).
- Idempotency keys required on all payment/fee-related POST endpoints.
- Pagination (cursor-based for large tenant datasets like student lists) standardized across all list endpoints from day one.

## 7. Frontend architecture notes
- Component library: build a small internal design system on top of a headless component kit (e.g., Radix UI) + Tailwind, rather than a heavy pre-styled kit — keeps branding flexible per tenant (tenant logo/color theming is a stated requirement) without fighting a framework's opinions.
- In-app onboarding/tooltips: use an open-source product-tour library (e.g., **Shepherd.js** or **Intro.js**) rather than building a custom tour engine.
- Offline tolerance for the community app (attendance marking in low-connectivity areas): local-first queue (IndexedDB via a small sync layer) with conflict resolution favoring "last teacher action wins" for MVP simplicity.

## 8. Non-functional targets for MVP
- p95 API latency < 400ms for standard CRUD under expected MVP load (a few hundred concurrent tenants, low thousands of daily active users).
- 99.5% uptime target for MVP (not 99.99% — right-sized for stage, revisit SLA once selling to larger institutions).
- RTO/RPO: daily automated backups, 24h RPO acceptable for MVP; tighten as paying customer base grows.
