package com.medclinic;

import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.Appointment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Invoice;
import com.medclinic.entity.MedicalRecord;
import com.medclinic.entity.Medicine;
import com.medclinic.entity.TimeSlot;
import com.medclinic.entity.User;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.enums.SlotStatus;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.MedicalRecordService;
import com.medclinic.service.PdfExportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false"})
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
class SqlServerReleaseAcceptanceEndToEndIntegrationTest {
    @Autowired private AppointmentService appointmentService;
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private PdfExportService pdfExportService;
    @Autowired private UserRepository users;
    @Autowired private DoctorProfileRepository doctors;
    @Autowired private TimeSlotRepository slots;
    @Autowired private MedicineRepository medicines;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean private SimpMessagingTemplate messagingTemplate;

    @Test
    @WithUserDetails("admin")
    void completeOutpatientJourneyPersistsEveryInvariantAndExportsDocuments() {
        clearInvocations(messagingTemplate);
        User patient = users.findByUsername("patient").orElseThrow();
        User doctorUser = users.findByUsername("doctor").orElseThrow();
        User admin = users.findByUsername("admin").orElseThrow();
        DoctorProfile doctor = doctors.findByUserId(doctorUser.getId()).orElseThrow();
        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        TimeSlot slot = slots.findAvailableSlotsByDoctorAndDate(
                doctor.getId(), appointmentDate, SlotStatus.AVAILABLE).getFirst();
        Medicine medicine = medicines.findByIsActiveTrue().stream()
                .filter(item -> item.getStockQuantity() >= 2)
                .findFirst().orElseThrow();
        int initialStock = medicine.getStockQuantity();
        BigDecimal expectedMedicineFee = medicine.getUnitPrice().multiply(BigDecimal.valueOf(2));
        Appointment booked = appointmentService.bookAppointment(
                patient.getId(), doctor.getId(), appointmentDate, slot.getId(), "Release acceptance E2E");
        assertNotNull(booked.getId());
        assertTrue(booked.getBookingCode().startsWith("MED-"));
        assertEquals(AppointmentStatus.CONFIRMED, booked.getStatus());

        Appointment checkedIn = appointmentService.checkInAppointment(booked.getId());
        assertEquals(AppointmentStatus.CHECKED_IN, checkedIn.getStatus());
        assertNotNull(checkedIn.getQueueSequence());
        assertTrue(checkedIn.getQueueSequence() > 0);
        assertTrue(checkedIn.getQueueNumber().startsWith("STT-"));

        Appointment inProgress = medicalRecordService.startExamination(
                booked.getId(), doctorUser.getId());
        assertEquals(AppointmentStatus.IN_PROGRESS, inProgress.getStatus());

        PrescriptionItemDto prescriptionItem = PrescriptionItemDto.builder()
                .medicineId(medicine.getId())
                .quantity(2)
                .dosage("1 viên")
                .frequency("2 lần/ngày")
                .instructions("Uống sau ăn")
                .build();
        MedicalRecord record = medicalRecordService.saveMedicalRecordAndPrescription(
                booked.getId(), doctorUser.getId(), "Sốt và đau đầu", "Cảm cúm",
                "Nghỉ ngơi và uống đủ nước", appointmentDate.plusDays(7), List.of(prescriptionItem));
        assertNotNull(record.getId());
        Invoice invoice = invoiceService.findByAppointmentId(booked.getId());
        assertEquals(PaymentStatus.UNPAID, invoice.getPaymentStatus());
        assertEquals(0, expectedMedicineFee.compareTo(invoice.getMedicineFee()));
        BigDecimal expectedTotal = doctor.getConsultationFee().add(expectedMedicineFee);
        assertEquals(0, expectedTotal.compareTo(invoice.getTotalAmount()));

        Invoice paid = invoiceService.processPayment(invoice.getId(), PaymentMethod.CASH);
        assertEquals(PaymentStatus.PAID, paid.getPaymentStatus());
        assertEquals(PaymentMethod.CASH, paid.getPaymentMethod());
        assertNotNull(paid.getPaidAt());

        List<MedicalRecord> history = medicalRecordService.getPatientMedicalHistory(patient.getId());
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(item -> item.getId().equals(record.getId())));

        byte[] prescriptionPdf = pdfExportService.exportPrescriptionPdf(
                booked.getId(), admin.getId(), "ROLE_ADMIN");
        byte[] invoicePdf = pdfExportService.exportInvoicePdf(
                invoice.getId(), admin.getId(), "ROLE_ADMIN");
        assertPdf(prescriptionPdf);
        assertPdf(invoicePdf);

        assertEquals("COMPLETED", jdbcTemplate.queryForObject(
                "select status from appointments where id = ?", String.class, booked.getId()));
        assertEquals("BOOKED", jdbcTemplate.queryForObject(
                "select status from time_slots where id = ?", String.class, slot.getId()));
        assertEquals(initialStock - 2, jdbcTemplate.queryForObject(
                "select stock_quantity from medicines where id = ?", Integer.class, medicine.getId()));
        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from medical_records where appointment_id = ?", Integer.class, booked.getId()));
        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from prescriptions p join medical_records m on p.medical_record_id = m.id " +
                        "where m.appointment_id = ?", Integer.class, booked.getId()));
        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from prescription_items pi join prescriptions p on pi.prescription_id = p.id " +
                        "join medical_records m on p.medical_record_id = m.id where m.appointment_id = ?",
                Integer.class, booked.getId()));
        assertEquals("PAID", jdbcTemplate.queryForObject(
                "select payment_status from invoices where appointment_id = ?", String.class, booked.getId()));
        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from appointments where appointment_date = ? and queue_sequence = ?",
                Integer.class, Date.valueOf(appointmentDate), checkedIn.getQueueSequence()));

        BigDecimal snapshotPrice = jdbcTemplate.queryForObject(
                "select pi.unit_price_at_time from prescription_items pi " +
                        "join prescriptions p on pi.prescription_id = p.id " +
                        "join medical_records m on p.medical_record_id = m.id where m.appointment_id = ?",
                BigDecimal.class, booked.getId());
        assertNotNull(snapshotPrice);
        assertEquals(0, medicine.getUnitPrice().compareTo(snapshotPrice));
        verify(messagingTemplate, times(3)).convertAndSendToUser(
                eq("doctor"), eq("/queue/doctor"), any());
    }

    private void assertPdf(byte[] pdf) {
        assertNotNull(pdf);
        assertTrue(pdf.length > 1000);
        String header = new String(pdf, 0, Math.min(pdf.length, 5), StandardCharsets.US_ASCII);
        assertTrue(header.startsWith("%PDF"));
    }
}
