package com.edusuite.platform;

import com.edusuite.platform.ping.event.PongRequestedEvent;
import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.EventType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Modulith structure and event wiring tests.
 *
 * Verifies that the application's module structure is valid and that the
 * {@code ping} module publishes {@link PongRequestedEvent} for the {@code pong}
 * module to listen to.
 */
class ModulithStructureTest {

    private final ApplicationModules modules = ApplicationModules.of(EduSuitePlatformApplication.class);

    @Test
    void modulithStructureIsValid() {
        modules.verify();
    }

    @Test
    void pingModulePublishesPongRequestedEvent() {
        ApplicationModule ping = modules.getModuleByName("ping").orElseThrow();

        assertThat(ping.getPublishedEvents())
                .extracting(EventType::getType)
                .extracting(JavaClass::getName)
                .contains(PongRequestedEvent.class.getName());
    }

    @Test
    void pongModuleListensToPongRequestedEvent() {
        ApplicationModule pong = modules.getModuleByName("pong").orElseThrow();

        assertThat(pong.getEventsListenedTo(modules))
                .extracting(JavaClass::getName)
                .contains(PongRequestedEvent.class.getName());
    }
}
