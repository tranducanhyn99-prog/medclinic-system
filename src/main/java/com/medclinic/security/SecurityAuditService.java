package com.medclinic.security;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditService.class);
    private final MeterRegistry meterRegistry;

    public void authenticationSuccess(String principal, String remoteIp) {
        meterRegistry.counter("medclinic.security.authentication", "outcome", "success").increment();
        LOGGER.info("security_audit event=AUTH_SUCCESS principal_hash={} remote_hash={}",
                fingerprint(principal), fingerprint(remoteIp));
    }

    public void authenticationFailure(String principal, String remoteIp, String reason) {
        meterRegistry.counter("medclinic.security.authentication", "outcome", "failure").increment();
        LOGGER.warn("security_audit event=AUTH_FAILURE principal_hash={} remote_hash={} reason={}",
                fingerprint(principal), fingerprint(remoteIp), sanitize(reason));
    }

    public void logout(String principal, String remoteIp) {
        meterRegistry.counter("medclinic.security.authentication", "outcome", "logout").increment();
        LOGGER.info("security_audit event=LOGOUT principal_hash={} remote_hash={}",
                fingerprint(principal), fingerprint(remoteIp));
    }

    public void rateLimitRejected(String endpoint, String remoteIp) {
        meterRegistry.counter("medclinic.security.rate_limit.rejected", "endpoint", endpoint).increment();
        LOGGER.warn("security_audit event=RATE_LIMIT_REJECTED endpoint={} remote_hash={}",
                sanitize(endpoint), fingerprint(remoteIp));
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replace('\n', '_').replace('\r', '_').replace('\t', '_');
    }

    private String fingerprint(String value) {
        try {
            String normalized = value == null ? "unknown" : value.trim().toLowerCase();
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }
}
