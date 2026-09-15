package com.medclinic.service.impl;

import com.medclinic.entity.*;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public Invoice createInvoiceForAppointment(Long appointmentId, BigDecimal medicineFee) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));

        BigDecimal consultationFee = appointment.getDoctor().getConsultationFee();
        if (consultationFee == null) consultationFee = BigDecimal.ZERO;
        if (medicineFee == null) medicineFee = BigDecimal.ZERO;

        BigDecimal totalAmount = consultationFee.add(medicineFee);
        String invoiceCode = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(invoiceCode);
        invoice.setAppointment(appointment);
        invoice.setConsultationFee(consultationFee);
        invoice.setMedicineFee(medicineFee);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaymentStatus(PaymentStatus.UNPAID);
        invoice.setCreatedAt(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice processPayment(Long invoiceId, PaymentMethod paymentMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));

        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidAt(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice findByAppointmentId(Long appointmentId) {
        return invoiceRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn cho cuộc hẹn này"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findAllByStatus(PaymentStatus status) {
        return invoiceRepository.findByPaymentStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }
}
