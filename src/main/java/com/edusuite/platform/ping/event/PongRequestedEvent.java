package com.edusuite.platform.ping.event;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * Domain event published by the {@code ping} module to request a {@code pong} response.
 */
@DomainEvent
public class PongRequestedEvent {

    private final String correlationId;

    public PongRequestedEvent(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
