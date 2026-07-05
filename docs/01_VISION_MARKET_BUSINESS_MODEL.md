# 01 — Vision, Market & Business Model

## 1. Problem statement
The majority of small-to-mid-size schools, colleges and coaching institutes in India (and similar emerging markets) run on paper registers, WhatsApp groups, and spreadsheets. Existing ERP options (Fedena, Entab CampusCare, Teachmint, Vidyalaya, etc.) are either:
- Priced/packaged for larger institutions (per-student fees that punish growth, "custom pricing" opacity),
- Feature-heavy but hard to onboard without a "technical person" or a vendor-led setup project (weeks of implementation),
- Or dated in UI/UX, hurting daily adoption by teachers and parents.

There is a clear underserved segment: small schools, single-branch coaching institutes, and standalone colleges who want to self-serve a trial, turn on only what they need, and get real value within a day — not after a 4–8 week implementation project.

## 2. Target customers (in priority order for MVP)
1. **Coaching institutes / tutorial centers** (single location, 100–2000 students) — simplest data model, fastest sales cycle, highest pain around fee collection and attendance.
2. **K-12 private schools** (unaffiliated or single-board, 200–1500 students) — need report cards, timetables, parent communication.
3. **Small colleges** — more complex (semesters, credits) — targeted after MVP validates core.
4. **International** (Southeast Asia, Africa, Middle East) — same product, config-driven locale/currency/board — phase 3+, not MVP.

## 3. Differentiation / wedge
- **Modular pricing**: only pay for modules you use — a coaching institute with no library, hostel or transport shouldn't pay for or see those modules.
- **Self-serve trial**: signup → guided setup wizard → usable system in under 30 minutes, no salesperson required to start (sales-assist available for larger accounts).
- **In-app learning**: contextual tooltips, an interactive first-run checklist per role, and a searchable in-app help center — instead of "call our support to get trained."
- **Modern UX** as the primary differentiator against incumbents whose common complaint is "dated interface."
- **Transparent, gentle pricing** — no "contact us for pricing," no per-student penalty that punishes growth in the low tiers.

## 4. Business model
**Multi-tenant SaaS**, hybrid pricing:
- **Tiered flat subscription** (Starter / Growth / Scale) covering the base platform + a generous student/user allowance — gives predictable recurring revenue and a friction-free signup.
- **Metered add-ons introduced only after MVP validation** (Phase 2+): WhatsApp/SMS credits, extra storage, extra document generation (e.g., bulk report cards), premium support SLA. This is deliberately *not* in MVP — see architecture doc for rationale (billing/metering infra is real engineering cost that should be justified by revenue first).
- **Annual billing discount** (e.g., 2 months free on annual vs monthly) to improve cash flow and reduce churn — Indian schools budget annually, so this aligns naturally.
- Full pricing tier detail lives in `07_BILLING_SUPPORT.md`.

## 5. Go-to-market approach
- Free trial (14–21 days, full feature access, no credit card) — removes the biggest signup friction competitors have.
- Landing page + inbound content (India-specific SEO: "school ERP for coaching institutes," "free school management software India") — supported by the customer support system in doc 07.
- Direct outreach to 20–30 local institutes for design-partner feedback before public launch (validates MVP before spending on marketing).
- Referral incentive (discount for referring another institute) — cheap CAC channel in a networked market (school principals talk to each other).

## 6. Success criteria for "should we keep building this?"
Defined checkpoints (tie to roadmap milestones):
- **After MVP (Milestone 2):** 5–10 paying or committed-trial institutes actively using attendance + fees weekly.
- **After Milestone 3:** ≥40% of trial signups convert to paid within 30 days; churn <5%/month.
- **After Milestone 4:** positive unit economics (revenue per tenant > infra + support cost per tenant) before investing further in modules 6+.

If these gates aren't met, the roadmap explicitly supports stopping and taking stock rather than continuing to build (see doc 05, "Go/No-Go Gates").

## 7. Explicit non-goals for MVP (to prevent scope creep)
- No AI-based grading/personalization (nice-to-have later, not a wedge).
- No native mobile apps for MVP — responsive PWA first (see architecture doc); native app is a post-MVP milestone.
- No multi-currency/multi-language beyond English + Hindi for MVP; internationalization framework exists from day one, but only English is fully translated.
- No hospital management work of any kind in MVP — only module *boundaries* are kept clean enough to reuse later (see architecture doc §"Domain modules designed for reuse").
