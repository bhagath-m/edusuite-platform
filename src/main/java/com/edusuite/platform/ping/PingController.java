package com.edusuite.platform.ping;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public endpoint that emits a ping and returns its correlation id.
 */
@RestController
@RequestMapping("/api/v1/public")
public class PingController {

    private final PingService pingService;

    public PingController(PingService pingService) {
        this.pingService = pingService;
    }

    @PostMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        String correlationId = pingService.ping();
        return ResponseEntity.ok(Map.of("correlationId", correlationId));
    }
}
