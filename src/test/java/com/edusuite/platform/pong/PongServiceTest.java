package com.edusuite.platform.pong;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PongServiceTest {

    private final PongService pongService = new PongService();

    @Test
    void recordPongStoresCorrelationId() {
        pongService.recordPong("corr-1");

        assertThat(pongService.wasReceived("corr-1")).isTrue();
        assertThat(pongService.getReceivedCorrelationIds()).containsExactly("corr-1");
    }

    @Test
    void recordPongIgnoresNull() {
        pongService.recordPong(null);

        assertThat(pongService.getReceivedCorrelationIds()).isEmpty();
    }

    @Test
    void getReceivedCorrelationIdsReturnsInInsertionOrder() {
        pongService.recordPong("corr-1");
        pongService.recordPong("corr-2");
        pongService.recordPong("corr-3");

        List<String> ids = pongService.getReceivedCorrelationIds();

        assertThat(ids).containsExactly("corr-1", "corr-2", "corr-3");
    }

    @Test
    void wasReceivedReturnsFalseForUnknownId() {
        assertThat(pongService.wasReceived("unknown")).isFalse();
    }
}
