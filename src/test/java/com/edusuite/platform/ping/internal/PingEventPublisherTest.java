package com.edusuite.platform.ping.internal;

import com.edusuite.platform.ping.event.PongRequestedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PingEventPublisherTest {

    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final PingEventPublisher publisher = new PingEventPublisher(applicationEventPublisher);

    @Test
    void requestPongPublishesPongRequestedEvent() {
        String correlationId = "corr-123";

        publisher.requestPong(correlationId);

        ArgumentCaptor<PongRequestedEvent> captor = ArgumentCaptor.forClass(PongRequestedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().correlationId()).isEqualTo(correlationId);
    }
}
