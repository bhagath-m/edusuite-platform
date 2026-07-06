package com.edusuite.platform.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentTenantIdentifierResolverImplTest {

    private final CurrentTenantIdentifierResolverImpl resolver = new CurrentTenantIdentifierResolverImpl();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void resolveReturnsNoTenantWhenContextIsUnset() {
        assertThat(resolver.resolveCurrentTenantIdentifier())
                .isEqualTo(CurrentTenantIdentifierResolverImpl.NO_TENANT);
    }

    @Test
    void resolveReturnsTenantIdStringWhenContextIsSet() {
        UUID tenantId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        TenantContext.set(tenantId);

        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo(tenantId.toString());
    }

    @Test
    void validateExistingCurrentSessionsReturnsTrue() {
        assertThat(resolver.validateExistingCurrentSessions()).isTrue();
    }
}
