package com.medclinic.service.impl;

import com.medclinic.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    @Override
    public void sendBookingConfirmationEmail(String toEmail, String patientName, String bookingCode, String doctorName, LocalDate appointmentDate, String timeRange) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt lịch khám - MedClinic");
            
            String htmlBody = String.format("""
                <html>
                    <body>
                        <h2>Chào %s,</h2>
                        <p>Bạn đã đặt lịch khám thành công tại MedClinic.</p>
                        <p><b>Mã đặt lịch:</b> %s</p>
                        <p><b>Bác sĩ:</b> %s</p>
                        <p><b>Ngày khám:</b> %s</p>
                        <p><b>Thời gian:</b> %s</p>
                        <br/>
                        <p>Vui lòng đến trước 15 phút để hoàn tất thủ tục đăng ký.</p>
                        <p>Trân trọng,<br/>Phòng khám MedClinic</p>
                    </body>
                </html>
                """, patientName, bookingCode, doctorName, appointmentDate, timeRange);
                
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            log.info("Booking confirmation email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation email to: {}", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendCancellationEmail(String toEmail, String patientName, String bookingCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Hủy lịch khám - MedClinic");
            
            String htmlBody = String.format("""
                <html>
                    <body>
                        <h2>Chào %s,</h2>
                        <p>Lịch khám của bạn đã được hủy thành công.</p>
                        <p><b>Mã đặt lịch:</b> %s</p>
                        <br/>
                        <p>Nếu bạn có nhu cầu, vui lòng đặt lại lịch khám mới trên hệ thống.</p>
                        <p>Trân trọng,<br/>Phòng khám MedClinic</p>
                    </body>
                </html>
                """, patientName, bookingCode);
                
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            log.info("Cancellation email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send cancellation email to: {}", toEmail, e);
        }
    }
    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetUrl) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Khôi phục mật khẩu MedClinic");
            String htmlBody = String.format("""
                    <html><body>
                    <h2>Chào %s,</h2>
                    <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu tài khoản MedClinic của bạn.</p>
                    <p><a href="%s">Đặt lại mật khẩu</a></p>
                    <p>Liên kết có hiệu lực trong 24 giờ và chỉ dùng được một lần.</p>
                    <p>Nếu không phải bạn yêu cầu, hãy bỏ qua email này.</p>
                    </body></html>
                    """, userName, resetUrl);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
        } catch (MessagingException exception) {
            log.error("Failed to send password reset email to: {}", toEmail, exception);
        }
    }
}
