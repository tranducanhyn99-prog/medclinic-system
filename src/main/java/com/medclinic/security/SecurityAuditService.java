package com.medclinic.security;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditService.class);
    private final MeterRegistry meterRegistry;

    public void authenticationSuccess(String principal, String remoteIp) {
        meterRegistry.counter("medclinic.security.authentication", "outcome", "success").increment();
        LOGGER.info("security_audit event=AUTH_SUCCESS principal={} remote_ip={}",
                sanitize(principal), sanitize(remoteIp));
    }

    public void authenticationFailure(String principal, String remoteIp, String reason) {
        meterRegistry.counter("medclinic.security.authentication", "outcome", "failure").increment();
        LOGGER.warn("security_audit event=AUTH_FAILURE principal={} remote_ip={} reason={}",
                sanitize(principal), sanitize(remoteIp), sanitize(reason));
    }

    public void logout(String principal, String remoteIp) {
        meterRegistry.counter("medclinic.security.authentication", "outcome", "logout").increment();
        LOGGER.info("security_audit event=LOGOUT principal={} remote_ip={}",
                sanitize(principal), sanitize(remoteIp));
    }

    public void rateLimitRejected(String endpoint, String remoteIp) {
        meterRegistry.counter("medclinic.security.rate_limit.rejected", "endpoint", endpoint).increment();
        LOGGER.warn("security_audit event=RATE_LIMIT_REJECTED endpoint={} remote_ip={}",
                sanitize(endpoint), sanitize(remoteIp));
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replace('\n', '_').replace('\r', '_').replace('\t', '_');
    }
}
