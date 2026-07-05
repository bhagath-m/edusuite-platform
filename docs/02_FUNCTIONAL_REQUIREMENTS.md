# 02 — Functional Requirements

## 1. Personas
| Persona | Surface used | Key needs |
|---|---|---|
| **Institute Admin / Owner** | Web app | Setup, module toggling, staff/user management, billing, reports |
| **Front-office / Registrar staff** | Web app | Admissions, fee collection, attendance entry, document generation |
| **Teacher / Faculty** | Web app + mobile app | Attendance marking, gradebook, timetable, messaging parents |
| **Student** | Mobile app (+ web) | Timetable, homework, grades, fee status, library, notices |
| **Parent** | Mobile app | Ward's attendance/grades/fees, notices, direct messaging with teacher |
| **Support agent (our team)** | Helpdesk system | Handle tenant issues, view tenant health, respond to tickets |
| **Platform Super Admin (us)** | Internal admin console | Tenant provisioning, module entitlement, billing oversight, impersonation-for-support (audited) |

## 2. System surfaces (three distinct front-ends, one backend)
1. **Admin/Staff Web App** — full-featured web application (desktop-first), used by admins, registrar staff, and optionally teachers on desktop.
2. **Community Mobile/PWA App** — for teachers, students, parents; a responsive Progressive Web App for MVP (installable, offline-tolerant for key screens), native wrapper (React Native / Capacitor) in a later milestone reusing the same UI codebase.
3. **Marketing Website + Support Portal** — public landing page, pricing, signup/trial flow, help center/knowledge base, and a ticketing widget (see doc 07 for the specific open-source helpdesk).

## 3. Module catalog (each module independently toggleable per tenant)

### Core (always on — not a "module," part of the platform)
- Tenant setup wizard (institute profile, academic year, boards/grading system, branding — logo/colors on portals)
- User & role management (RBAC: Admin, Staff roles, Teacher, Student, Parent, custom roles per tenant)
- Academic structure: Institute → Campus (optional multi-branch) → Course/Grade/Class-Section → Subject
- Notifications (in-app, email, SMS/WhatsApp — provider-pluggable, see architecture doc)
- Notice board / circulars
- Document generation engine (ID cards, certificates, report cards — templated, tenant-customizable)
- Audit log (who did what, when — required for every module, see doc 06)

### Module: Admissions & Enrollment
- Enquiry capture (web form + manual entry), lead pipeline (Enquiry → Applied → Admitted → Enrolled)
- Online application form builder (tenant-configurable fields)
- Document upload & verification checklist
- Seat/quota management, waitlist

### Module: Student Information System (SIS)
- Student profile (personal, guardian, medical, previous school records)
- Class/section assignment, promotion/transfer between academic years
- Bulk import (CSV) with validation report

### Module: Attendance
- Daily/period-wise attendance (teacher marks; biometric/RFID integration point for later)
- Leave requests (student/staff) with approval workflow
- Attendance analytics & low-attendance alerts to parents

### Module: Fees & Billing (tenant's own student fees — distinct from our platform billing)
- Fee structure builder (heads, installments, late fee rules, scholarships/concessions)
- Online payment collection (Razorpay/Cashfree/PayU integration — India-first gateways, pluggable for international gateways later)
- Receipts, defaulter reports, reminders (SMS/WhatsApp/email)

### Module: Examination & Gradebook
- Exam scheduling, marks entry (teacher), configurable grading schemes (CBSE/ICSE/State board/percentage/GPA)
- Report card generation (templated, board-specific formats)
- Progress tracking over terms/years

### Module: Timetable
- Class/section timetable builder with conflict detection (teacher/room double-booking)
- Substitute teacher assignment on leave

### Module: Library Management
- Catalog (books/media), barcode/ISBN lookup
- Issue/return/reservation, fine calculation
- Optional — clearly independent of academics, first "toggle off" example

### Module: Transport Management
- Route & vehicle management, stop-wise student mapping
- Optional GPS tracking integration point (hardware-dependent, phase 2+)

### Module: Hostel Management
- Room/bed allocation, attendance, visitor log
- Optional, targeted at residential schools/colleges

### Module: HR & Payroll (staff-facing, distinct from student modules)
- Staff records, leave management, payroll processing (basic; deep payroll/compliance is a later milestone given India's PF/ESI/TDS complexity)

### Module: Communication / LMS-lite
- Homework assignment & submission
- Learning material sharing (files/links)
- Parent-teacher messaging thread (not open chat — structured, auditable)
- Note: full LMS (video lessons, quizzes, adaptive learning) is explicitly out of scope for MVP — coordinate with existing LMS via integration if a tenant already uses one.

### Module: Reports & Analytics
- Cross-module dashboards (attendance trends, fee collection %, exam performance)
- Exportable reports (PDF/Excel)

## 4. Module dependency rules (for the module framework in doc 03)
- Some modules have soft dependencies (e.g., Examination reads Academic Structure and SIS — those are core, always available).
- No module should hard-depend on another *optional* module. E.g., Transport must not require Hostel; Library must not require Fees. If a genuine cross-module feature exists (e.g., "add library fine to fee invoice"), it must be implemented as an **event-driven integration** (Library emits `FineIssued` event; Fees module optionally subscribes if enabled) — not a direct code dependency. This is the single most important functional rule for keeping modularity real rather than cosmetic.

## 5. Representative user stories (sample — full backlog to be maintained in the project tracker)
- As an **Admin**, during signup I complete a guided wizard (institute name, board, academic year, first admin user) and land on a dashboard with a checklist of "Recommended next steps" (add classes, invite teachers, import students) — I should reach a usable system without contacting anyone.
- As a **Teacher**, I open the mobile app each morning and mark attendance for my assigned section in under 60 seconds, offline-tolerant if connectivity drops (queues and syncs).
- As a **Parent**, I get a push/WhatsApp notification the moment my child is marked absent, and I can see the fee due date without calling the office.
- As an **Admin**, I can turn off "Library" and "Hostel" during setup because my coaching institute doesn't need them, and those modules disappear entirely from every role's UI — with no leftover "grayed out" clutter.
- As a **Support agent**, I can see, per tenant, which modules are enabled, plan tier, and recent errors, without needing DB access, to answer "why isn't X working" tickets quickly.

## 6. Localization requirements (international-readiness)
- All user-facing strings externalized (i18n framework) from day one, even if only English + Hindi ship in MVP.
- Currency, date format, timezone, first-day-of-week, and grading scheme are tenant-level configuration, not hardcoded.
- Academic calendar structure (term/semester/trimester) is configurable per tenant, not assumed to be the Indian April–March year.
