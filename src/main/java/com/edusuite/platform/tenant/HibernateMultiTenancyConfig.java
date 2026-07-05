package com.edusuite.platform.tenant;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class HibernateMultiTenancyConfig implements HibernatePropertiesCustomizer {

    private final TenantAwareConnectionProvider connectionProvider;
    private final CurrentTenantIdentifierResolverImpl tenantIdentifierResolver;

    public HibernateMultiTenancyConfig(TenantAwareConnectionProvider connectionProvider,
                                        CurrentTenantIdentifierResolverImpl tenantIdentifierResolver) {
        this.connectionProvider = connectionProvider;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        hibernateProperties.put(org.hibernate.cfg.AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
        // NOTE FOR WHOEVER BUILDS THIS FIRST: property constant names / required companion
        // settings occasionally shift between Hibernate minor versions. Spring Boot 3.3.4 ships
        // Hibernate 6.5.x. If the build fails on these constants, check
        // org.hibernate.cfg.AvailableSettings in that exact Hibernate version for the current
        // names (they have historically been MULTI_TENANT_CONNECTION_PROVIDER and
        // MULTI_TENANT_IDENTIFIER_RESOLVER, stable since Hibernate 5). This is the one piece of
        // this starter that could not be verified against a live build in this environment
        // (no Maven Central access), so treat it as the first thing to confirm compiles cleanly.
    }
}
