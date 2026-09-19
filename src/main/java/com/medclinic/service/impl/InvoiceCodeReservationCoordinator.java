package com.medclinic.service.impl;

import com.medclinic.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Isolates a unique-key collision in its own transaction. The caller's clinical
 * transaction remains usable and only receives an exception after all attempts fail.
 */
@Service
@RequiredArgsConstructor
public class InvoiceCodeReservationCoordinator {
    public static final int MAX_ATTEMPTS = 8;
    private final InvoiceCodeReservationWriter writer;

    public String reserveUniqueCode() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        return reserveUniqueCode(() -> prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    /** Exposed for deterministic collision tests; production uses {@link #reserveUniqueCode()}. */
    public String reserveUniqueCode(Supplier<String> candidate) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = candidate.get();
            try {
                writer.reserve(code);
                return code;
            } catch (DataIntegrityViolationException ignored) {
                // The REQUIRES_NEW transaction rolled back; retry with another code.
            }
        }
        throw new BusinessLogicException("Không thể tạo mã hóa đơn duy nhất, vui lòng thử lại");
    }
}
