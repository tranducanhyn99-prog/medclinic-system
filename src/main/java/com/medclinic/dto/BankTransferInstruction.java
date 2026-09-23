package com.medclinic.dto;

import com.medclinic.enums.BankTransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankTransferInstruction(
        Long paymentId,
        Long invoiceId,
        String invoiceCode,
        String bankId,
        String bankName,
        String accountNo,
        String accountName,
        String transferCode,
        BigDecimal amount,
        String qrImageUrl,
        BankTransferStatus status,
        LocalDateTime expiresAt,
        LocalDateTime confirmedAt) {
}
