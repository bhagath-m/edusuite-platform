# Decision Log

This file records major architectural and design decisions made during implementation. It is append-only: once a decision is recorded, update its status rather than deleting it.

## 2024-001: Postgres with Row-Level Security for tenant isolation
- Status: DECIDED
- Context: The platform is multi-tenant SaaS. Tenant data must be isolated at the database layer so application bugs cannot leak rows across tenants.
- Decision: Use PostgreSQL Row-Level Security (RLS) policies tied to a session-level `app.current_tenant_id` setting. Each tenant-scoped table gets an RLS policy comparing `tenant_id` to `NULLIF(current_setting('app.current_tenant_id'), '')::uuid`.
- Consequences: All tenant-scoped queries are filtered by Postgres regardless of application code. Requires the app to connect via a restricted role (`edusuite_app`) so RLS is enforced. Every new tenant-scoped table must copy the exact RLS pattern.

## 2024-002: Hibernate multi-tenancy via `TenantContext` + `TenantIdentifierFilter`
- Status: DECIDED
- Context: Hibernate needs a tenant identifier per session/transaction to route connections and qualify queries.
- Decision: Implement Hibernate's multi-tenancy SPI using `CurrentTenantIdentifierResolverImpl` reading from `TenantContext`, plus `TenantAwareConnectionProvider` setting the Postgres session variable. A servlet filter (`TenantIdentifierFilter`) extracts `tenant_id` strictly from the authenticated JWT and stores it in `TenantContext`.
- Consequences: Tenant resolution is centralized and never driven by client input. The filter must run after authentication. The platform `tenant` table lookup is bypassed for the identifier because the JWT already carries it.

## 2024-003: Keycloak as OAuth2 / OIDC identity provider
- Status: DECIDED
- Context: EduSuite needs identity, login, role claims, and JWT issuance without building an identity provider.
- Decision: Use Keycloak as the OAuth2 / OIDC provider. A realm export includes a custom `tenant_id` mapper attached to users, and test users are pre-scoped to tenants for local development.
- Consequences: User management, password policies, MFA, and federation are delegated to Keycloak. Local dev requires the Keycloak container. Production deployment will need a reproducible realm import or Terraform equivalent.

## 2024-004: Spring Modulith for modular monolith
- Status: DECIDED
- Context: The product has many functional modules (Attendance, Fees, Library, etc.) and must stay cheap to operate and reason about early on. Microservices are premature.
- Decision: Build a modular monolith with Spring Modulith. Each module has its own package, exposes only intended public types/APIs, and communicates via domain events.
- Consequences: Modules can be reasoned about and tested in isolation while sharing a single deployable. Event-driven boundaries are enforced via Spring Modulith verification tests. Extracting a module to a service later is possible because boundaries are explicit.

## 2024-005: Platform-level `Tenant` table outside RLS
- Status: DECIDED
- Context: There must be a place to store global tenant metadata (name, subdomain, institute type, status) that is visible to platform administrators and used before a tenant-specific session is established.
- Decision: Keep the `tenant` table as a platform-level table without RLS. Tenant-scoped tables reference it via `tenant_id`.
- Consequences: The platform can list and manage tenants without switching identities. Any access control to tenant metadata must be enforced at the application layer for this table.

## 2024-006: UUID primary keys
- Status: DECIDED
- Context: Multi-tenant SaaS entities need globally unique identifiers that are safe to expose in URLs/APIs and easy to generate without central coordination.
- Decision: Use UUID (version 7-ready where supported, currently random UUID) for all primary keys.
- Consequences: IDs are opaque, non-sequential, and safe across tenants. Slightly larger storage and index cost than BIGINT, but acceptable for the isolation and merge safety benefits.

## 2024-007: Java 21 + Spring Boot 3
- Status: SUPERSEDED
- Context: The platform targets a small team and needs long-term support, modern language features, and a mature ecosystem.
- Decision: Build on Java 21 (LTS) and Spring Boot 3.3.x with Spring Modulith, Spring Security OAuth2 resource server, and Flyway.
- Consequences: Developers can use virtual threads and newer Java APIs. Dependency versions are managed by Spring Boot BOM. Spring Security 6 configuration patterns must be followed.
- Superseded by: 2025-001 (Java 26 + Spring Boot 4.1.0)

## 2024-008: Local-first environment with Docker Compose
- Status: DECIDED
- Context: The project is bootstrapped on free-tier resources and small-team cadence; cloud cost must be deferred until there is a design partner.
- Decision: Run Postgres, Redis, and Keycloak locally via Docker Compose through Milestone 0 and Milestone 1. Defer Terraform/AWS until Milestone 2.
- Consequences: Fast local iteration and zero cloud spend early. A migration to staging infra will be needed before the Milestone 2 pilot.

## 2025-001: Java 26 + Spring Boot 4.1.0
- Status: DECIDED
- Context: The platform is on Spring Boot 4.1.0 / Java 26, upgraded as part of the Milestone 0 hardening/upgrade pass. This aligns with the Spring Boot 4 baseline and enables Java 26 preview/stable features.
- Decision: Use Java 26 as the compile/runtime JDK and Spring Boot 4.1.0 (plus Spring Modulith 2.1.0, Testcontainers 1.21.4, ArchUnit 1.4.2) as the application baseline.
- Consequences: Developers must use JDK 26, the CI matrix uses JDK 26, and dependency overrides (ArchUnit, Testcontainers) are captured in `pom.xml` `dependencyManagement`.
