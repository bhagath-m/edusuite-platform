package com.edusuite.platform.ping;

import com.edusuite.platform.ping.internal.PingEventPublisher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PingServiceTest {

    private final PingEventPublisher publisher = mock(PingEventPublisher.class);
    private final PingService pingService = new PingService(publisher);

    @Test
    void pingReturnsNonNullCorrelationId() {
        String correlationId = pingService.ping();

        assertThat(correlationId).isNotNull();
        assertThat(correlationId).isNotBlank();
    }

    @Test
    void pingRequestsPongWithCorrelationId() {
        String correlationId = pingService.ping();

        verify(publisher).requestPong(correlationId);
    }
}
