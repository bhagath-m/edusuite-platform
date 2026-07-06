package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Immutable-style record of a state-changing action taken against a tenant-scoped entity.
 *
 * <p>Created automatically by {@link AuditAspect} around methods annotated with {@link Audited}.
 * The tenant id is populated from {@link com.edusuite.platform.tenant.TenantContext} by the
 * inherited {@link TenantScopedEntity} machinery, never from client input.</p>
 */
@Entity
@Table(name = "audit_log")
public class AuditLog extends TenantScopedEntity {

    @Column(name = "actor", nullable = false, length = 255)
    private String actor;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 255)
    private String entityId;

    @Column(name = "before_json", columnDefinition = "TEXT")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "TEXT")
    private String afterJson;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    protected AuditLog() {
    }

    public AuditLog(String actor, String action, String entityType, String entityId,
                    String beforeJson, String afterJson, String clientIp, String userAgent) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.beforeJson = beforeJson;
        this.afterJson = afterJson;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    @PrePersist
    void prePersist() {
        if (this.occurredAt == null) {
            this.occurredAt = Instant.now();
        }
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getBeforeJson() {
        return beforeJson;
    }

    public void setBeforeJson(String beforeJson) {
        this.beforeJson = beforeJson;
    }

    public String getAfterJson() {
        return afterJson;
    }

    public void setAfterJson(String afterJson) {
        this.afterJson = afterJson;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
