package com.medclinic;

import com.medclinic.entity.Appointment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Invoice;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.impl.InvoiceCodeReservationCoordinator;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.BusinessAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock InvoiceRepository invoiceRepository;
    @Mock AppointmentRepository appointmentRepository;    @Mock InvoiceCodeReservationCoordinator coordinator;
    @Mock BusinessAuditService auditService;
    @InjectMocks InvoiceService service;

    private Appointment appointment(BigDecimal consultationFee) {
        DoctorProfile doctor = new DoctorProfile();
        doctor.setConsultationFee(consultationFee);
        Appointment appointment = new Appointment();
        appointment.setId(1L); appointment.setDoctor(doctor);
        return appointment;
    }

    private Invoice invoice(long id, PaymentStatus status) {
        Invoice invoice = new Invoice();
        invoice.setId(id); invoice.setPaymentStatus(status);
        return invoice;
    }

    @Test
    void createsInvoiceAndNormalizesNullFees() {
        Appointment appointment = appointment(new BigDecimal("100000"));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(invoiceRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(coordinator.reserveUniqueCode()).thenReturn("INV-001");
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice created = service.createInvoiceForAppointment(1L, new BigDecimal("25000"));
        assertEquals("INV-001", created.getInvoiceCode());
        assertEquals(new BigDecimal("125000"), created.getTotalAmount());
        assertEquals(PaymentStatus.UNPAID, created.getPaymentStatus());
        assertNotNull(created.getCreatedAt());

        Appointment noFeeAppointment = appointment(null);
        when(appointmentRepository.findById(2L)).thenReturn(Optional.of(noFeeAppointment));
        when(invoiceRepository.findByAppointmentId(2L)).thenReturn(Optional.empty());
        when(coordinator.reserveUniqueCode()).thenReturn("INV-002");
        Invoice noFee = service.createInvoiceForAppointment(2L, null);
        assertEquals(BigDecimal.ZERO, noFee.getConsultationFee());
        assertEquals(BigDecimal.ZERO, noFee.getMedicineFee());
        assertEquals(BigDecimal.ZERO, noFee.getTotalAmount());
    }

    @Test
    void rejectsMissingAppointmentAndDuplicateInvoice() {
        when(appointmentRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.createInvoiceForAppointment(9L, BigDecimal.ZERO));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment(BigDecimal.ONE)));
        when(invoiceRepository.findByAppointmentId(1L)).thenReturn(Optional.of(invoice(3L, PaymentStatus.UNPAID)));
        assertThrows(BusinessLogicException.class,
                () -> service.createInvoiceForAppointment(1L, BigDecimal.ZERO));
    }

    @Test
    void processesCashPaymentAndRejectsInvalidStatesOrMethods() {
        Invoice unpaid = invoice(1L, PaymentStatus.UNPAID);
        when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(unpaid));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice paid = service.processPayment(1L, PaymentMethod.CASH);
        assertEquals(PaymentStatus.PAID, paid.getPaymentStatus());
        assertEquals(PaymentMethod.CASH, paid.getPaymentMethod());
        assertNotNull(paid.getPaidAt());

        Invoice alreadyPaid = invoice(2L, PaymentStatus.PAID);
        when(invoiceRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(alreadyPaid));
        assertThrows(BusinessLogicException.class,
                () -> service.processPayment(2L, PaymentMethod.CASH));

        Invoice unpaidVnpay = invoice(3L, PaymentStatus.UNPAID);
        when(invoiceRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(unpaidVnpay));
        assertThrows(BusinessLogicException.class,
                () -> service.processPayment(3L, PaymentMethod.VNPAY));

        when(invoiceRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.processPayment(9L, PaymentMethod.CASH));
    }

    @Test
    void delegatesInvoiceQueriesAndReportsMissingRows() {
        Invoice invoice = invoice(1L, PaymentStatus.UNPAID);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.findByAppointmentId(10L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(invoice));

        assertSame(invoice, service.findById(1L));
        assertSame(invoice, service.findByAppointmentId(10L));
        assertEquals(List.of(invoice), service.findAll());

        when(invoiceRepository.findById(9L)).thenReturn(Optional.empty());
        when(invoiceRepository.findByAppointmentId(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(9L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByAppointmentId(99L));
    }
}
