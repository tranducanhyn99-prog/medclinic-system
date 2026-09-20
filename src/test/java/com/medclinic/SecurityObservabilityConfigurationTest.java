package com.medclinic;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityObservabilityConfigurationTest {

    @Test
    void productionSecurityAndObservabilityDefaultsRemainHardened() throws Exception {
        String config = Files.readString(Path.of("src", "main", "resources", "application.yml"));
        String compose = Files.readString(Path.of("compose.yml"));

        assertTrue(config.contains("include-message: never"));
        assertTrue(config.contains("include-binding-errors: never"));
        assertTrue(config.contains("include-stacktrace: never"));
        assertTrue(config.contains("http-only: true"));
        assertTrue(config.contains("same-site: lax"));
        assertTrue(config.contains("secure: ${SESSION_COOKIE_SECURE:true}"));
        assertTrue(config.contains("include: health,metrics"));
        assertTrue(config.contains("show-details: never"));
        assertTrue(compose.contains("SESSION_COOKIE_SECURE: ${SESSION_COOKIE_SECURE:-false}"));
    }
}
