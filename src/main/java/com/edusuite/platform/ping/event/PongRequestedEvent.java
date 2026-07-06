package com.edusuite.platform.ping.event;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * Domain event published by the {@code ping} module to request a {@code pong} response.
 */
@DomainEvent
public record PongRequestedEvent(String correlationId) {
}
