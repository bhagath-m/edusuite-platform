# 04 — Data Model (Core Entities)

This describes the core entity shape, not a full DDL. An implementing agent should generate migrations (Flyway, versioned SQL) from this shape, adding module-specific tables as each module is built.

## 1. Platform / tenancy entities
- **Tenant**: id, name, subdomain (e.g., `abcinstitute.edusuite.app`), institute_type (school/college/coaching), plan_tier, billing_status, created_at, locale, currency, timezone, academic_year_start_month.
- **TenantModuleSubscription**: tenant_id, module_key, enabled, source (tier|addon|trial), enabled_at.
- **User**: id, tenant_id (nullable for platform super-admins), keycloak_subject_id, email/phone, status. Auth/credentials live in Keycloak, not in this table — this table holds app-level profile + role linkage.
- **Role / UserRole**: standard RBAC — Role(id, tenant_id, name, is_system_role), UserRole(user_id, role_id), Permission(role_id, permission_key).
- **AuditLogEntry**: id, tenant_id, actor_user_id, action, entity_type, entity_id, before_state(jsonb), after_state(jsonb), ip, timestamp. Written by an aspect/interceptor, not manually in each service — see doc 06.

## 2. Party / Person (generic, reusable — see architecture doc §5)
- **Person**: id, tenant_id, first_name, last_name, dob, gender, contact_info (phone/email), address (jsonb for locale flexibility), photo_url.
- **PersonRelationship**: person_id, related_person_id, relationship_type (guardian_of, sibling_of, emergency_contact_of), is_primary.
- Student, Staff, Parent are **role records that reference a Person**, not separate copies of personal data — avoids duplicate/out-of-sync personal info when one human plays multiple roles (a teacher whose child also studies at the same institute, for example).

## 3. Academic structure (core)
- **Campus** (optional, for multi-branch tenants): id, tenant_id, name, address.
- **AcademicYear**: id, tenant_id, label (e.g., "2026-27"), start_date, end_date, is_current.
- **Course/Grade**: id, tenant_id, name, sequence_order (e.g., Grade 1..12, or a coaching batch name).
- **Section**: id, tenant_id, course_id, academic_year_id, name, capacity.
- **Subject**: id, tenant_id, name, code.
- **SectionSubjectTeacher**: section_id, subject_id, teacher_person_id.

## 4. Module-scoped entities (one representative slice per module — extend per module as built)
- **Student**: person_id, tenant_id, admission_number, current_section_id, admission_date, status (active/alumni/transferred).
- **StaffMember**: person_id, tenant_id, employee_code, designation, department, joining_date.
- **AttendanceRecord**: id, tenant_id, section_id, student_id, date, status (present/absent/late/excused), marked_by_user_id, marked_at.
- **FeeStructure / FeeHead / FeeInstallment**: tenant_id, course_id/section_id scope, amount, due_date rules.
- **FeeTransaction**: id, tenant_id, student_id, installment_id, amount, payment_gateway_ref, status, paid_at.
- **ExamSchedule / ExamResult**: tenant_id, section_id, subject_id, student_id, marks_obtained, max_marks, grade.
- **LibraryItem / LibraryTransaction**, **TransportRoute / TransportAssignment**, **HostelRoom / HostelAllocation** — same pattern: tenant-scoped, referencing Person/Student/Staff, module-owned.

## 5. Tenant isolation implementation detail
- Every table above (except global reference/lookup tables like a static list of Indian states, or platform-level tables like `Tenant` itself) has a **non-nullable `tenant_id`**.
- PostgreSQL RLS policy pattern (representative):
  ```sql
  ALTER TABLE attendance_record ENABLE ROW LEVEL SECURITY;
  CREATE POLICY tenant_isolation ON attendance_record
    USING (tenant_id = current_setting('app.current_tenant')::uuid);
  ```
- Application sets `app.current_tenant` per request/transaction from the authenticated JWT tenant claim — never from a client-supplied parameter, to prevent tenant-spoofing.
- Hibernate-level tenant filter is a second, defense-in-depth layer, not a replacement for RLS.

## 6. Tenant-configurable fields
Some fields genuinely vary by institute (custom admission form fields, additional student attributes a particular school wants to track). For these, prefer a **structured extension via a `custom_fields jsonb` column** on the relevant entity plus a `CustomFieldDefinition` table (tenant_id, entity_type, field_key, field_label, field_type, validation_rules) rather than a fully dynamic EAV schema across the whole system. This keeps 95% of the schema strongly typed and query-able, while giving controlled flexibility exactly where tenants need it (mainly Admissions and Student profile).
