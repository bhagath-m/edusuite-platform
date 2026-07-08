package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantContext;
import com.edusuite.platform.tenant.TenantScopedEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Records an {@link AuditLog} for every method annotated with {@link Audited}.
 *
 * <p>The aspect captures the current tenant, actor, client IP and user-agent from the request,
 * snapshots the returned entity as JSON, and persists the audit row after the method succeeds.
 * It does not intercept exceptions - failures in the target method are propagated unchanged.</p>
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AuditAspect.class);
    private static final String EMPTY_SNAPSHOT = "{}";
    private static final String NULL_SNAPSHOT = "null";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(audited)")
    @SuppressWarnings({"IllegalThrows", "IllegalCatch", "PMD.AvoidCatchingGenericException"})
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            recordAudit(joinPoint, audited, result);
        } catch (Exception ex) {
            // Audit logging must never break business logic. Swallow and continue so that a
            // transient JSON or persistence failure does not undo a successful operation.
            // A production system would forward this to a structured error reporter.
            if (LOG.isWarnEnabled()) {
                LOG.warn("Audit logging failed for action {} on {}", audited.action(), joinPoint.getSignature(), ex);
            }
        }

        return result;
    }

    private void recordAudit(ProceedingJoinPoint joinPoint, Audited audited, Object result) {
        Object entity = extractEntity(joinPoint, audited, result);

        String entityType = resolveEntityType(audited, entity);
        String entityId = resolveEntityId(entity);
        String afterJson = snapshotEntity(entity);

        AuditLog log = new AuditLog(
                resolveActor(),
                audited.action(),
                entityType,
                entityId,
                EMPTY_SNAPSHOT,
                afterJson,
                resolveClientIp(),
                resolveUserAgent()
        );

        // AuditLog is tenant-scoped and must be persisted with an explicit tenant context,
        // even if the audited method has already cleared its own runAs context.
        UUID tenantId = resolveTenantId(entity);
        if (tenantId != null) {
            TenantContext.runAs(tenantId, () -> {
                auditLogRepository.save(log);
                return null;
            });
        } else {
            auditLogRepository.save(log);
        }
    }

    private UUID resolveTenantId(Object entity) {
        if (entity instanceof TenantScopedEntity tenantEntity) {
            return tenantEntity.getTenantId();
        }
        if (TenantContext.isSet()) {
            return TenantContext.get();
        }
        return null;
    }

    private Object extractEntity(ProceedingJoinPoint joinPoint, Audited audited, Object result) {
        try {
            AuditedEntityExtractor extractor = audited.extractor()
                    .getDeclaredConstructor()
                    .newInstance();
            return extractor.extractEntity(joinPoint, result);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                 | NoSuchMethodException ex) {
            // Fall back to auditing the raw return value if a custom extractor cannot be built.
            return result;
        }
    }

    private String resolveEntityType(Audited audited, Object entity) {
        if (!audited.entityType().isBlank()) {
            return audited.entityType();
        }
        if (entity != null) {
            return entity.getClass().getSimpleName();
        }
        return null;
    }

    private String resolveEntityId(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            Method getId = findGetIdMethod(entity.getClass());
            if (getId == null) {
                return null;
            }
            Object id = getId.invoke(entity);
            return id != null ? Objects.toString(id) : null;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return null;
        }
    }

    private Method findGetIdMethod(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod("getId");
            } catch (NoSuchMethodException ex) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private String snapshotEntity(Object entity) {
        if (entity == null) {
            return NULL_SNAPSHOT;
        }
        try {
            Map<String, Object> map = objectMapper.convertValue(entity, new TypeReference<>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (JacksonException ex) {
            return NULL_SNAPSHOT;
        }
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "anonymous";
        }
        String name = authentication.getName();
        return (name == null || name.isBlank()) ? "anonymous" : name;
    }

    private String resolveClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserAgent() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
