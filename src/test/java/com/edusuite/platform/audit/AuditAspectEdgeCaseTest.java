package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantContext;
import com.edusuite.platform.tenant.TenantScopedEntity;
import tools.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuditAspectEdgeCaseTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditAspect auditAspect = new AuditAspect(auditLogRepository, objectMapper);

    @BeforeEach
    void setUp() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditSwallowsPersistenceExceptionAndReturnsResult() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint("customAction");
        Audited audited = mockAudited("customAction", "", SpelAuditedEntityExtractor.class);
        Object result = new DummyResult("value");
        when(joinPoint.proceed()).thenReturn(result);
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("DB down"));

        Object returned = auditAspect.audit(joinPoint, audited);

        assertThat(returned).isSameAs(result);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void nullEntityProducesNullSnapshot() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint("nullAction");
        Audited audited = mockAudited("nullAction", "", SpelAuditedEntityExtractor.class);
        when(joinPoint.proceed()).thenReturn(null);

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getAfterJson()).isEqualTo("null");
        assertThat(captor.getValue().getEntityType()).isNull();
    }

    @Test
    void requestAttributesProvideClientIpAndUserAgent() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.addHeader("User-Agent", "TestAgent/1.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ProceedingJoinPoint joinPoint = mockJoinPoint("requestAction");
        Audited audited = mockAudited("requestAction", "", SpelAuditedEntityExtractor.class);
        when(joinPoint.proceed()).thenReturn(new DummyResult("x"));

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getClientIp()).isEqualTo("192.168.1.1");
        assertThat(captor.getValue().getUserAgent()).isEqualTo("TestAgent/1.0");
    }

    @Test
    void xForwardedForHeaderOverridesRemoteAddr() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ProceedingJoinPoint joinPoint = mockJoinPoint("forwardedAction");
        Audited audited = mockAudited("forwardedAction", "", SpelAuditedEntityExtractor.class);
        when(joinPoint.proceed()).thenReturn(new DummyResult("x"));

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getClientIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void authenticatedActorIsResolvedFromSecurityContext() throws Throwable {
        UserDetails user = User.withUsername("teacher@school.test").password("n/a").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        ProceedingJoinPoint joinPoint = mockJoinPoint("authAction");
        Audited audited = mockAudited("authAction", "", SpelAuditedEntityExtractor.class);
        when(joinPoint.proceed()).thenReturn(new DummyResult("x"));

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActor()).isEqualTo("teacher@school.test");
    }

    @Test
    void anonymousActorWhenNoAuthentication() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint("anonAction");
        Audited audited = mockAudited("anonAction", "", SpelAuditedEntityExtractor.class);
        when(joinPoint.proceed()).thenReturn(new DummyResult("x"));

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActor()).isEqualTo("anonymous");
    }

    @Test
    void customExtractorProvidesEntity() throws Throwable {
        CustomResult custom = new CustomResult("custom-id", "custom-value");
        DefaultResult defaulted = new DefaultResult("default-id", "default-value");

        ProceedingJoinPoint joinPoint = mockJoinPoint("customAction");
        Audited audited = mockAudited("customAction", "", SwitchingExtractor.class);
        when(joinPoint.proceed()).thenReturn(defaulted);

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getEntityId()).isEqualTo("custom-id");
        assertThat(captor.getValue().getAfterJson()).contains("custom-value");
    }

    @Test
    void tenantScopedEntityUsesEntityTenantId() throws Throwable {
        UUID tenantId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        TenantScopedResult entity = new TenantScopedResult(tenantId, "value");

        ProceedingJoinPoint joinPoint = mockJoinPoint("tenantAction");
        Audited audited = mockAudited("tenantAction", "", SpelAuditedEntityExtractor.class);
        when(joinPoint.proceed()).thenReturn(entity);

        auditAspect.audit(joinPoint, audited);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo("tenantAction");
        assertThat(captor.getValue().getAfterJson()).contains("value");
    }

    private ProceedingJoinPoint mockJoinPoint(String methodName) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getSignature()).thenReturn(signature);
        return joinPoint;
    }

    @SuppressWarnings("unchecked")
    private Audited mockAudited(String action, String entityType, Class<? extends AuditedEntityExtractor> extractor) {
        Audited audited = mock(Audited.class);
        when(audited.action()).thenReturn(action);
        when(audited.entityType()).thenReturn(entityType);
        when(audited.extractor()).thenReturn((Class) extractor);
        return audited;
    }

    public static class DummyResult {
        private final String value;

        public DummyResult(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class DefaultResult {
        private final String id;
        private final String value;

        public DefaultResult(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public static class CustomResult {
        private final String id;
        private final String value;

        public CustomResult(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SwitchingExtractor implements AuditedEntityExtractor {
        @Override
        public Object extractEntity(ProceedingJoinPoint joinPoint, Object result) {
            return new CustomResult("custom-id", "custom-value");
        }
    }

    public static class TenantScopedResult extends TenantScopedEntity {
        private final String value;

        public TenantScopedResult(UUID tenantId, String value) {
            this.value = value;
            TenantContext.runAs(tenantId, () -> {
                assignTenantOnCreate();
                return null;
            });
        }

        public String getValue() {
            return value;
        }
    }
}
