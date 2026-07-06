package com.edusuite.platform.ping;

import com.edusuite.platform.ping.internal.PingEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Public API of the {@code ping} module.
 */
@Service
public class PingService {

    private final PingEventPublisher publisher;

    public PingService(PingEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Emits a ping and requests a pong response.
     *
     * @return the correlation id that the {@code pong} module can use to match the response
     */
    public String ping() {
        String correlationId = UUID.randomUUID().toString();
        publisher.requestPong(correlationId);
        return correlationId;
    }
}
