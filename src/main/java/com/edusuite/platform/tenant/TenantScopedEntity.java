package com.edusuite.platform.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Every domain entity that belongs to a tenant (Student, AttendanceRecord, FeeInvoice, etc.)
 * should extend this class rather than redeclaring tenant_id by hand. This gives two things:
 *
 * 1. A single, consistent place where tenant_id is populated (from TenantContext, never from
 *    client input — see the @PrePersist below).
 * 2. A forcing function during code review: if a new entity does NOT extend this class, that
 *    should trigger the question "does this really not belong to a tenant?" (the only
 *    legitimate "no" answers so far are Tenant itself and pure lookup/reference tables like a
 *    static list of Indian states or exam boards).
 *
 * Remember: this column existing and being correct is necessary but not sufficient for
 * isolation — the actual enforcement is the Postgres RLS policy on the underlying table (see
 * db/migration). This field being wrong would still be caught by RLS on read; but keep it
 * correct anyway, since some legitimate queries (aggregate reports, admin tooling) may run with
 * elevated DB roles that bypass RLS intentionally, and for those, correctness of this column is
 * the only remaining safeguard.
 */
@MappedSuperclass
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class TenantScopedEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    protected void assignTenantOnCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.get(); // throws if unset - fail closed, not silently null
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }
}
