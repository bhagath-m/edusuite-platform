package com.edusuite.platform.pong;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Public API of the {@code pong} module.
 */
@Service
public class PongService {

    private final ConcurrentLinkedQueue<String> receivedCorrelationIds = new ConcurrentLinkedQueue<>();

    /**
     * Records a correlation id received from a {@code ping} event.
     *
     * @param correlationId the correlation id to record
     */
    public void recordPong(String correlationId) {
        if (correlationId != null) {
            receivedCorrelationIds.add(correlationId);
        }
    }

    /**
     * @return all correlation ids received so far, in insertion order
     */
    public List<String> getReceivedCorrelationIds() {
        return receivedCorrelationIds.stream().collect(Collectors.toList());
    }

    /**
     * @param correlationId the correlation id to check
     * @return {@code true} if the given correlation id has been received
     */
    public boolean wasReceived(String correlationId) {
        return receivedCorrelationIds.contains(correlationId);
    }
}
