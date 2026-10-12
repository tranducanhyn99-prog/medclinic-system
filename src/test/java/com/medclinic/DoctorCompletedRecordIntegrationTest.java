package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.repository.*;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:doctor-records-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@WithUserDetails("doctor")
class DoctorCompletedRecordIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired DoctorProfileRepository doctors;
    @Autowired DoctorScheduleRepository schedules;
    @Autowired TimeSlotRepository slots;
    @Autowired AppointmentRepository appointments;
    @Autowired MedicalRecordRepository records;
    @Autowired MedicineRepository medicines;
    @Autowired InvoiceRepository invoices;
    @Autowired InventoryTransactionRepository movements;

    private DoctorProfile owner() {
        return doctors.findByUserId(users.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
    }

    private MedicalRecord record(DoctorProfile doctor, AppointmentStatus status, int daysAgo) {
        var date = LocalDate.now().minusDays(daysAgo);
        var schedule = schedules.findByDoctorIdAndWorkDateAndShift(doctor.getId(), date, ShiftType.MORNING)
                .orElseGet(() -> schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date)
                        .shift(ShiftType.MORNING).maxPatients(50).build()));
        var slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30)).status(SlotStatus.BOOKED).build());
        var appointment = appointments.save(Appointment.builder().doctor(doctor)
                .patient(users.findByUsername("patient").orElseThrow()).timeSlot(slot).appointmentDate(date)
                .bookingCode("RECORD-" + UUID.randomUUID().toString().substring(0, 8)).reason("Demo bệnh án")
                .status(status).build());
        return records.saveAndFlush(MedicalRecord.builder().appointment(appointment).doctor(doctor)
                .patient(appointment.getPatient()).symptoms("Đau đầu\n<script>alert(1)</script>")
                .diagnosis("Kết quả khám mẫu").build());
    }

    @Test void readsPastRecordAndSnapshotPriceWithoutChangingClinicalOrBillingData() throws Exception {
        var record = record(owner(), AppointmentStatus.COMPLETED, 30);
        var medicine = medicines.findAll().getFirst();
        var prescription = Prescription.builder().medicalRecord(record).build();
        prescription.setItems(List.of(PrescriptionItem.builder().prescription(prescription).medicine(medicine)
                .quantity(2).unitPriceAtTime(new BigDecimal("1234")).dosage("1 viên / lần")
                .frequency("2 lần / ngày").instructions("Sau ăn").build()));
        record.setPrescription(prescription);
        records.saveAndFlush(record);
        long invoiceCount = invoices.count(), movementCount = movements.count();
        int stock = medicine.getStockQuantity();
        var response = mvc.perform(get("/doctor/records/{id}", record.getAppointment().getId()))
                .andExpect(status().isOk()).andExpect(view().name("doctor/record-detail"))
                .andExpect(content().string(containsString("1 234 đ")))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("Không có lời dặn bổ sung.")))
                .andExpect(content().string(not(containsString("examinationForm"))))
                .andReturn().getResponse();
        UxReviewFixture.write("doctor-record-detail", response);
        assertEquals(invoiceCount, invoices.count());
        assertEquals(movementCount, movements.count());
        assertEquals(stock, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertEquals(AppointmentStatus.COMPLETED, appointments.findById(record.getAppointment().getId()).orElseThrow().getStatus());
    }

    @Test void listIsScopedPagedAndOrderedAndDoesNotShowUnfinishedRecords() throws Exception {
        var owner = owner();
        for (int day = 1; day <= 26; day++) record(owner, AppointmentStatus.COMPLETED, day);
        var otherDoctor = doctors.findAll().stream().filter(d -> !d.getId().equals(owner.getId())).findFirst().orElseThrow();
        var privateRecord = record(otherDoctor, AppointmentStatus.COMPLETED, 0);
        var unfinished = record(owner, AppointmentStatus.IN_PROGRESS, 0);
        var response = mvc.perform(get("/doctor/records"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("26 bệnh án")))
                .andExpect(content().string(containsString("Trang sau")))
                .andExpect(content().string(not(containsString(privateRecord.getAppointment().getBookingCode()))))
                .andExpect(content().string(not(containsString(unfinished.getAppointment().getBookingCode()))))
                .andReturn();
        var page = (org.springframework.data.domain.Page<?>) response.getModelAndView().getModel().get("records");
        assertEquals(25, page.getContent().size());
        assertEquals(LocalDate.now().minusDays(1), ((MedicalRecord) page.getContent().getFirst()).getAppointment().getAppointmentDate());
        UxReviewFixture.write("doctor-records", response.getResponse());
        mvc.perform(get("/doctor/records?page=1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Trang 2 / 2")));
        mvc.perform(get("/doctor/records?page=-1")).andExpect(status().isOk());
        mvc.perform(get("/doctor/records?page=999")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Trang này không có bệnh án")));
        mvc.perform(get("/doctor/records?page=2147483647")).andExpect(status().isOk());
    }

    @Test void rejectsOtherDoctorAndNonDoctorRolesAndMissingOrUnfinishedRecord() throws Exception {
        var owner = owner();
        var other = doctors.findAll().stream().filter(d -> !d.getId().equals(owner.getId())).findFirst().orElseThrow();
        var record = record(owner, AppointmentStatus.COMPLETED, 15);
        mvc.perform(get("/doctor/records/{id}", record.getAppointment().getId())
                        .with(user(new CustomUserDetails(other.getUser()))))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"))
                .andExpect(flash().attributeExists("errorMessage"));
        for (String username : List.of("patient", "staff", "admin")) {
            var principal = new CustomUserDetails(users.findByUsername(username).orElseThrow());
            mvc.perform(get("/doctor/records/{id}", record.getAppointment().getId()).with(user(principal)))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/doctor/records").with(user(principal))).andExpect(status().isForbidden());
        }
        mvc.perform(get("/doctor/records/9223372036854775807")).andExpect(status().isNotFound());
        var unfinished = record(owner, AppointmentStatus.IN_PROGRESS, 1);
        mvc.perform(get("/doctor/records/{id}", unfinished.getAppointment().getId()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"));
    }

    @Test void rendersEmptyListAndRecordWithoutMedication() throws Exception {
        var empty = mvc.perform(get("/doctor/records")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Chưa có bệnh án đã hoàn tất"))).andReturn().getResponse();
        UxReviewFixture.write("doctor-records-empty", empty);
        var record = record(owner(), AppointmentStatus.COMPLETED, 3);
        mvc.perform(get("/doctor/records/{id}", record.getAppointment().getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ca khám này không kê thuốc.")));
    }
}
