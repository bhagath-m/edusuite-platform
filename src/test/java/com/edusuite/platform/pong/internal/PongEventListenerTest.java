package com.edusuite.platform.pong.internal;

import com.edusuite.platform.ping.event.PongRequestedEvent;
import com.edusuite.platform.pong.PongService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PongEventListenerTest {

    private final PongService pongService = mock(PongService.class);
    private final PongEventListener listener = new PongEventListener(pongService);

    @Test
    void onPongRequestedRecordsCorrelationId() {
        String correlationId = "corr-abc";
        PongRequestedEvent event = new PongRequestedEvent(correlationId);

        listener.onPongRequested(event);

        verify(pongService).recordPong(correlationId);
    }
}
