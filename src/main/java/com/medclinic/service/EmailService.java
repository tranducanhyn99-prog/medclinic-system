package com.medclinic.service;

import java.time.LocalDate;

public interface EmailService {
    void sendBookingConfirmationEmail(String toEmail, String patientName, String bookingCode, String doctorName, LocalDate appointmentDate, String timeRange);
    void sendCancellationEmail(String toEmail, String patientName, String bookingCode);
    void sendPasswordResetEmail(String toEmail, String userName, String resetUrl);
}
