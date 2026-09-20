package com.medclinic.security;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityAuditServiceTest {

    @Test
    void recordsAuthenticationAndRateLimitMetricsWithoutPrincipalTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SecurityAuditService service = new SecurityAuditService(registry);

        service.authenticationSuccess("doctor", "127.0.0.1");
        service.authenticationFailure("doctor", "127.0.0.1", "BadCredentialsException");
        service.logout("doctor", "127.0.0.1");
        service.rateLimitRejected("/login", "127.0.0.1");

        assertEquals(1.0, registry.get("medclinic.security.authentication")
                .tag("outcome", "success").counter().count());
        assertEquals(1.0, registry.get("medclinic.security.authentication")
                .tag("outcome", "failure").counter().count());
        assertEquals(1.0, registry.get("medclinic.security.authentication")
                .tag("outcome", "logout").counter().count());
        assertEquals(1.0, registry.get("medclinic.security.rate_limit.rejected")
                .tag("endpoint", "/login").counter().count());
    }
}
