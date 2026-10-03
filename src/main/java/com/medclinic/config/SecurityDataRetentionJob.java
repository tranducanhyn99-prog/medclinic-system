package com.medclinic.config;

import com.medclinic.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityDataRetentionJob {
    private final AuthVerificationChallengeRepository challenges;
    private final PasswordResetTokenRepository resetTokens;
    private final SecurityRateLimitBucketRepository rateLimits;
    private final BusinessAuditEventRepository auditEvents;
    private final BankTransferPaymentRepository bankPayments;
    private final MeterRegistry meterRegistry;

    @Value("${app.retention.auth-token-days:7}")
    private int authTokenDays;

    @Value("${app.retention.audit-days:1825}")
    private int auditDays;

    @Scheduled(cron = "${app.retention.cleanup-cron:0 30 2 * * *}", zone = "UTC")
    @Transactional
    public void purgeExpiredOperationalData() {
        LocalDateTime now = LocalDateTime.now();
        long challengeCount = challenges.deleteByExpiresAtBefore(now.minusDays(authTokenDays));
        long resetCount = resetTokens.deleteByExpiresAtBefore(now.minusDays(authTokenDays));
        long bucketCount = rateLimits.deleteByWindowStartBefore(now.minusDays(1));
        long auditCount = auditEvents.deleteByCreatedAtBefore(now.minusDays(auditDays));
        long expiredPayments = bankPayments.expirePendingBefore(now);

        record("auth_challenge", challengeCount);
        record("password_reset", resetCount);
        record("rate_limit", bucketCount);
        record("business_audit", auditCount);
        record("bank_payment_expired", expiredPayments);
    }

    private void record(String type, long count) {
        if (count > 0) {
            meterRegistry.counter("medclinic.retention.deleted", "type", type).increment(count);
        }
    }
}
