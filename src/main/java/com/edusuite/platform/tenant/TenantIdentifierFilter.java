package com.edusuite.platform.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Populates {@link TenantContext} for the duration of the request from the "tenant_id" claim
 * on the authenticated JWT (see keycloak/realm-export.json for how that claim is issued).
 *
 * This filter MUST run after Spring Security's bearer-token authentication filter (so that
 * SecurityContextHolder already has the authenticated Jwt), and MUST run before any controller
 * or persistence code executes. It is registered explicitly in SecurityConfig via
 * addFilterAfter(..., BearerTokenAuthenticationFilter.class) — do not rely on component
 * scanning + @Order for this, ordering here is a security property, not a convenience.
 *
 * Public/unauthenticated endpoints (health checks, trial signup, login) will have no JWT and
 * therefore no tenant — that is expected. Do not "fall back" to a default tenant here; any code
 * path needing a tenant on an unauthenticated endpoint has a design problem, not a filter problem.
 */
public class TenantIdentifierFilter extends OncePerRequestFilter {

    public static final String TENANT_CLAIM = "tenant_id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            extractTenantFromJwt().ifPresent(TenantContext::set);
            filterChain.doFilter(request, response);
        } finally {
            // Always clear: thread pools reuse threads, and a leftover tenant id from a previous
            // request on this thread would be a cross-tenant data leak waiting to happen.
            TenantContext.clear();
        }
    }

    private java.util.Optional<UUID> extractTenantFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return java.util.Optional.empty();
        }
        String tenantIdClaim = jwt.getClaimAsString(TENANT_CLAIM);
        if (tenantIdClaim == null || tenantIdClaim.isBlank()) {
            // An authenticated user with no tenant claim is a configuration error for any
            // tenant-scoped user (platform super-admins are the one legitimate exception —
            // handle those via a separate, explicitly-audited admin API, not by silently
            // allowing tenant-less access on normal endpoints).
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(UUID.fromString(tenantIdClaim));
    }
}
