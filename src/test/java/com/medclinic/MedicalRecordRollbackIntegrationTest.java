package com.medclinic;

import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("dev")
class MedicalRecordRollbackIntegrationTest {
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private DoctorProfileRepository doctorProfiles;
    @Autowired private DoctorScheduleRepository schedules;
    @Autowired private TimeSlotRepository slots;
    @Autowired private AppointmentRepository appointments;
    @Autowired private MedicalRecordRepository records;
    @Autowired private MedicineRepository medicines;
    @Autowired private InventoryTransactionRepository inventoryTransactions;
    @Autowired private UserRepository users;
    @MockBean private InvoiceService invoiceService;

    @Test
    @WithUserDetails("doctor")
    void invoiceFailureRollsBackRecordAppointmentAndMedicineStock() {
        DoctorProfile doctor = doctorProfiles.findByUserId(users.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
        User patient = users.findByUsername("patient").orElseThrow();
        LocalDate date = LocalDate.now().plusDays(70);
        DoctorSchedule schedule = schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date)
                .shift(ShiftType.MORNING).maxPatients(8).build());
        TimeSlot slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30)).status(SlotStatus.BOOKED).build());
        Appointment appointment = appointments.save(Appointment.builder().patient(patient).doctor(doctor).appointmentDate(date)
                .timeSlot(slot).bookingCode("RB-" + UUID.randomUUID().toString().substring(0, 8))
                .reason("Rollback").status(AppointmentStatus.IN_PROGRESS).build());
        Medicine medicine = medicines.findAll().getFirst();
        int initialStock = medicine.getStockQuantity();
        doThrow(new BusinessLogicException("invoice failure")).when(invoiceService).createInvoiceForAppointment(any(), any());

        assertThrows(BusinessLogicException.class, () -> medicalRecordService.saveMedicalRecordAndPrescription(
                appointment.getId(), doctor.getUser().getId(), "S", "D", null, null,
                List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1)
                        .dosage("1").frequency("1").instructions("").build())));

        assertTrue(records.findByAppointmentId(appointment.getId()).isEmpty());
        assertEquals(AppointmentStatus.IN_PROGRESS, appointments.findById(appointment.getId()).orElseThrow().getStatus());
        assertEquals(initialStock, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertTrue(inventoryTransactions.findAll().stream().noneMatch(tx ->
                tx.getType() == InventoryTransactionType.DISPENSE
                        && appointment.getId().equals(tx.getReferenceId())));
    }
}
