package com.edusuite.platform.audit;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Default {@link AuditedEntityExtractor} that audits the method's return value.
 *
 * <p>This implementation is intentionally simple: it makes the common case (a service method
 * returning the entity it mutated) work out of the box. More advanced extraction - for example
 * selecting an argument by index or evaluating a SpEL expression - can be provided by a custom
 * extractor referenced from the {@link Audited#extractor()} attribute.</p>
 */
public class SpelAuditedEntityExtractor implements AuditedEntityExtractor {

    @Override
    public Object extractEntity(ProceedingJoinPoint joinPoint, Object result) {
        return result;
    }
}
