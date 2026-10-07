package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.repository.*;
import com.medclinic.security.CustomUserDetails;
import java.time.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev") @Transactional
class ClinicalDemoWorkflowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired Clock clock;
    @Autowired UserRepository users;
    @Autowired DoctorProfileRepository doctors;
    @Autowired DoctorScheduleRepository schedules;
    @Autowired TimeSlotRepository slots;
    @Autowired AppointmentRepository appointments;
    @Autowired MedicineRepository medicines;
    @Autowired MedicalRecordRepository records;
    @Autowired InvoiceRepository invoices;

    @ParameterizedTest @ValueSource(ints = {1, 14})
    void futureBookingCanCompleteTheDefenseJourneyWithoutChangingItsDate(int offset) throws Exception {
        LocalDate bookedDate = LocalDate.now(clock).plusDays(offset);
        var doctor = doctor();
        var slot = slot(doctor, bookedDate);
        var result = mvc.perform(post("/patient/booking").with(user(principal("patient"))).with(csrf())
                        .param("doctorId", doctor.getId().toString()).param("specialtyId", doctor.getSpecialty().getId().toString())
                        .param("date", bookedDate.toString()).param("slotId", slot.getId().toString())
                        .param("reason", "DEMO full journey " + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection()).andReturn();
        String detail = result.getResponse().getRedirectedUrl();
        assertNotNull(detail);
        long id = Long.parseLong(detail.substring(detail.lastIndexOf('/') + 1));
        var appointment = appointments.findById(id).orElseThrow();

        mvc.perform(get("/staff/reception").param("keyword", appointment.getBookingCode()).with(user(principal("staff"))))
                .andExpect(status().isOk()).andExpect(content().string(containsString(appointment.getBookingCode())));
        mvc.perform(get("/staff/reception/search").param("keyword", appointment.getBookingCode()).with(user(principal("staff"))))
                .andExpect(status().isOk()).andExpect(content().string(containsString(appointment.getBookingCode())));
        mvc.perform(post("/staff/reception/check-in/{id}", id).with(user(principal("staff"))).with(csrf()))
                .andExpect(redirectedUrl("/staff/reception")).andExpect(flash().attributeExists("successMessage"));
        assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
        assertNotNull(appointment.getQueueSequence());
        mvc.perform(get("/doctor/desk").with(user(principal("doctor"))))
                .andExpect(status().isOk()).andExpect(content().string(containsString(appointment.getBookingCode())));
        mvc.perform(post("/doctor/start-examination/{id}", id).with(user(principal("doctor"))).with(csrf()))
                .andExpect(redirectedUrl("/doctor/examine/" + id));
        mvc.perform(get("/doctor/examine/{id}", id).with(user(principal("doctor"))))
                .andExpect(status().isOk()).andExpect(content().string(containsString("examinationForm")));
        var medicine = medicines.findByIsActiveTrue().stream().filter(m -> m.getStockQuantity() > 0).findFirst().orElseThrow();
        int stockBefore = medicine.getStockQuantity();
        mvc.perform(post("/doctor/examine/{id}", id).with(user(principal("doctor"))).with(csrf())
                        .param("symptoms", "DEMO triệu chứng").param("diagnosis", "DEMO chẩn đoán")
                        .param("medicineIds", medicine.getId().toString()).param("quantities", "1")
                        .param("dosages", "1 viên").param("frequencies", "1 lần/ngày"))
                .andExpect(redirectedUrl("/doctor/desk")).andExpect(flash().attributeExists("successMessage"));
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        assertEquals(bookedDate, appointment.getAppointmentDate());
        assertEquals(stockBefore - 1, medicine.getStockQuantity());
        assertTrue(records.findByAppointmentId(id).isPresent());
        var invoice = invoices.findByAppointmentId(id).orElseThrow();
        mvc.perform(get("/staff/billing").with(user(principal("staff"))))
                .andExpect(status().isOk()).andExpect(content().string(containsString(invoice.getInvoiceCode())));
        mvc.perform(post("/staff/billing/pay/{id}", invoice.getId()).param("method", "CASH")
                        .with(user(principal("staff"))).with(csrf()))
                .andExpect(redirectedUrl("/staff/billing"));
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        mvc.perform(get("/export/invoice/{id}", invoice.getId()).with(user(principal("staff"))))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
    }

    @ParameterizedTest @ValueSource(ints = {-1, 15})
    void invalidDemoDateCannotEnterReceptionOrWriteClinicalData(int offset) throws Exception {
        LocalDate date = LocalDate.now(clock).plusDays(offset);
        var doctor = doctor();
        var appointment = appointments.saveAndFlush(Appointment.builder().patient(principal("patient").getUser())
                .doctor(doctor).appointmentDate(date).timeSlot(slot(doctor, date))
                .bookingCode("DEMO-" + UUID.randomUUID().toString().substring(0, 8))
                .reason("DEMO rejected date").status(AppointmentStatus.CONFIRMED).build());
        long recordCount = records.count(), invoiceCount = invoices.count();
        mvc.perform(post("/staff/reception/check-in/{id}", appointment.getId()).with(user(principal("staff"))).with(csrf())
                        .header("Referer", "/staff/reception"))
                .andExpect(flash().attributeExists("errorMessage"));
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertNull(appointment.getQueueSequence());
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointments.saveAndFlush(appointment);
        mvc.perform(post("/doctor/examine/{id}", appointment.getId()).with(user(principal("doctor"))).with(csrf())
                        .param("symptoms", "DEMO").param("diagnosis", "DEMO"))
                .andExpect(redirectedUrl("/doctor/desk"));
        assertEquals(recordCount, records.count());
        assertEquals(invoiceCount, invoices.count());
        assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
        mvc.perform(get("/doctor/desk").with(user(principal("doctor"))))
                .andExpect(status().isOk()).andExpect(model().attribute("todayQueue", org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(appointment.getId()))))));
    }

    private CustomUserDetails principal(String username) {
        return new CustomUserDetails(users.findByUsername(username).orElseThrow());
    }

    private DoctorProfile doctor() {
        return doctors.findByUserId(principal("doctor").getId()).orElseThrow();
    }

    private TimeSlot slot(DoctorProfile doctor, LocalDate date) {
        var schedule = schedules.findByDoctorIdAndWorkDateAndShift(doctor.getId(), date, ShiftType.MORNING)
                .orElseGet(() -> schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date)
                        .shift(ShiftType.MORNING).maxPatients(8).build()));
        return slots.saveAndFlush(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30)).status(SlotStatus.AVAILABLE).build());
    }
}
