package com.medclinic;

import com.medclinic.dto.BankTransferCallbackDto;
import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.security.HmacSignatureVerifier;
import com.medclinic.service.BankTransferPaymentService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithUserDetails;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"app.payment.bank.enabled=true", "app.payment.bank.account-no=0123456789",
        "app.payment.bank.account-name=CONCURRENCY TEST", "app.payment.bank.callback-secret=0123456789abcdef0123456789abcdef"})
@ActiveProfiles("dev")
class BankPaymentConcurrencyIntegrationTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    @Autowired BankTransferPaymentService bank;
    @Autowired InvoiceService invoiceService;
    @Autowired InvoiceRepository invoices;
    @Autowired BankTransferPaymentRepository payments;
    @Autowired AppointmentRepository appointments;
    @Autowired DoctorProfileRepository doctors;
    @Autowired UserRepository users;
    @Autowired TimeSlotRepository slots;
    @Autowired MedicalRecordService examinations;
    @Autowired MedicalRecordRepository records;
    @Autowired MedicineRepository medicines;

    @Test
    @WithUserDetails("doctor")
    void prescribedMedicineProducesIntegralInvoiceAndQrWithoutConfirmingPayment() {
        var appointment = examination();
        var medicine = medicines.saveAndFlush(Medicine.builder().name("Money pipeline " + UUID.randomUUID())
                .unit("viên").unitPrice(new BigDecimal("1500.00")).stockQuantity(10).isActive(true).build());
        examinations.saveMedicalRecordAndPrescription(appointment.getId(), appointment.getDoctor().getUser().getId(),
                "DEMO", "DEMO", null, null, List.of(new PrescriptionItemDto(medicine.getId(), 2, "DEMO", "DEMO", null)));
        var invoice = invoices.findByAppointmentId(appointment.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("3000").compareTo(invoice.getMedicineFee()));
        assertEquals(0, invoice.getConsultationFee().add(new BigDecimal("3000")).compareTo(invoice.getTotalAmount()));
        var instruction = bank.createInstruction(invoice.getId());
        assertTrue(instruction.qrImageUrl().contains("amount=" + invoice.getTotalAmount().longValueExact()));
        assertEquals(PaymentStatus.UNPAID, invoices.findById(invoice.getId()).orElseThrow().getPaymentStatus());
        assertEquals(8, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    @WithUserDetails("doctor")
    void invoiceOverflowRollsBackMedicalRecordAndStockWithoutRounding() {
        var appointment = examination();
        var medicine = medicines.saveAndFlush(Medicine.builder().name("Money overflow " + UUID.randomUUID())
                .unit("viên").unitPrice(new BigDecimal("9999999999.00")).stockQuantity(10).isActive(true).build());
        assertThrows(BusinessLogicException.class, () -> examinations.saveMedicalRecordAndPrescription(
                appointment.getId(), appointment.getDoctor().getUser().getId(), "DEMO", "DEMO", null, null,
                List.of(new PrescriptionItemDto(medicine.getId(), 2, "DEMO", "DEMO", null))));
        assertTrue(records.findByAppointmentId(appointment.getId()).isEmpty());
        assertTrue(invoices.findByAppointmentId(appointment.getId()).isEmpty());
        assertEquals(10, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertEquals(AppointmentStatus.IN_PROGRESS, appointments.findById(appointment.getId()).orElseThrow().getStatus());
    }

    @Test
    void expiredInstructionRegenerationAndManualConfirmationFinishWithoutDeadlock() throws Exception {
        Invoice invoice = invoice();
        var instruction = bank.createInstruction(invoice.getId());
        var expired = payments.findById(instruction.paymentId()).orElseThrow();
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        payments.saveAndFlush(expired);
        var results = race(() -> bank.createInstruction(invoice.getId()),
                () -> bank.confirmManual(instruction.paymentId(), "manual-" + UUID.randomUUID(), null));
        assertTrue(results[1]);
        assertPaidOnce(invoice.getId());
    }

    @Test
    void simultaneousDuplicateCallbacksAreIdempotent() throws Exception {
        Invoice invoice = invoice();
        var instruction = bank.createInstruction(invoice.getId());
        var callback = new BankTransferCallbackDto("event-" + UUID.randomUUID(), "txn-" + UUID.randomUUID(),
                instruction.transferCode(), instruction.amount(), null);
        String signature = HmacSignatureVerifier.signSha256(SECRET, BankTransferPaymentService.canonical(callback));
        var results = race(() -> bank.confirmCallback(callback, signature), () -> bank.confirmCallback(callback, signature));
        assertTrue(results[0]);
        assertTrue(results[1]);
        assertPaidOnce(invoice.getId());
    }

    @Test
    void callbackAndCashCannotBothConfirmInvoice() throws Exception {
        Invoice invoice = invoice();
        var instruction = bank.createInstruction(invoice.getId());
        var callback = new BankTransferCallbackDto("event-" + UUID.randomUUID(), "txn-" + UUID.randomUUID(),
                instruction.transferCode(), instruction.amount(), null);
        String signature = HmacSignatureVerifier.signSha256(SECRET, BankTransferPaymentService.canonical(callback));
        var results = race(() -> bank.confirmCallback(callback, signature),
                () -> invoiceService.processPayment(invoice.getId(), PaymentMethod.CASH));
        assertNotEquals(results[0], results[1]);
        assertEquals(PaymentStatus.PAID, invoices.findById(invoice.getId()).orElseThrow().getPaymentStatus());
        long confirmed = payments.findAll().stream().filter(p -> p.getInvoice().getId().equals(invoice.getId())
                && p.getStatus() == BankTransferStatus.CONFIRMED).count();
        assertEquals(results[0] ? 1 : 0, confirmed);
    }

    private void assertPaidOnce(Long invoiceId) {
        assertEquals(PaymentStatus.PAID, invoices.findById(invoiceId).orElseThrow().getPaymentStatus());
        assertEquals(1, payments.findAll().stream().filter(p -> p.getInvoice().getId().equals(invoiceId)
                && p.getStatus() == BankTransferStatus.CONFIRMED).count());
    }

    private boolean[] race(Callable<?> first, Callable<?> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            Future<Boolean> a = pool.submit(() -> run(first, ready, start));
            Future<Boolean> b = pool.submit(() -> run(second, ready, start));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return new boolean[]{a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)};
        } finally { pool.shutdownNow(); }
    }

    private boolean run(Callable<?> task, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        assertTrue(start.await(10, TimeUnit.SECONDS));
        try { task.call(); return true; }
        catch (BusinessLogicException expected) { return false; }
    }

    private Invoice invoice() {
        var doctor = doctors.findAll().getFirst();
        var slot = slots.findAll().getFirst();
        String token = UUID.randomUUID().toString().substring(0, 12);
        var appointment = appointments.saveAndFlush(Appointment.builder().patient(users.findByUsername("patient").orElseThrow())
                .doctor(doctor).timeSlot(slot).appointmentDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .bookingCode("PAY-" + token).status(AppointmentStatus.COMPLETED).build());
        return invoices.saveAndFlush(Invoice.builder().appointment(appointment).invoiceCode("PAY-" + token)
                .consultationFee(new BigDecimal("125000.00")).medicineFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("125000.00")).paymentStatus(PaymentStatus.UNPAID).build());
    }

    private Appointment examination() {
        var doctor = doctors.findByUserId(users.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
        var day = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var slot = slots.findAvailableSlotsByDoctorAndDate(doctor.getId(), day, SlotStatus.AVAILABLE).getFirst();
        return appointments.saveAndFlush(Appointment.builder().patient(users.findByUsername("patient").orElseThrow())
                .doctor(doctor).timeSlot(slot).appointmentDate(day)
                .bookingCode("PIPE-" + UUID.randomUUID().toString().substring(0, 12))
                .status(AppointmentStatus.IN_PROGRESS).build());
    }
}
