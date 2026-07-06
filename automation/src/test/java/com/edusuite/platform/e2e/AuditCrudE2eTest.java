package com.edusuite.platform.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditCrudE2eTest extends E2eBaseTest {

    @Test
    void createdAuditLogAppearsInList() {
        String token = obtainAccessToken("admin@tenant-a.test", "password123");

        String action = "CREATE";
        String entityType = "Course";
        String entityId = "course-123";
        String afterJson = "{\"name\":\"Intro to Testing\"}";

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "action", action,
                        "entityType", entityType,
                        "entityId", entityId,
                        "afterJson", afterJson
                ))
                .when()
                .post("/api/v1/audit")
                .then()
                .statusCode(200);

        List<Map<String, Object>> auditLogs = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/audit")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".");

        assertThat(auditLogs)
                .anyMatch(log -> action.equals(log.get("action"))
                        && entityType.equals(log.get("entityType"))
                        && entityId.equals(log.get("entityId"))
                        && afterJson.equals(log.get("afterJson")));
    }
}
