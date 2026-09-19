package com.medclinic;

import com.medclinic.exception.BusinessLogicException;
import com.medclinic.service.impl.InvoiceCodeReservationCoordinator;
import com.medclinic.service.impl.InvoiceCodeReservationWriter;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceCodeReservationCoordinatorTest {
    @Test
    void retriesAfterUniqueViolationFromIndependentReservationTransaction() {
        InvoiceCodeReservationWriter writer = mock(InvoiceCodeReservationWriter.class);
        doThrow(new DataIntegrityViolationException("duplicate invoice code")).doNothing().when(writer).reserve(anyString());
        InvoiceCodeReservationCoordinator coordinator = new InvoiceCodeReservationCoordinator(writer);
        AtomicInteger calls = new AtomicInteger();

        assertEquals("INV-second", coordinator.reserveUniqueCode(() -> calls.getAndIncrement() == 0 ? "INV-first" : "INV-second"));
        verify(writer).reserve("INV-first");
        verify(writer).reserve("INV-second");
    }

    @Test
    void exhaustsReservationsWithBusinessErrorForOuterTransactionRollback() {
        InvoiceCodeReservationWriter writer = mock(InvoiceCodeReservationWriter.class);
        doThrow(new DataIntegrityViolationException("duplicate invoice code")).when(writer).reserve(anyString());
        InvoiceCodeReservationCoordinator coordinator = new InvoiceCodeReservationCoordinator(writer);

        assertThrows(BusinessLogicException.class, () -> coordinator.reserveUniqueCode(() -> "INV-collision"));
        verify(writer, times(InvoiceCodeReservationCoordinator.MAX_ATTEMPTS)).reserve("INV-collision");
    }
}
