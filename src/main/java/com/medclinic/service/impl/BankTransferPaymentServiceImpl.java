package com.medclinic.service.impl;

import com.medclinic.config.BankPaymentProperties;
import com.medclinic.dto.BankTransferCallbackDto;
import com.medclinic.dto.BankTransferInstruction;
import com.medclinic.entity.BankTransferPayment;
import com.medclinic.entity.Invoice;
import com.medclinic.enums.BankTransferStatus;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.BankTransferPaymentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.security.HmacSignatureVerifier;
import com.medclinic.service.BankTransferPaymentService;
import com.medclinic.service.BusinessAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankTransferPaymentServiceImpl implements BankTransferPaymentService {
    private final BankTransferPaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BankPaymentProperties properties;
    private final BusinessAuditService auditService;

    @Override
    public boolean isAvailable() {
        return properties.isReady();
    }

    @Override
    @Transactional
    public BankTransferInstruction createInstruction(Long invoiceId) {
        requireReady();
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
        if (invoice.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new BusinessLogicException("Hóa đơn này không còn chờ thanh toán");
        }

        LocalDateTime now = LocalDateTime.now();
        var existing = paymentRepository.findFirstByInvoiceIdAndStatusOrderByCreatedAtDesc(
                invoiceId, BankTransferStatus.PENDING);
        if (existing.isPresent() && existing.get().getExpiresAt().isAfter(now)) {
            return toInstruction(existing.get());
        }
        existing.ifPresent(payment -> {
            payment.setStatus(BankTransferStatus.EXPIRED);
            paymentRepository.save(payment);
        });

        BankTransferPayment payment = BankTransferPayment.builder()
                .invoice(invoice)
                .transferCode(nextTransferCode())
                .amount(invoice.getTotalAmount())
                .bankId(properties.getBankId().toUpperCase(Locale.ROOT))
                .status(BankTransferStatus.PENDING)
                .expiresAt(now.plusMinutes(Math.max(5, properties.getExpiryMinutes())))
                .build();
        payment = paymentRepository.save(payment);
        auditService.record(null, "BANK_TRANSFER_CREATED", "INVOICE", invoiceId,
                "SUCCESS", payment.getTransferCode(), "VIETQR");
        return toInstruction(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public BankTransferInstruction findInstruction(Long paymentId) {
        BankTransferPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu chuyển khoản"));
        return toInstruction(payment);
    }

    @Override
    @Transactional
    public BankTransferInstruction confirmCallback(BankTransferCallbackDto callback, String signature) {
        requireReady();
        String canonical = canonical(callback);
        if (!HmacSignatureVerifier.matchesSha256(properties.getCallbackSecret(), canonical, signature)) {
            auditService.recordIndependent(null, "BANK_CALLBACK", "BANK_TRANSFER", callback.transferCode(),
                    "REJECTED", callback.eventId(), "INVALID_SIGNATURE");
            throw new SecurityException("Chữ ký callback không hợp lệ");
        }

        var duplicate = paymentRepository.findByProviderEventId(callback.eventId());
        if (duplicate.isPresent()) return toInstruction(duplicate.get());

        BankTransferPayment payment = paymentRepository.findByTransferCodeForUpdate(callback.transferCode())
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy nội dung chuyển khoản"));
        if (payment.getStatus() == BankTransferStatus.CONFIRMED
                && callback.transactionId().equals(payment.getBankTransactionId())) {
            return toInstruction(payment);
        }
        if (payment.getStatus() != BankTransferStatus.PENDING) {
            reject(payment, callback, "PAYMENT_NOT_PENDING");
            throw new BusinessLogicException("Yêu cầu chuyển khoản không còn hiệu lực");
        }
        if (payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(BankTransferStatus.EXPIRED);
            paymentRepository.save(payment);
            reject(payment, callback, "PAYMENT_EXPIRED");
            throw new BusinessLogicException("Yêu cầu chuyển khoản đã hết hạn; cần đối soát thủ công");
        }
        if (payment.getAmount().compareTo(callback.amount()) != 0) {
            reject(payment, callback, "AMOUNT_MISMATCH");
            throw new BusinessLogicException("Số tiền callback không khớp hóa đơn");
        }
        if (paymentRepository.existsByBankTransactionId(callback.transactionId())) {
            reject(payment, callback, "DUPLICATE_BANK_TRANSACTION");
            throw new BusinessLogicException("Giao dịch ngân hàng đã được sử dụng");
        }

        confirm(payment, callback.transactionId(), callback.eventId(),
                callback.occurredAt() == null ? LocalDateTime.now() : callback.occurredAt());
        auditService.record(null, "BANK_CALLBACK", "INVOICE", payment.getInvoice().getId(),
                "SUCCESS", callback.eventId(), "AMOUNT_AND_REFERENCE_MATCHED");
        return toInstruction(payment);
    }

    @Override
    @Transactional
    public BankTransferInstruction confirmManual(Long paymentId, String bankTransactionId, Long actorId) {
        if (bankTransactionId == null || !bankTransactionId.matches("[A-Za-z0-9._-]{6,100}")) {
            throw new BusinessLogicException("Mã giao dịch ngân hàng không hợp lệ");
        }
        BankTransferPayment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu chuyển khoản"));
        if (payment.getStatus() == BankTransferStatus.CONFIRMED) return toInstruction(payment);
        if (payment.getStatus() == BankTransferStatus.REJECTED) {
            throw new BusinessLogicException("Yêu cầu chuyển khoản đã bị từ chối");
        }
        if (paymentRepository.existsByBankTransactionId(bankTransactionId)) {
            throw new BusinessLogicException("Mã giao dịch ngân hàng đã được đối soát");
        }
        confirm(payment, bankTransactionId, null, LocalDateTime.now());
        auditService.record(actorId, "BANK_TRANSFER_MANUAL_CONFIRM", "INVOICE",
                payment.getInvoice().getId(), "SUCCESS", bankTransactionId, "STAFF_RECONCILIATION");
        return toInstruction(payment);
    }

    private void confirm(BankTransferPayment payment, String bankTransactionId,
                         String providerEventId, LocalDateTime confirmedAt) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(payment.getInvoice().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
        if (invoice.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new BusinessLogicException("Hóa đơn đã được thanh toán bằng phương thức khác");
        }
        payment.setStatus(BankTransferStatus.CONFIRMED);
        payment.setBankTransactionId(bankTransactionId);
        payment.setProviderEventId(providerEventId);
        payment.setConfirmedAt(confirmedAt.truncatedTo(ChronoUnit.MILLIS));
        invoice.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setPaidAt(payment.getConfirmedAt());
        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
    }

    private void reject(BankTransferPayment payment, BankTransferCallbackDto callback, String reason) {
        auditService.recordIndependent(null, "BANK_CALLBACK", "BANK_TRANSFER", payment.getId(),
                "REJECTED", callback.eventId(), reason);
    }

    private void requireReady() {
        if (!properties.isReady()) {
            throw new BusinessLogicException("Thanh toán ngân hàng chưa được cấu hình đầy đủ");
        }
    }

    private String nextTransferCode() {
        for (int i = 0; i < 8; i++) {
            String random = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 6).toUpperCase(Locale.ROOT);
            String code = "Thanh toan hoa don " + random;
            if (!paymentRepository.existsByTransferCode(code)) return code;
        }
        throw new BusinessLogicException("Không thể cấp nội dung chuyển khoản duy nhất");
    }

    private BankTransferInstruction toInstruction(BankTransferPayment payment) {
        Invoice invoice = payment.getInvoice();
        return new BankTransferInstruction(
                payment.getId(), invoice.getId(), invoice.getInvoiceCode(),
                properties.getBankId(), properties.getBankName(), properties.getAccountNo(),
                properties.getAccountName(), payment.getTransferCode(), payment.getAmount(),
                qrUrl(payment), payment.getStatus(), payment.getExpiresAt(), payment.getConfirmedAt());
    }

    private String qrUrl(BankTransferPayment payment) {
        String path = "https://img.vietqr.io/image/" + properties.getBankId() + "-"
                + properties.getAccountNo() + "-" + properties.getQrTemplate() + ".png";
        long amount;
        try {
            amount = payment.getAmount().stripTrailingZeros().longValueExact();
        } catch (ArithmeticException exception) {
            throw new BusinessLogicException("Số tiền VND phải là số nguyên");
        }
        return UriComponentsBuilder.fromUriString(path)
                .queryParam("amount", amount)
                .queryParam("addInfo", payment.getTransferCode())
                .queryParam("accountName", properties.getAccountName())
                .build().encode().toUriString();
    }

    public static String canonical(BankTransferCallbackDto callback) {
        return callback.eventId() + "|" + callback.transactionId() + "|"
                + callback.transferCode() + "|" + callback.amount().stripTrailingZeros().toPlainString()
                + "|" + (callback.occurredAt() == null ? "" : callback.occurredAt().toString());
    }
}
