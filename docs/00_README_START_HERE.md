# EduSuite (working name) — Project Documentation Set

## What this is
A multi-tenant SaaS platform for education institution management (schools, colleges, universities, coaching institutes), targeted first at the Indian market with international expansion in mind. Modular by design — a tenant only pays for and sees the modules it enables.

## Who this is for
This documentation set is written so that an AI coding agent (or a human developer) can implement the product with minimal back-and-forth. Each document is self-contained but cross-references others. Read in this order:

1. `01_VISION_MARKET_BUSINESS_MODEL.md` — why we're building this, who we sell to, pricing.
2. `02_FUNCTIONAL_REQUIREMENTS.md` — every module, its features, user stories, personas.
3. `03_TECHNICAL_ARCHITECTURE.md` — system architecture, tech stack, multi-tenancy, module framework.
4. `04_DATA_MODEL.md` — core entities, ERD description, tenant isolation strategy.
5. `05_ROADMAP_MILESTONES.md` — phased delivery plan, MVP scope, what ships in each milestone.
6. `06_DEVOPS_SECURITY_TESTING.md` — CI/CD, cloud infra, security & audit baseline, test strategy.
7. `07_BILLING_SUPPORT.md` — billing engine design, tier definitions, customer support system.

## Ground rules for whoever (or whatever) implements this
- **Bias toward boring, proven tech.** Modular monolith, not microservices, until there's a concrete scaling reason not to.
- **Ship the smallest usable slice first.** Each roadmap milestone must be independently demo-able and sellable — no milestone should require the next one to be "worth it."
- **Reuse open source instead of building infra.** (auth, support desk, email/SMS, feature flags, observability — see architecture doc for specific picks.)
- **Every module is optional per tenant.** A module being "off" must mean zero UI clutter and zero unnecessary cost for that tenant, not just a hidden menu item.
- **Design for India first, structure for the world.** Currency, locale, date format, boards/grading systems, tax (GST) are configuration, not hardcoded assumptions.
- **Security, testing and audit logging are not a "later" phase.** They are baked into Milestone 0 (see roadmap).

## Product name
"EduSuite" is a placeholder — check trademark/domain availability before committing (recommend checking via MCA/IP India trademark search and domain registrars before launch).
