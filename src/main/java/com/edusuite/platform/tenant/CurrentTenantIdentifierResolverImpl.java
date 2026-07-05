package com.edusuite.platform.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Part of Hibernate's official multi-tenancy SPI (paired with
 * {@link TenantAwareConnectionProvider}). Hibernate calls this whenever it needs to know which
 * tenant the current Session belongs to — most importantly, right before it asks the
 * ConnectionProvider for a connection.
 *
 * The tenant identifier we hand back is a String (Hibernate's SPI is generic but Spring Boot's
 * auto-detection here uses String most reliably) representing the tenant's UUID. See
 * {@link TenantAwareConnectionProvider} for where this value is actually applied to the DB
 * session via `SET LOCAL` / set_config.
 */
@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

    /**
     * Used for operations that are legitimately tenant-less at the Hibernate session level
     * (e.g. reading the platform-level Tenant table itself, which has no RLS policy and no
     * tenant_id column). RLS policies never reference this value — an unset/placeholder tenant
     * simply never matches any tenant-scoped row, which is the fail-closed behavior we want.
     */
    public static final String NO_TENANT = "__no_tenant__";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.isSet() ? TenantContext.get().toString() : NO_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // Required by the SPI: prevents Hibernate from reusing a Session across two different
        // tenant contexts without noticing. Keep this true.
        return true;
    }
}
