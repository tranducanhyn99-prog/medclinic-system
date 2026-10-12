package com.medclinic.service.impl;

import com.medclinic.service.EmailDeliveryMonitor;
import com.medclinic.service.EmailService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.util.HtmlUtils;
import com.medclinic.util.QrCodeUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Base64;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final EmailDeliveryMonitor monitor;
    private final int maxAttempts;
    private final long retryBackoffMs;
    private final String fromAddress;

    @Autowired
    public EmailServiceImpl(
            JavaMailSender javaMailSender,
            EmailDeliveryMonitor monitor,
            @Value("${app.mail-delivery.max-attempts:3}") int maxAttempts,
            @Value("${app.mail-delivery.retry-backoff-ms:250}") long retryBackoffMs,
            @Value("${app.mail-delivery.from:no-reply@medclinic.test}") String fromAddress) {
        // Google presents app passwords in four groups; other SMTP passwords keep their spaces.
        if (javaMailSender instanceof JavaMailSenderImpl sender
                && "smtp.gmail.com".equalsIgnoreCase(sender.getHost()) && sender.getPassword() != null
                && sender.getPassword().matches("\\S{4}( \\S{4}){3}")) {
            sender.setPassword(sender.getPassword().replace(" ", ""));
        }
        this.javaMailSender = javaMailSender;
        this.monitor = monitor;
        this.maxAttempts = Math.max(1, Math.min(maxAttempts, 5));
        this.retryBackoffMs = Math.max(0, Math.min(retryBackoffMs, 5000));
        this.fromAddress = fromAddress;
    }

    public EmailServiceImpl(JavaMailSender javaMailSender, EmailDeliveryMonitor monitor,
                            int maxAttempts, long retryBackoffMs) {
        this(javaMailSender, monitor, maxAttempts, retryBackoffMs, "no-reply@medclinic.test");
    }

    /** Convenience constructor retained for focused unit tests. */
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this(javaMailSender, new EmailDeliveryMonitor(new SimpleMeterRegistry()), 1, 0);
    }

    @Async
    @Override
    public void sendBookingConfirmationEmail(String toEmail, String patientName, String bookingCode,
                                             String doctorName, LocalDate appointmentDate, String timeRange) {
        sendWithRetry("booking", toEmail, helper -> {
            helper.setSubject("Xác nhận đặt lịch khám - MedClinic");
            helper.setText(String.format("""
                    <html><body>
                    <h2>Chào %s,</h2>
                    <p>Bạn đã đặt lịch khám thành công tại MedClinic.</p>
                    <p><b>Mã đặt lịch:</b> %s</p><p><b>Bác sĩ:</b> %s</p>
                    <p><b>Ngày khám:</b> %s</p><p><b>Thời gian:</b> %s</p>
                    <p>Xuất trình mã QR dưới đây tại quầy tiếp nhận. Nếu ảnh không hiển thị, dùng mã đặt lịch phía trên.</p>
                    <img src="cid:booking-qr" alt="Mã QR phiếu hẹn" width="240" height="240" />
                    <p>Vui lòng đến trước 15 phút để hoàn tất thủ tục đăng ký.</p>
                    <p>Trân trọng,<br/>Phòng khám MedClinic</p>
                    </body></html>
                    """, escape(patientName), escape(bookingCode), escape(doctorName), appointmentDate, escape(timeRange)), true);
            String qr = QrCodeUtil.generateQrCodeBase64(bookingCode, 240, 240);
            if (qr.isBlank()) throw new IllegalStateException("Booking QR generation failed");
            byte[] png = Base64.getDecoder().decode(qr.substring(qr.indexOf(',') + 1));
            helper.addInline("booking-qr", new ByteArrayResource(png), "image/png");
        });
    }

    @Async
    @Override
    public void sendCancellationEmail(String toEmail, String patientName, String bookingCode) {
        sendWithRetry("cancellation", toEmail, helper -> {
            helper.setSubject("Hủy lịch khám - MedClinic");
            helper.setText(String.format("""
                    <html><body><h2>Chào %s,</h2>
                    <p>Lịch khám <b>%s</b> đã được hủy thành công.</p>
                    <p>Nếu có nhu cầu, vui lòng đặt lại lịch khám mới trên hệ thống.</p>
                    <p>Trân trọng,<br/>Phòng khám MedClinic</p></body></html>
                    """, escape(patientName), escape(bookingCode)), true);
        });
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetUrl) {
        sendWithRetry("password_reset", toEmail, helper -> {
            helper.setSubject("Khôi phục mật khẩu MedClinic");
            helper.setText(String.format("""
                    <html><body><h2>Chào %s,</h2>
                    <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu tài khoản MedClinic của bạn.</p>
                    <p><a href="%s">Đặt lại mật khẩu</a></p>
                    <p>Liên kết có hiệu lực trong 24 giờ và chỉ dùng được một lần.</p>
                    <p>Nếu không phải bạn yêu cầu, hãy bỏ qua email này.</p>
                    </body></html>
                    """, escape(userName), escape(resetUrl)), true);
        });
    }

    @Async
    @Override
    public void sendAuthenticationOtpEmail(String toEmail, String userName, String otpCode,
                                           String confirmationUrl, long validMinutes, String purpose) {
        sendWithRetry("authentication_otp", toEmail, helper -> {
            helper.setSubject("Mã xác thực " + purpose + " - MedClinic");
            helper.setText(String.format("""
                    <html><body style="font-family:Arial,sans-serif;color:#1f2937">
                    <h2>Chào %s,</h2><p>Bạn đang xác thực <b>%s</b> trên MedClinic.</p>
                    <p>Mã OTP của bạn:</p>
                    <div style="font-size:30px;font-weight:700;letter-spacing:8px;margin:18px 0">%s</div>
                    <p>Mã và liên kết này có hiệu lực trong <b>%d phút</b> và chỉ dùng được một lần.</p>
                    <p style="margin:24px 0"><a href="%s" style="background:#0f6f8f;color:white;padding:12px 22px;text-decoration:none;border-radius:8px;font-weight:700">Xác nhận và đăng nhập</a></p>
                    <p>Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.</p>
                    <p>Trân trọng,<br/>MedClinic</p></body></html>
                    """, escape(userName), escape(purpose), escape(otpCode), validMinutes, escape(confirmationUrl)), true);
        });
    }

    private void sendWithRetry(String type, String toEmail, MessageComposer composer) {
        String recipient = EmailDeliveryMonitor.recipientFingerprint(toEmail);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(toEmail);
                composer.compose(helper);
                javaMailSender.send(message);
                monitor.success(type);
                log.info("Email delivered type={} recipient_hash={}", type, recipient);
                return;
            } catch (Exception exception) {
                if (attempt < maxAttempts) {
                    monitor.retry(type);
                    sleepBeforeRetry(attempt);
                } else {
                    monitor.failure(type);
                    log.error("Email delivery exhausted retries type={} recipient_hash={} attempts={} reason={}",
                            type, recipient, maxAttempts, exception.getClass().getSimpleName());
                }
            }
        }
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(retryBackoffMs * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface MessageComposer {
        void compose(MimeMessageHelper helper) throws Exception;
    }

    private static String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
