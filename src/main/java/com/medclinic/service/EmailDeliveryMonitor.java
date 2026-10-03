package com.medclinic.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class EmailDeliveryMonitor {
    private final MeterRegistry meterRegistry;
    private final AtomicInteger consecutiveFailures = new AtomicInteger();

    public EmailDeliveryMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        Gauge.builder("medclinic.email.consecutive_failures", consecutiveFailures, AtomicInteger::get)
                .description("Consecutive email deliveries that exhausted all retries")
                .register(meterRegistry);
    }

    public void success(String type) {
        consecutiveFailures.set(0);
        meterRegistry.counter("medclinic.email.delivery", "type", type, "outcome", "success").increment();
    }

    public void retry(String type) {
        meterRegistry.counter("medclinic.email.delivery", "type", type, "outcome", "retry").increment();
    }

    public void failure(String type) {
        consecutiveFailures.incrementAndGet();
        meterRegistry.counter("medclinic.email.delivery", "type", type, "outcome", "failure").increment();
    }

    public int consecutiveFailures() {
        return consecutiveFailures.get();
    }

    public static String recipientFingerprint(String email) {
        String normalized = email == null ? "unknown" : email.trim().toLowerCase();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }
}
