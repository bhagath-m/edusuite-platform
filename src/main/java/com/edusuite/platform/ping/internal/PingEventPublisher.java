package com.edusuite.platform.ping.internal;

import com.edusuite.platform.ping.event.PongRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Internal gateway that publishes {@link PongRequestedEvent} instances using
 * plain Spring application events.
 */
@Component
public class PingEventPublisher {

    private final ApplicationEventPublisher publisher;

    public PingEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void requestPong(String correlationId) {
        publisher.publishEvent(new PongRequestedEvent(correlationId));
    }
}
