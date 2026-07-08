package com.edusuite.platform.pong.internal;

import com.edusuite.platform.ping.event.PongRequestedEvent;
import com.edusuite.platform.pong.PongService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Internal listener that reacts to {@link PongRequestedEvent} from the {@code ping} module.
 */
@Component
public class PongEventListener {

    private final PongService pongService;

    public PongEventListener(PongService pongService) {
        this.pongService = pongService;
    }

    @EventListener
    public void onPongRequested(PongRequestedEvent event) {
        pongService.recordPong(event.correlationId());
    }
}
