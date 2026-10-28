package com.medclinic.service;

import com.medclinic.entity.*;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.impl.InvoiceCodeReservationCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.medclinic.util.VndMoney;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceCodeReservationCoordinator invoiceCodeReservationCoordinator;
    private final BusinessAuditService auditService;

    private com.medclinic.repository.BankTransferPaymentRepository bankTransferPaymentRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setBankTransferPaymentRepository(com.medclinic.repository.BankTransferPaymentRepository bankTransferPaymentRepository) {
        this.bankTransferPaymentRepository = bankTransferPaymentRepository;
    }

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
        VndMoney.requireValid(consultationFee, "Tiền khám");
        VndMoney.requireValid(medicineFee, "Tiền thuốc");
        VndMoney.requireValid(totalAmount, "Tổng hóa đơn");
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

    @Transactional
    public Invoice processPayment(Long invoiceId, PaymentMethod paymentMethod) {
        return processPayment(invoiceId, paymentMethod, null);
    }

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
        if (bankTransferPaymentRepository != null) {
            bankTransferPaymentRepository.findFirstByInvoiceIdAndStatusOrderByCreatedAtDesc(
                    invoiceId, com.medclinic.enums.BankTransferStatus.PENDING).ifPresent(pending -> {
                pending.setStatus(com.medclinic.enums.BankTransferStatus.EXPIRED);
                bankTransferPaymentRepository.save(pending);
            });
        }
        auditService.record(actorId, "CASH_PAYMENT_CONFIRMED", "INVOICE", invoiceId,
                "SUCCESS", invoice.getInvoiceCode(), "STAFF_CASH_COLLECTION");
        return saved;
    }

    @Transactional(readOnly = true)
    public Invoice findById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
    }

    @Transactional(readOnly = true)
    public Invoice findByAppointmentId(Long appointmentId) {
        return invoiceRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn cho cuộc hẹn này"));
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findForBilling(PaymentStatus status, LocalDate from, LocalDate to, int page, int size) {
        validateDateRange(from, to);
        int boundedSize = Math.max(1, Math.min(100, size));
        var pageable = PageRequest.of(Math.max(0, Math.min(1_000_000, page)), boundedSize,
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var result = invoiceRepository.findForBilling(status, from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(), pageable);
        if (result.getTotalPages() == 0 && result.getNumber() > 0) {
            return Page.empty(pageable.withPage(0));
        }
        if (result.getTotalPages() > 0 && result.getNumber() >= result.getTotalPages()) {
            return invoiceRepository.findForBilling(status, from == null ? null : from.atStartOfDay(),
                    to == null ? null : to.plusDays(1).atStartOfDay(),
                    pageable.withPage(result.getTotalPages() - 1));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public long countForBilling(PaymentStatus status, LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        return invoiceRepository.countForBilling(status, from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay());
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (to != null && (to.equals(LocalDate.MAX) || from != null && from.isAfter(to))) {
            throw new BusinessLogicException("Khoảng ngày hóa đơn không hợp lệ");
        }
    }

}
