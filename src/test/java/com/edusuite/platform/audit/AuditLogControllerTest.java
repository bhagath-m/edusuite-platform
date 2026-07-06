package com.edusuite.platform.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuditLogController.class, excludeAutoConfiguration = OAuth2ResourceServerWebSecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @Test
    void getAuditLogsReturnsList() throws Exception {
        AuditLog log = new AuditLog("actor", "CREATE", "Entity", "1", null, "{}", "127.0.0.1", "test");
        when(auditLogService.findByCurrentTenant()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].actor").value("actor"))
                .andExpect(jsonPath("$[0].action").value("CREATE"));
    }

    @Test
    void getAuditLogsReturnsEmptyList() throws Exception {
        when(auditLogService.findByCurrentTenant()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAuditLogReturnsSavedLog() throws Exception {
        AuditLog saved = new AuditLog("alice", "CREATE", "Entity", "42", null, "{}", null, null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CREATE\",\"entityType\":\"Entity\",\"entityId\":\"42\",\"afterJson\":\"{}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actor").value("alice"))
                .andExpect(jsonPath("$.action").value("CREATE"));
    }
}
