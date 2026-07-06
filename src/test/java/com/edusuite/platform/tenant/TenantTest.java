package com.edusuite.platform.tenant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantTest {

    @Test
    void constructorInitializesRequiredFieldsAndDefaults() {
        Tenant tenant = new Tenant("Alpha School", "alpha", "SCHOOL");

        assertThat(tenant.getName()).isEqualTo("Alpha School");
        assertThat(tenant.getSubdomain()).isEqualTo("alpha");
        assertThat(tenant.getInstituteType()).isEqualTo("SCHOOL");
        assertThat(tenant.getPlanTier()).isEqualTo("STARTER");
        assertThat(tenant.getBillingStatus()).isEqualTo("TRIAL");
        assertThat(tenant.getLocale()).isEqualTo("en-IN");
        assertThat(tenant.getCurrency()).isEqualTo("INR");
        assertThat(tenant.getTimezone()).isEqualTo("Asia/Kolkata");
        assertThat(tenant.getCreatedAt()).isNotNull();
        assertThat(tenant.getId()).isNull();
    }

    @Test
    void gettersAndSettersForMutableFields() {
        Tenant tenant = new Tenant("Beta College", "beta", "COLLEGE");

        tenant.setPlanTier("GROWTH");
        tenant.setBillingStatus("ACTIVE");

        assertThat(tenant.getPlanTier()).isEqualTo("GROWTH");
        assertThat(tenant.getBillingStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void protectedConstructorExistsForJpa() {
        Tenant tenant = new Tenant();
        tenant.setPlanTier("SCALE");
        tenant.setBillingStatus("PAST_DUE");

        assertThat(tenant.getPlanTier()).isEqualTo("SCALE");
        assertThat(tenant.getBillingStatus()).isEqualTo("PAST_DUE");
        assertThat(tenant.getCreatedAt()).isNotNull();
        assertThat(tenant.getLocale()).isEqualTo("en-IN");
    }
}
