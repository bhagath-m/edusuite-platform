package com.edusuite.platform.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Enables AspectJ-style Spring AOP proxying so that {@link AuditAspect} is applied
 * to methods annotated with {@link Audited}.
 */
@Configuration
@EnableAspectJAutoProxy
public class AuditConfig {
}
