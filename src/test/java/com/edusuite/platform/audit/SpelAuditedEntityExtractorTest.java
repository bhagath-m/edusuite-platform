package com.edusuite.platform.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpelAuditedEntityExtractorTest {

    private final SpelAuditedEntityExtractor extractor = new SpelAuditedEntityExtractor();

    @Test
    void extractEntityReturnsResultUnchanged() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object result = new Object();

        assertThat(extractor.extractEntity(joinPoint, result)).isSameAs(result);
    }

    @Test
    void extractEntityReturnsNullResult() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        assertThat(extractor.extractEntity(joinPoint, null)).isNull();
    }
}
