package com.edusuite.platform.tenant;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantScopedEntityTest {

    private static final UUID TENANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getIdAndTenantIdDefaultToNull() {
        ConcreteTenantScopedEntity entity = new ConcreteTenantScopedEntity();

        assertThat(entity.getId()).isNull();
        assertThat(entity.getTenantId()).isNull();
    }

    @Test
    void assignTenantOnCreatePopulatesTenantIdWhenContextIsSet() {
        ConcreteTenantScopedEntity entity = TenantContext.runAs(TENANT_ID, () -> {
            ConcreteTenantScopedEntity e = new ConcreteTenantScopedEntity();
            e.assignTenantOnCreate();
            return e;
        });

        assertThat(entity.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void assignTenantOnCreateThrowsWhenContextIsUnset() {
        ConcreteTenantScopedEntity entity = new ConcreteTenantScopedEntity();

        assertThatThrownBy(entity::assignTenantOnCreate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No tenant set on the current thread");
    }

    @Test
    void assignTenantOnCreateIsIdempotent() {
        UUID otherTenant = UUID.fromString("44444444-4444-4444-4444-444444444444");
        ConcreteTenantScopedEntity entity = TenantContext.runAs(TENANT_ID, () -> {
            ConcreteTenantScopedEntity e = new ConcreteTenantScopedEntity();
            e.assignTenantOnCreate();
            return e;
        });

        TenantContext.runAs(otherTenant, () -> {
            entity.assignTenantOnCreate();
            return null;
        });

        assertThat(entity.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Entity
    static class ConcreteTenantScopedEntity extends TenantScopedEntity {
    }
}
