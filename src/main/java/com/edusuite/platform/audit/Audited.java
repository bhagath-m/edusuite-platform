package com.edusuite.platform.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as audited. When a method carrying this annotation is invoked through a Spring
 * managed bean, {@link AuditAspect} records an {@link AuditLog} row describing the actor, action,
 * entity, and before/after state.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {

    /**
     * The human-readable action being performed, e.g. "CREATE_ACADEMIC_YEAR" or "UPDATE_FEE".
     */
    String action();

    /**
     * Optional override for the entity type. If left blank, the simple class name of the audited
     * entity is used.
     */
    String entityType() default "";

    /**
     * Strategy for extracting the audited entity from the join point and return value.
     * Defaults to returning the method's return value.
     */
    Class<? extends AuditedEntityExtractor> extractor() default SpelAuditedEntityExtractor.class;
}
