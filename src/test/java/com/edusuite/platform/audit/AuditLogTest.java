package com.edusuite.platform.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogTest {

    @Test
    void constructorAndGetters() {
        AuditLog log = new AuditLog(
                "admin",
                "CREATE",
                "Student",
                "student-1",
                "{}",
                "{\"name\":\"Alice\"}",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        assertThat(log.getActor()).isEqualTo("admin");
        assertThat(log.getAction()).isEqualTo("CREATE");
        assertThat(log.getEntityType()).isEqualTo("Student");
        assertThat(log.getEntityId()).isEqualTo("student-1");
        assertThat(log.getBeforeJson()).isEqualTo("{}");
        assertThat(log.getAfterJson()).isEqualTo("{\"name\":\"Alice\"}");
        assertThat(log.getClientIp()).isEqualTo("127.0.0.1");
        assertThat(log.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(log.getOccurredAt()).isNotNull();
    }

    @Test
    void settersAndGetters() {
        AuditLog log = new AuditLog();
        Instant fixed = Instant.parse("2025-01-01T00:00:00Z");

        log.setActor("user");
        log.setAction("UPDATE");
        log.setEntityType("Course");
        log.setEntityId("course-2");
        log.setBeforeJson("{\"a\":1}");
        log.setAfterJson("{\"a\":2}");
        log.setClientIp("10.0.0.1");
        log.setUserAgent("curl/8.0");
        log.setOccurredAt(fixed);

        assertThat(log.getActor()).isEqualTo("user");
        assertThat(log.getAction()).isEqualTo("UPDATE");
        assertThat(log.getEntityType()).isEqualTo("Course");
        assertThat(log.getEntityId()).isEqualTo("course-2");
        assertThat(log.getBeforeJson()).isEqualTo("{\"a\":1}");
        assertThat(log.getAfterJson()).isEqualTo("{\"a\":2}");
        assertThat(log.getClientIp()).isEqualTo("10.0.0.1");
        assertThat(log.getUserAgent()).isEqualTo("curl/8.0");
        assertThat(log.getOccurredAt()).isEqualTo(fixed);
    }

    @Test
    void prePersistKeepsExistingOccurredAt() {
        AuditLog log = new AuditLog();
        Instant fixed = Instant.parse("2024-06-15T12:00:00Z");
        log.setOccurredAt(fixed);

        log.prePersist();

        assertThat(log.getOccurredAt()).isEqualTo(fixed);
    }
}
