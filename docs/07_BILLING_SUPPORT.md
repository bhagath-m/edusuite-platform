# 07 — Billing Model & Customer Support System

## 1. Pricing tiers (MVP — flat subscription, no metering yet)
Rationale for structure: gentle entry price, generous allowance so a small coaching institute never feels "nickel-and-dimed," clear upgrade path as an institute grows. Exact rupee figures need validation with real design-partner institutes (see Milestone 2 exit criterion) — the structure below is the framework to plug numbers into.

| Tier | Target | Included | Illustrative structure |
|---|---|---|---|
| **Starter** | Small coaching institutes, <300 students | Core + Attendance + Fees + Notices; 1 campus; email + basic SMS notifications; community support (help center + ticket, best-effort response) | Low flat annual price, positioned well below Fedena's entry pricing (~₹40k+/yr per public estimates) — e.g., a simple flat annual fee regardless of exact student count up to the cap, mirroring the "flat, unlimited-within-cap" pricing pattern some newer Indian competitors already use successfully |
| **Growth** | Schools/colleges, 300–1500 students | Everything in Starter + Examination/Gradebook + Timetable + Admissions + WhatsApp notifications + priority ticket support | Moderate step-up, still flat |
| **Scale** | Larger institutions, multi-campus, >1500 students | Everything + Library/Transport/Hostel/HR modules + multi-campus + SLA-backed support + dedicated onboarding call | Higher flat tier, still transparent (no "contact us" pricing — a stated differentiator vs. incumbents) |

- **Annual billing discount** (e.g., pay annually, get ~2 months free vs monthly) — improves cash flow, matches how Indian institutes budget.
- **14–21 day free trial**, full feature access, no credit card required to start (removes signup friction — directly targets the competitor weakness of opaque/heavy sales processes).
- **Module add-ons within a tier**: a tenant on Starter can add an individual optional module (e.g., just Library) for a small flat add-on fee, without upgrading the whole tier — this is what makes "modular pricing" real rather than just three fixed bundles.

## 2. Phase 2+: usage-based add-ons (explicitly NOT in MVP)
Introduce only after Gate A/B in the roadmap are met, because metering, invoice reconciliation, and "why was I charged extra" support load are real costs that should be justified by proven revenue first:
- WhatsApp/SMS credit packs (pay-as-you-go top-up, since messaging costs are genuinely variable and per-provider).
- Extra storage beyond a generous default (for institutes with heavy document/photo usage).
- Bulk operations (e.g., large-batch report card PDF generation) beyond a fair-use threshold.

Billing engine implementation note for when this phase starts: don't build a metering system from scratch — evaluate an open-source or lightweight metered-billing layer (e.g., Lago, an open-source billing engine) integrated with the chosen payment gateway, rather than hand-rolling usage aggregation and invoicing logic.

## 3. Billing platform implementation (MVP)
- Subscription state lives in the platform's own `Tenant`/`TenantModuleSubscription` tables (doc 04).
- Payment gateway (Razorpay/Cashfree) subscription/recurring-payment APIs handle the actual recurring charge; our system reacts to webhook events (payment succeeded/failed/subscription cancelled) to update tenant billing_status.
- Dunning: on payment failure, grace period (e.g., 7 days) with in-app + email reminders before any feature restriction; never abruptly lock out an active school mid-term without warning — a missed payment risking a school's day-to-day operations is a support/trust disaster, handle gracefully.
- GST invoicing: Indian B2B SaaS must issue GST-compliant invoices — generate these automatically per billing cycle (again, don't hand-build tax logic if the payment gateway or a billing engine already produces compliant invoices).

## 4. Customer support system (backing the landing page)
- **Chatwoot** (open-source, self-hostable) as the single system for:
  - Live chat widget on the marketing/landing page (pre-sales questions, trial support).
  - Ticketing for existing tenant support requests (can be embedded inside the Admin web app too, so a tenant admin doesn't have to leave the product to get help).
  - A public help center/knowledge base (Chatwoot supports this, or pair with a simple docs site) — supports the "self-learning UI" goal by giving a searchable destination for the contextual tooltips to link out to.
- **Support tiering matches product tiers**: Starter = best-effort/community + help center; Growth = priority queue; Scale = SLA-backed + onboarding call — ties support cost to revenue instead of promising white-glove support to the lowest-paying tier.
- **Support-to-product feedback loop**: tag tickets by module/feature; review weekly during early milestones to directly inform which tooltips/onboarding steps need improvement — this is the mechanism that make "self-learning UI" actually improve over time instead of being a one-time design pass.
