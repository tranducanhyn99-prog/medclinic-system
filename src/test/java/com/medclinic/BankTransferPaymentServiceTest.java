package com.medclinic;

import com.medclinic.config.BankPaymentProperties;
import com.medclinic.dto.BankTransferCallbackDto;
import com.medclinic.entity.BankTransferPayment;
import com.medclinic.entity.Invoice;
import com.medclinic.enums.BankTransferStatus;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.BankTransferPaymentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.security.HmacSignatureVerifier;
import com.medclinic.service.BusinessAuditService;
import com.medclinic.service.impl.BankTransferPaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    @Mock BankTransferPaymentRepository payments;
    @Mock InvoiceRepository invoices;
    @Mock BusinessAuditService audit;
    private BankTransferPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        BankPaymentProperties properties = new BankPaymentProperties();
        properties.setEnabled(true);
        properties.setBankId("970422");
        properties.setBankName("MB Bank");
        properties.setAccountNo("0123456789");
        properties.setAccountName("MEDCLINIC TEST");
        properties.setCallbackSecret(SECRET);
        service = new BankTransferPaymentServiceImpl(payments, invoices, properties, audit);
    }

    @Test
    void createsReusableVietQrInstructionWithoutMarkingInvoicePaid() {
        Invoice invoice = unpaidInvoice();
        when(invoices.findByIdForUpdate(7L)).thenReturn(Optional.of(invoice));
        when(payments.findFirstByInvoiceIdAndStatusOrderByCreatedAtDesc(7L, BankTransferStatus.PENDING))
                .thenReturn(Optional.empty());
        when(payments.existsByTransferCode(anyString())).thenReturn(false);
        when(payments.save(any())).thenAnswer(invocation -> {
            BankTransferPayment payment = invocation.getArgument(0);
            payment.setId(11L);
            return payment;
        });

        var instruction = service.createInstruction(7L);

        assertEquals(11L, instruction.paymentId());
        assertEquals("970422", instruction.bankId());
        assertEquals(new BigDecimal("125000.00"), instruction.amount());
        assertTrue(instruction.qrImageUrl().startsWith("https://img.vietqr.io/image/970422-0123456789-compact2.png"));
        assertTrue(instruction.qrImageUrl().contains("amount=125000"));
        assertEquals(PaymentStatus.UNPAID, invoice.getPaymentStatus());
    }

    @Test
    void rejectsUnsignedOrWrongAmountCallback() {
        var callback = callback(new BigDecimal("120000.00"));
        assertThrows(SecurityException.class, () -> service.confirmCallback(callback, "00".repeat(32)));

        BankTransferPayment payment = pendingPayment(unpaidInvoice());
        when(payments.findByProviderEventId("evt-1")).thenReturn(Optional.empty());
        when(payments.findByTransferCodeForUpdate("MC7ABCDEFGH")).thenReturn(Optional.of(payment));
        String signature = HmacSignatureVerifier.signSha256(
                SECRET, BankTransferPaymentServiceImpl.canonical(callback));

        assertThrows(BusinessLogicException.class, () -> service.confirmCallback(callback, signature));
        assertEquals(BankTransferStatus.PENDING, payment.getStatus());
        assertEquals(PaymentStatus.UNPAID, payment.getInvoice().getPaymentStatus());
        verify(invoices, never()).save(any());
    }

    @Test
    void confirmsExactCallbackAndTreatsProviderRetryAsIdempotent() {
        Invoice invoice = unpaidInvoice();
        BankTransferPayment payment = pendingPayment(invoice);
        var callback = callback(new BigDecimal("125000.00"));
        when(payments.findByProviderEventId("evt-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(payment));
        when(payments.findByTransferCodeForUpdate("MC7ABCDEFGH")).thenReturn(Optional.of(payment));
        when(payments.existsByBankTransactionId("txn-123456")).thenReturn(false);
        when(invoices.findByIdForUpdate(7L)).thenReturn(Optional.of(invoice));
        String signature = HmacSignatureVerifier.signSha256(
                SECRET, BankTransferPaymentServiceImpl.canonical(callback));

        var confirmed = service.confirmCallback(callback, signature);
        var retry = service.confirmCallback(callback, signature);

        assertEquals(BankTransferStatus.CONFIRMED, confirmed.status());
        assertEquals(BankTransferStatus.CONFIRMED, retry.status());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        assertEquals(PaymentMethod.BANK_TRANSFER, invoice.getPaymentMethod());
        assertEquals("txn-123456", payment.getBankTransactionId());
        verify(invoices, times(1)).save(invoice);
    }

    @Test
    void manualReconciliationRequiresUniqueBankTransactionId() {
        Invoice invoice = unpaidInvoice();
        BankTransferPayment payment = pendingPayment(invoice);
        when(payments.findByIdForUpdate(11L)).thenReturn(Optional.of(payment));
        when(payments.existsByBankTransactionId("MB-123456")).thenReturn(false);
        when(invoices.findByIdForUpdate(7L)).thenReturn(Optional.of(invoice));

        var result = service.confirmManual(11L, "MB-123456", 4L);

        assertEquals(BankTransferStatus.CONFIRMED, result.status());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        verify(audit).record(4L, "BANK_TRANSFER_MANUAL_CONFIRM", "INVOICE", 7L,
                "SUCCESS", "MB-123456", "STAFF_RECONCILIATION");
    }

    private Invoice unpaidInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(7L);
        invoice.setInvoiceCode("INV-0007");
        invoice.setTotalAmount(new BigDecimal("125000.00"));
        invoice.setPaymentStatus(PaymentStatus.UNPAID);
        return invoice;
    }

    private BankTransferPayment pendingPayment(Invoice invoice) {
        return BankTransferPayment.builder()
                .id(11L)
                .invoice(invoice)
                .transferCode("MC7ABCDEFGH")
                .amount(new BigDecimal("125000.00"))
                .bankId("970422")
                .status(BankTransferStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();
    }

    private BankTransferCallbackDto callback(BigDecimal amount) {
        return new BankTransferCallbackDto(
                "evt-1", "txn-123456", "MC7ABCDEFGH", amount,
                LocalDateTime.of(2026, 9, 23, 21, 0));
    }
}
