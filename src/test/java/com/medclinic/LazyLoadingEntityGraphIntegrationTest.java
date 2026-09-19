package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Repository calls finish their transactions before nested properties are read below. */
@SpringBootTest
@ActiveProfiles("dev")
class LazyLoadingEntityGraphIntegrationTest {
    @Autowired private UserRepository users;
    @Autowired private DoctorProfileRepository doctors;
    @Autowired private DoctorScheduleRepository schedules;
    @Autowired private TimeSlotRepository slots;
    @Autowired private AppointmentRepository appointments;
    @Autowired private MedicineRepository medicines;
    @Autowired private MedicalRecordRepository records;
    @Autowired private InvoiceRepository invoices;

    @Test
    void medicalHistoryAndInvoiceGraphsRemainUsableAfterRepositoryTransactionCloses() {
        DoctorProfile doctor = doctors.findAll().getFirst();
        User patient = users.findByUsername("patient").orElseThrow();
        LocalDate date = LocalDate.now().plusDays(90);
        DoctorSchedule schedule = schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date)
                .shift(ShiftType.MORNING).maxPatients(8).build());
        TimeSlot slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30)).status(SlotStatus.BOOKED).build());
        Appointment appointment = appointments.save(Appointment.builder().patient(patient).doctor(doctor)
                .appointmentDate(date).timeSlot(slot).bookingCode("LG-" + UUID.randomUUID().toString().substring(0, 8))
                .reason("Entity graph").status(AppointmentStatus.COMPLETED).build());
        Medicine medicine = medicines.findAll().getFirst();
        MedicalRecord record = MedicalRecord.builder().appointment(appointment).doctor(doctor).patient(patient)
                .symptoms("S").diagnosis("D").build();
        Prescription prescription = Prescription.builder().medicalRecord(record).build();
        PrescriptionItem item = PrescriptionItem.builder().prescription(prescription).medicine(medicine).quantity(1)
                .unitPriceAtTime(medicine.getUnitPrice()).dosage("1 viên").frequency("1 lần/ngày").build();
        prescription.setItems(List.of(item));
        record.setPrescription(prescription);
        records.saveAndFlush(record);
        Invoice invoice = invoices.saveAndFlush(Invoice.builder().appointment(appointment)
                .invoiceCode("LI-" + UUID.randomUUID().toString().substring(0, 8)).consultationFee(BigDecimal.ZERO)
                .medicineFee(medicine.getUnitPrice()).totalAmount(medicine.getUnitPrice())
                .paymentStatus(PaymentStatus.UNPAID).createdAt(LocalDateTime.now()).build());

        MedicalRecord detachedRecord = records.findByAppointmentId(appointment.getId()).orElseThrow();
        Invoice detachedInvoice = invoices.findById(invoice.getId()).orElseThrow();

        assertEquals(patient.getFullName(), detachedRecord.getPatient().getFullName());
        assertEquals(doctor.getUser().getFullName(), detachedRecord.getDoctor().getUser().getFullName());
        assertEquals(medicine.getName(), detachedRecord.getPrescription().getItems().getFirst().getMedicine().getName());
        assertEquals(patient.getFullName(), detachedInvoice.getAppointment().getPatient().getFullName());
        assertEquals(doctor.getUser().getFullName(), detachedInvoice.getAppointment().getDoctor().getUser().getFullName());
    }
}
