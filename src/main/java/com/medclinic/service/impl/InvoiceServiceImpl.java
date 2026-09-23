package com.medclinic.service.impl;

import com.medclinic.entity.*;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.BusinessAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceCodeReservationCoordinator invoiceCodeReservationCoordinator;
    private final BusinessAuditService auditService;

    @Override
    @Transactional
    public Invoice createInvoiceForAppointment(Long appointmentId, BigDecimal medicineFee) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
        if (invoiceRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BusinessLogicException("Cuộc hẹn này đã có hóa đơn");
        }

        BigDecimal consultationFee = appointment.getDoctor().getConsultationFee();
        if (consultationFee == null) consultationFee = BigDecimal.ZERO;
        if (medicineFee == null) medicineFee = BigDecimal.ZERO;

        BigDecimal totalAmount = consultationFee.add(medicineFee);
        String invoiceCode = invoiceCodeReservationCoordinator.reserveUniqueCode();

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
        return processPayment(invoiceId, paymentMethod, null);
    }

    @Override
    @Transactional
    public Invoice processPayment(Long invoiceId, PaymentMethod paymentMethod, Long actorId) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
        if (invoice.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new BusinessLogicException("Hóa đơn này đã được thanh toán");
        }
        if (paymentMethod != PaymentMethod.CASH) {
            throw new BusinessLogicException("Phương thức thanh toán này chưa được tích hợp");
        }

        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidAt(LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);
        auditService.record(actorId, "CASH_PAYMENT_CONFIRMED", "INVOICE", invoiceId,
                "SUCCESS", invoice.getInvoiceCode(), "STAFF_CASH_COLLECTION");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice findById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
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
