package com.medclinic.config;

import com.medclinic.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Keeps H2/dev and integration tests offline while exercising after-commit events. */
@Service
@Primary
@Profile({"dev", "demo"})
@Slf4j
public class DevelopmentEmailService implements EmailService {
    @Override
    public void sendBookingConfirmationEmail(String toEmail, String patientName, String bookingCode,
                                             String doctorName, LocalDate appointmentDate, String timeRange) {
        log.info("DEV email suppressed: booking confirmation {} to {}", bookingCode, toEmail);
    }

    @Override
    public void sendCancellationEmail(String toEmail, String patientName, String bookingCode) {
        log.info("DEV email suppressed: cancellation {} to {}", bookingCode, toEmail);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetUrl) {
        log.info("DEV email suppressed: password reset to {}", toEmail);
    }
}
