package com.medclinic;

import com.medclinic.repository.InvoiceCodeReservationRepository;
import com.medclinic.service.impl.InvoiceCodeReservationCoordinator;
import com.medclinic.service.impl.InvoiceCodeReservationWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
class InvoiceCodeReservationIntegrationTest {
    @Autowired private InvoiceCodeReservationWriter writer;
    @Autowired private InvoiceCodeReservationCoordinator coordinator;
    @Autowired private InvoiceCodeReservationRepository repository;

    @Test
    void retriesAfterActualDatabaseUniqueConstraintViolation() {
        String token = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        String collision = "INV-COLLIDE-" + token;
        String resolved = "INV-RESOLVED-" + token;
        writer.reserve(collision);
        assertTrue(repository.existsById(collision));

        AtomicInteger attempts = new AtomicInteger();
        assertEquals(resolved, coordinator.reserveUniqueCode(
                () -> attempts.getAndIncrement() == 0 ? collision : resolved));

        assertEquals(2, attempts.get());
        assertTrue(repository.existsById(resolved));
    }
}
