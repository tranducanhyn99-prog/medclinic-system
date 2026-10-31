package com.medclinic.config;

import com.medclinic.service.EmailDeliveryMonitor;
import com.medclinic.service.EmailService;
import com.medclinic.service.impl.EmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Keeps H2/dev and integration tests offline when SMTP credentials are absent or recipient is a test address,
 * while forwarding to real SMTP when MAIL_USERNAME and MAIL_PASSWORD are configured.
 */
@Service
@Primary
@Profile("dev")
@Slf4j
public class DevelopmentEmailService implements EmailService {
    private final EmailServiceImpl delegate;
    private final boolean smtpConfigured;

    public DevelopmentEmailService(
            EmailServiceImpl delegate,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.delegate = delegate;
        this.smtpConfigured = mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
    }

    private boolean shouldSendRealEmail(String toEmail) {
        if (!smtpConfigured || toEmail == null || toEmail.isBlank()) {
            return false;
        }
        String lower = toEmail.toLowerCase();
        return !lower.endsWith(".test") && !lower.endsWith(".local") && !lower.endsWith("@medclinic.com");
    }

    @Override
    public void sendBookingConfirmationEmail(String toEmail, String patientName, String bookingCode,
                                             String doctorName, LocalDate appointmentDate, String timeRange) {
        if (shouldSendRealEmail(toEmail)) {
            delegate.sendBookingConfirmationEmail(toEmail, patientName, bookingCode, doctorName, appointmentDate, timeRange);
            return;
        }
        log.info("DEV email suppressed: booking={} recipient_hash={}", bookingCode,
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }

    @Override
    public void sendCancellationEmail(String toEmail, String patientName, String bookingCode) {
        if (shouldSendRealEmail(toEmail)) {
            delegate.sendCancellationEmail(toEmail, patientName, bookingCode);
            return;
        }
        log.info("DEV email suppressed: cancellation={} recipient_hash={}", bookingCode,
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetUrl) {
        log.info("DEV password reset URL: to={} url={}", toEmail, resetUrl);
        if (shouldSendRealEmail(toEmail)) {
            delegate.sendPasswordResetEmail(toEmail, userName, resetUrl);
            return;
        }
        log.info("DEV email suppressed: password_reset recipient_hash={}",
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }

    @Override
    public void sendAuthenticationOtpEmail(String toEmail, String userName, String otpCode,
                                           String confirmationUrl, long validMinutes, String purpose) {
        log.info("DEV OTP [{}]: to={} code={} url={}", purpose, toEmail, otpCode, confirmationUrl);
        if (shouldSendRealEmail(toEmail)) {
            delegate.sendAuthenticationOtpEmail(toEmail, userName, otpCode, confirmationUrl, validMinutes, purpose);
            return;
        }
        log.info("DEV OTP email suppressed: purpose={} recipient_hash={}", purpose,
                EmailDeliveryMonitor.recipientFingerprint(toEmail));
    }
}
