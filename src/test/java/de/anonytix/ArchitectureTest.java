package de.anonytix;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTest {

    @Test
    void verifiesSpringModulithBoundaries() {
        ApplicationModules.of(AnonytixApplication.class).verify();
    }
}
