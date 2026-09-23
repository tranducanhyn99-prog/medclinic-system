package com.medclinic;

import com.medclinic.service.impl.EmailServiceImpl;
import com.medclinic.service.EmailDeliveryMonitor;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.mail.Session;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {
    @Mock private JavaMailSender javaMailSender;

    private MimeMessage message() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private EmailServiceImpl service() {
        return new EmailServiceImpl(javaMailSender);
    }
    @Test
    void sendsBookingConfirmationEmail() {
        MimeMessage message = message();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        service().sendBookingConfirmationEmail(
                "patient@example.com", "Nguyễn Văn A", "MED-001",
                "BS Nguyễn", LocalDate.of(2026, 9, 20), "08:00 - 08:30");

        verify(javaMailSender).send(message);
    }

    @Test
    void sendsCancellationEmail() {
        MimeMessage message = message();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        service().sendCancellationEmail("patient@example.com", "Nguyễn Văn A", "MED-001");

        verify(javaMailSender).send(message);
    }
    @Test
    void sendsPasswordResetEmail() {
        MimeMessage message = message();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        service().sendPasswordResetEmail(
                "patient@example.com", "Nguyễn Văn A", "https://example.test/reset?token=abc");

        verify(javaMailSender).send(message);
    }

    @Test
    void suppressesMailInfrastructureFailure() {
        when(javaMailSender.createMimeMessage()).thenThrow(new IllegalStateException("SMTP unavailable"));

        assertDoesNotThrow(() -> service().sendBookingConfirmationEmail(
                "patient@example.com", "Nguyễn Văn A", "MED-001",
                "BS Nguyễn", LocalDate.of(2026, 9, 20), "08:00 - 08:30"));

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
    @Test
    void authenticationOtpEmailContainsCodeExpiryAndConfirmationLink() throws Exception {
        MimeMessage message = message();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        service().sendAuthenticationOtpEmail(
                "patient@example.com", "Nguyễn Văn A", "123456",
                "https://clinic.example/auth/verify-email?token=abc", 5, "đăng nhập");

        verify(javaMailSender).send(message);
        String body = extractText(message.getContent());
        assertTrue(body.contains("123456"));
        assertTrue(body.contains("5 phút"));
        assertTrue(body.contains("https://clinic.example/auth/verify-email?token=abc"));
        assertTrue(body.contains("Xác nhận và đăng nhập"));
    }

    @Test
    void retriesTransientFailureAndRecordsBoundedMetrics() {
        MimeMessage message = message();
        when(javaMailSender.createMimeMessage())
                .thenThrow(new IllegalStateException("temporary-1"))
                .thenThrow(new IllegalStateException("temporary-2"))
                .thenReturn(message);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        EmailDeliveryMonitor monitor = new EmailDeliveryMonitor(registry);
        EmailServiceImpl retrying = new EmailServiceImpl(javaMailSender, monitor, 3, 0);

        retrying.sendPasswordResetEmail(
                "patient@example.com", "Nguyễn Văn A", "https://example.test/reset?token=abc");

        verify(javaMailSender).send(message);
        assertEquals(2.0, registry.get("medclinic.email.delivery")
                .tag("type", "password_reset").tag("outcome", "retry").counter().count());
        assertEquals(1.0, registry.get("medclinic.email.delivery")
                .tag("type", "password_reset").tag("outcome", "success").counter().count());
        assertEquals(0, monitor.consecutiveFailures());
    }

    private String extractText(Object content) throws Exception {
        if (content instanceof String text) return text;
        if (content instanceof Multipart multipart) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                out.append(extractText(multipart.getBodyPart(i).getContent()));
            }
            return out.toString();
        }
        return "";
    }

}
