package com.edusuite.platform.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * External representation of an {@link AuditLog} entry returned by the REST API.
 *
 * <p>Decouples the JPA entity from the contract exposed to clients.</p>
 */
public record AuditLogResponse(
        UUID id,
        UUID tenantId,
        String actor,
        String action,
        String entityType,
        String entityId,
        String beforeJson,
        String afterJson,
        String clientIp,
        String userAgent,
        Instant occurredAt
) {

    /**
     * Maps a persisted {@link AuditLog} entity to its API representation.
     *
     * @param auditLog the entity to map; must not be {@code null}
     * @return the corresponding response object
     */
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getTenantId(),
                auditLog.getActor(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getBeforeJson(),
                auditLog.getAfterJson(),
                auditLog.getClientIp(),
                auditLog.getUserAgent(),
                auditLog.getOccurredAt()
        );
    }
}
