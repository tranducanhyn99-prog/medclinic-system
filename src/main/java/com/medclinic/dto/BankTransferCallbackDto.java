package com.medclinic.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankTransferCallbackDto(
        @NotBlank @Size(max = 100) String eventId,
        @NotBlank @Size(max = 100) String transactionId,
        @NotBlank @Pattern(regexp = "[A-Z0-9]{6,25}") String transferCode,
        @NotNull @DecimalMin("1") @Digits(integer = 12, fraction = 2) BigDecimal amount,
        LocalDateTime occurredAt) {
}
