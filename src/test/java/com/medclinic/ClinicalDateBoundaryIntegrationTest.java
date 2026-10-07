package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.repository.*;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.demo.early-clinical-processing=false")
@AutoConfigureMockMvc @ActiveProfiles("dev") @Transactional
class ClinicalDateBoundaryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired Clock clock;
    @Autowired AppointmentRepository appointments;
    @Autowired DoctorProfileRepository doctors;
    @Autowired UserRepository users;
    @Autowired DoctorScheduleRepository schedules;
    @Autowired TimeSlotRepository slots;
    @Autowired MedicalRecordRepository records;
    @Autowired InvoiceRepository invoices;
    @Autowired MedicineRepository medicines;
    @Autowired InventoryTransactionRepository inventory;

    @ParameterizedTest @ValueSource(ints = {-1, 1}) @WithUserDetails("staff")
    void directCheckInDoesNotIssueAQueueNumberForAnotherDay(int offset) throws Exception {
        var a = fixture(offset, AppointmentStatus.CONFIRMED);
        mvc.perform(post("/staff/reception/check-in/{id}", a.getId()).with(csrf()).header("Referer", "/staff/reception"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/staff/reception"))
                .andExpect(flash().attributeExists("errorMessage"));
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
        assertNull(a.getQueueSequence());
    }

    @ParameterizedTest @ValueSource(ints = {-1, 1}) @WithUserDetails("staff")
    void forgedWalkInRequestDoesNotCreateAPatientOrBookASlot(int offset) throws Exception {
        var a = fixture(offset, AppointmentStatus.CONFIRMED);
        var slot = a.getTimeSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slots.saveAndFlush(slot);
        long userCount = users.count(), appointmentCount = appointments.count();
        mvc.perform(post("/staff/walk-in").with(csrf()).header("Referer", "/staff/walk-in")
                        .param("fullName", "DEMO REJECTED").param("phone", "0900000020")
                        .param("doctorId", a.getDoctor().getId().toString()).param("date", a.getAppointmentDate().toString())
                        .param("slotId", slot.getId().toString()).param("reason", "DEMO"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/staff/walk-in"))
                .andExpect(flash().attributeExists("errorMessage"));
        assertEquals(userCount, users.count());
        assertEquals(appointmentCount, appointments.count());
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
    }

    @ParameterizedTest @ValueSource(ints = {-1, 1}) @WithUserDetails("doctor")
    void legacyCheckedInAppointmentCannotStartOrOpenTheFormOnAnotherDay(int offset) throws Exception {
        var a = fixture(offset, AppointmentStatus.CHECKED_IN);
        mvc.perform(post("/doctor/start-examination/{id}", a.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"))
                .andExpect(flash().attributeExists("errorMessage"));
        mvc.perform(get("/doctor/examine/{id}", a.getId()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"));
        assertEquals(AppointmentStatus.CHECKED_IN, a.getStatus());
        assertTrue(records.findByAppointmentId(a.getId()).isEmpty());
    }

    @ParameterizedTest @ValueSource(ints = {-1, 1}) @WithUserDetails("doctor")
    void forgedSaveForLegacyInProgressAppointmentCannotTouchStockRecordsOrInvoices(int offset) throws Exception {
        var a = fixture(offset, AppointmentStatus.IN_PROGRESS);
        var medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        long recordCount = records.count(), invoiceCount = invoices.count(), inventoryCount = inventory.count();
        mvc.perform(post("/doctor/examine/{id}", a.getId()).with(csrf())
                        .param("symptoms", "DEMO").param("diagnosis", "DEMO")
                        .param("medicineIds", medicine.getId().toString()).param("quantities", "1")
                        .param("dosages", "1 viên").param("frequencies", "1 lần/ngày"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"));
        assertEquals(AppointmentStatus.IN_PROGRESS, a.getStatus());
        assertEquals(stock, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertEquals(recordCount, records.count());
        assertEquals(invoiceCount, invoices.count());
        assertEquals(inventoryCount, inventory.count());
    }

    private Appointment fixture(int offset, AppointmentStatus status) {
        var doctor = doctors.findByUserId(users.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
        LocalDate date = LocalDate.now(clock).plusDays(offset);
        var schedule = schedules.findByDoctorIdAndWorkDateAndShift(doctor.getId(), date, ShiftType.MORNING)
                .orElseGet(() -> schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date).shift(ShiftType.MORNING).maxPatients(8).build()));
        var slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30)).status(SlotStatus.BOOKED).build());
        return appointments.saveAndFlush(Appointment.builder().patient(users.findByUsername("patient").orElseThrow())
                .doctor(doctor).appointmentDate(date).timeSlot(slot).reason("DEMO date boundary")
                .bookingCode("DATE-" + UUID.randomUUID().toString().substring(0, 8)).status(status).build());
    }
}
