package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Test-only tenant-scoped entity used to verify {@link AuditAspect} behavior.
 */
@Entity
@Table(name = "dummy_audited_entity")
public class DummyAuditedEntity extends TenantScopedEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    protected DummyAuditedEntity() {
    }

    public DummyAuditedEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
