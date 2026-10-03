package com.medclinic.config;

import com.medclinic.service.EmailService;
import com.medclinic.service.EmailDeliveryMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Keeps H2/dev and integration tests offline while exercising after-commit events. */
@Service
@Primary
@Profile("dev")
@Slf4j
public class DevelopmentEmailService implements EmailService {
    @Override
    public void sendBookingConfirmationEmail(String toEmail, String patientName, String bookingCode,
                                             String doctorName, LocalDate appointmentDate, String timeRange) {
        log.info("DEV email suppressed: booking={} recipient_hash={}", bookingCode,
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }

    @Override
    public void sendCancellationEmail(String toEmail, String patientName, String bookingCode) {
        log.info("DEV email suppressed: cancellation={} recipient_hash={}", bookingCode,
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetUrl) {
        log.info("DEV email suppressed: password_reset recipient_hash={}",
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }
    @Override
    public void sendAuthenticationOtpEmail(String toEmail, String userName, String otpCode,
                                           String confirmationUrl, long validMinutes, String purpose) {
        log.info("DEV OTP email suppressed: purpose={} recipient_hash={}", purpose,
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }
}
