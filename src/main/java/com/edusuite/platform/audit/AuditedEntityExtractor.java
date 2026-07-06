package com.edusuite.platform.audit;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Pluggable strategy for deciding which object represents the audited entity
 * for a method annotated with {@link Audited}.
 *
 * <p>The default implementation, {@link SpelAuditedEntityExtractor}, simply returns
 * the method's return value. Custom extractors can inspect arguments, the join point,
 * or evaluate a SpEL expression to locate the real entity (e.g. when the method returns
 * a DTO or wrapper).</p>
 */
@FunctionalInterface
public interface AuditedEntityExtractor {

    /**
     * Resolve the entity to audit from the join point and its return value.
     *
     * @param joinPoint the intercepted method invocation
     * @param result    the value returned by the target method
     * @return the entity that should be snapshotted and whose id should be recorded
     */
    Object extractEntity(ProceedingJoinPoint joinPoint, Object result);
}
