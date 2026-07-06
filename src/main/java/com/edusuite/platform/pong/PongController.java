package com.edusuite.platform.pong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public endpoint that lists correlation ids received from ping events.
 */
@RestController
@RequestMapping("/api/v1/public")
public class PongController {

    private final PongService pongService;

    public PongController(PongService pongService) {
        this.pongService = pongService;
    }

    @GetMapping("/pong")
    public ResponseEntity<List<String>> getReceivedCorrelationIds() {
        return ResponseEntity.ok(pongService.getReceivedCorrelationIds());
    }
}
