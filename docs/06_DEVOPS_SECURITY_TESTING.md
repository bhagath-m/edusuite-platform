# 06 — DevOps, Security, Auditing & Testing

Principle: these are Milestone-0 concerns, not "phase 3 hardening." Retrofitting security/testing discipline into a live multi-tenant system with real student data is far more expensive than building it in from the start.

## 1. DevOps / Cloud
- **Cloud provider:** AWS, region `ap-south-1` (Mumbai) for data residency and latency for the India-first launch.
- **Compute:** Start with ECS Fargate (containers, no server management) for the Spring Boot app and the frontend static hosting via S3+CloudFront. Move to EKS only if/when operational needs justify Kubernetes complexity.
- **Database:** Amazon RDS for PostgreSQL (Multi-AZ once paying customers exist; single-AZ acceptable for pre-revenue dev/staging).
- **Cache:** Amazon ElastiCache (Redis).
- **IaC:** Terraform, with separate state per environment (dev/staging/prod), stored in a remote backend (S3 + DynamoDB lock table).
- **CI/CD:** GitHub Actions — pipeline stages: lint → unit tests → build → integration tests (Testcontainers spinning up real Postgres) → security scan → deploy to staging → (manual approval) → deploy to prod.
- **Environments:** dev (ephemeral/local via Docker Compose), staging (mirrors prod, used for design-partner UAT), prod.
- **Secrets:** AWS Secrets Manager (or Parameter Store), never in source control or environment files checked into git.
- **Backups:** automated daily RDS snapshots + point-in-time recovery enabled from day one; periodic restore drills (quarterly) to actually verify backups work.

## 2. Security baseline (from Milestone 0)
- **AuthN/AuthZ:** Keycloak-issued OIDC/JWT, short-lived access tokens + refresh tokens, MFA available (encourage for Admin role at minimum).
- **Transport security:** TLS everywhere (enforced HTTPS, HSTS), no plaintext internal traffic between components beyond a private VPC.
- **Tenant isolation:** enforced at DB layer via Postgres RLS (doc 04) + application-layer filter — defense in depth, tested with an explicit automated test suite that tries to access another tenant's data and asserts failure.
- **Input validation:** Bean Validation (JSR-380) on all API inputs; output encoding to prevent XSS on the frontend; parameterized queries only (JPA/Hibernate handles this by default — no raw string-concatenated SQL).
- **OWASP Top 10 baseline:** dependency vulnerability scanning (e.g., GitHub Dependabot / OWASP Dependency-Check) in CI; SAST scanning (e.g., Semgrep or SonarQube Community) in CI on every PR.
- **Secrets & PII handling:** student/parent contact info, health notes, payment references are sensitive — encrypt at rest (RDS encryption) and restrict which roles/modules can read which fields (field-level authorization for sensitive fields like medical notes).
- **Data retention & deletion:** since this handles minors' data, define a data retention policy and support tenant-initiated data export/deletion (also aligns with India's DPDP Act 2023 obligations around children's data — data of anyone under 18 has extra consent/processing restrictions under that law; consult a lawyer for compliance sign-off before general availability, this doc is not legal advice).
- **Rate limiting & abuse prevention:** per-tenant and per-IP rate limits (via Redis) on public endpoints (login, trial signup, payment webhook receivers).
- **Payment security:** never store raw card data — rely entirely on the payment gateway's hosted checkout/tokenization (Razorpay/Cashfree handle PCI-DSS scope).

## 3. Audit logging
- A cross-cutting audit aspect (Spring AOP interceptor) around all state-changing service methods, capturing: tenant, actor, action, entity type/id, before/after diff (as JSON), timestamp, IP/user-agent.
- Audit log is **append-only** (no update/delete permission at the DB role level for the application user) and tenant-scoped for tenant admins to review "who changed this fee structure" style questions themselves, reducing support burden.
- Platform Super Admin actions (e.g., impersonating a tenant user for support) are audited with extra visibility — every impersonation session should be logged and, ideally, visible to the tenant admin ("Support accessed your account on [date] for ticket #123") for trust.

## 4. Testing strategy
| Test type | Tooling | When |
|---|---|---|
| Unit tests | JUnit 5 + Mockito | Every PR, per module, target >70% coverage on business logic (not a vanity 100% target) |
| Module boundary tests | Spring Modulith's built-in verification | Every PR — fails the build if a module reaches into another module's internals |
| Integration tests | Testcontainers (real Postgres, real Redis) | Every PR for modules touching persistence |
| Contract/API tests | Spring Cloud Contract or simple REST-assured tests against the OpenAPI spec | Every PR |
| Tenant isolation tests | Custom suite explicitly attempting cross-tenant reads/writes | Every PR — this is a correctness-critical suite, treat failures as release-blocking |
| End-to-end tests | Playwright (web + PWA flows) | Nightly + before each deploy to staging, covering the 3–5 most critical user journeys (signup, attendance marking, fee payment) |
| Load/performance tests | k6 or Gatling | Before each major milestone release, against staging |
| Security scanning | Dependabot/OWASP Dependency-Check + Semgrep/SonarQube | Every PR |
| Manual UAT | Design-partner institutes | Before each milestone goes to a wider trial audience |

## 5. Observability
- Structured logging (JSON logs) with tenant_id and request-id correlation on every log line.
- OpenTelemetry traces across the request lifecycle; metrics (request rate, error rate, latency per endpoint) exported to Grafana (self-hosted or Grafana Cloud free tier for MVP).
- Alerting on: error rate spikes, failed payment webhook processing, RLS policy violations (should be zero — alert immediately if one is ever caught), backup job failures.
