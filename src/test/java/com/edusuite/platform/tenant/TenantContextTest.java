package com.edusuite.platform.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantContextTest {

    private static final UUID TENANT_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TENANT_B = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getThrowsWhenNoTenantIsSet() {
        assertThat(TenantContext.isSet()).isFalse();
        assertThatThrownBy(TenantContext::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No tenant set on the current thread");
    }

    @Test
    void setAndGetRoundTrip() {
        TenantContext.set(TENANT_A);

        assertThat(TenantContext.isSet()).isTrue();
        assertThat(TenantContext.get()).isEqualTo(TENANT_A);
    }

    @Test
    void clearRemovesTenant() {
        TenantContext.set(TENANT_A);
        TenantContext.clear();

        assertThat(TenantContext.isSet()).isFalse();
        assertThatThrownBy(TenantContext::get)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void runAsExecutesActionWithTenantAndCleansUp() {
        String result = TenantContext.runAs(TENANT_A, () -> {
            assertThat(TenantContext.get()).isEqualTo(TENANT_A);
            return "done";
        });

        assertThat(result).isEqualTo("done");
        assertThat(TenantContext.isSet()).isFalse();
    }

    @Test
    void runAsRestoresPreviousTenant() {
        TenantContext.set(TENANT_A);

        TenantContext.runAs(TENANT_B, () -> {
            assertThat(TenantContext.get()).isEqualTo(TENANT_B);
            return null;
        });

        assertThat(TenantContext.get()).isEqualTo(TENANT_A);
    }

    @Test
    void nestedRunAsRestoresOuterTenant() {
        TenantContext.set(TENANT_A);

        TenantContext.runAs(TENANT_B, () -> {
            assertThat(TenantContext.get()).isEqualTo(TENANT_B);
            TenantContext.runAs(TENANT_A, () -> {
                assertThat(TenantContext.get()).isEqualTo(TENANT_A);
                return null;
            });
            assertThat(TenantContext.get()).isEqualTo(TENANT_B);
            return null;
        });

        assertThat(TenantContext.get()).isEqualTo(TENANT_A);
    }

    @Test
    void runAsCleansUpEvenWithNullPrevious() {
        assertThat(TenantContext.isSet()).isFalse();

        TenantContext.runAs(TENANT_A, () -> {
            assertThat(TenantContext.get()).isEqualTo(TENANT_A);
            return null;
        });

        assertThat(TenantContext.isSet()).isFalse();
    }
}
