package com.edusuite.platform.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * THIS CLASS IS THE ENFORCEMENT POINT FOR TENANT ISOLATION AT THE DATABASE LAYER.
 * Treat any change here as a security-sensitive change requiring careful review and re-running
 * the full TenantIsolationIT suite before merge.
 *
 * How it works:
 *  1. Hibernate asks {@link CurrentTenantIdentifierResolverImpl} which tenant is current.
 *  2. Hibernate calls {@link #getConnection(String)} with that identifier whenever a Session
 *     needs a physical connection.
 *  3. We obtain a connection from the pool and, as the FIRST statement run on it, execute
 *         SELECT set_config('app.current_tenant', ?, true)
 *     The third argument (`true`) makes this transaction-scoped ("SET LOCAL" semantics): it
 *     applies to the transaction that this first statement implicitly starts, and is
 *     automatically cleared on commit/rollback. This means a connection handed back to the
 *     pool can never "leak" a stale tenant setting into the next transaction that borrows it.
 *  4. Postgres RLS policies (db/migration V3+) read this value via
 *         current_setting('app.current_tenant', true)
 *     and restrict every row to tenant_id = that value. If the setting was never applied
 *     (e.g. NO_TENANT sentinel, or a bug), current_setting returns NULL/empty and every RLS
 *     policy denies all rows — fail-closed, not fail-open.
 *
 * This is the well-established pattern for combining Postgres RLS with a connection-pooled,
 * discriminator-column multi-tenant application (as opposed to schema-per-tenant, which would
 * use Hibernate's schema-switching multi-tenancy mode instead).
 */
@Component
public class TenantAwareConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public TenantAwareConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        // Used by Hibernate for tenant-independent operations (e.g. schema validation at
        // startup). Deliberately does NOT set a tenant — must only be used against
        // non-tenant-scoped tables (Tenant itself, Flyway's own tables, etc).
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = dataSource.getConnection();
        applyTenantSetting(connection, tenantIdentifier);
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        // Defense in depth: explicitly clear the setting before the connection goes back to the
        // pool, in case it was ever used outside a clean transaction boundary (autocommit edge
        // cases). SET LOCAL should already have reset at commit, so this is a no-op in the
        // normal path.
        try (connection; PreparedStatement ps = connection.prepareStatement(
                "SELECT set_config('app.current_tenant', '', false)")) {
            ps.execute();
        }
    }

    private void applyTenantSetting(Connection connection, String tenantIdentifier) throws SQLException {
        String value = CurrentTenantIdentifierResolverImpl.NO_TENANT.equals(tenantIdentifier) ? "" : tenantIdentifier;
        try (PreparedStatement ps = connection.prepareStatement("SELECT set_config('app.current_tenant', ?, true)")) {
            ps.setString(1, value);
            ps.execute();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("TenantAwareConnectionProvider does not support unwrapping");
    }
}
