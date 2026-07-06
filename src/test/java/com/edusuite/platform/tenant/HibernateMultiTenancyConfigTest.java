package com.edusuite.platform.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HibernateMultiTenancyConfigTest {

    @Test
    void customizeRegistersConnectionProviderAndResolver() {
        TenantAwareConnectionProvider connectionProvider = mock(TenantAwareConnectionProvider.class);
        CurrentTenantIdentifierResolverImpl resolver = mock(CurrentTenantIdentifierResolverImpl.class);
        HibernateMultiTenancyConfig config = new HibernateMultiTenancyConfig(connectionProvider, resolver);
        Map<String, Object> properties = new HashMap<>();

        config.customize(properties);

        assertThat(properties)
                .containsEntry(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider)
                .containsEntry(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
