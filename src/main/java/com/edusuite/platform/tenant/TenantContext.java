package com.edusuite.platform.tenant;

import java.util.UUID;

/**
 * Carries the current request's tenant id for the lifetime of the thread handling it.
 *
 * SECURITY NOTE: the only code allowed to call {@link #set(UUID)} is
 * {@link TenantIdentifierFilter}, and it must populate the value from the
 * authenticated JWT's "tenant_id" claim — never from a client-supplied
 * header, query param, or request body field. If you ever see tenant_id
 * being read from request input anywhere else in the codebase, that is a
 * tenant-spoofing vulnerability and must be fixed before merge.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    static void set(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID get() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("""
                    No tenant set on the current thread. Every authenticated request must pass \
                    through TenantIdentifierFilter before reaching persistence code. If this is a \
                    background job / scheduled task, it must explicitly call TenantContext.runAs(...) \
                    with an explicit tenant id — there is no such thing as a tenant-less DB operation \
                    in this system.""");
        }
        return tenantId;
    }

    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }

    static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * For background jobs / migrations / platform-admin operations that legitimately need to act
     * on behalf of a specific tenant outside of an HTTP request. Never use this to "work around"
     * a missing tenant context in request-handling code — fix the filter chain instead.
     */
    public static <T> T runAs(UUID tenantId, java.util.function.Supplier<T> action) {
        UUID previous = CURRENT_TENANT.get();
        CURRENT_TENANT.set(tenantId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT_TENANT.remove();
            } else {
                CURRENT_TENANT.set(previous);
            }
        }
    }
}
