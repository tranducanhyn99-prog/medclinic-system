package com.medclinic;

import com.medclinic.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
