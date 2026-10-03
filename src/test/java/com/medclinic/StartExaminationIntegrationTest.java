package com.medclinic;

import com.medclinic.entity.Appointment;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.DoctorScheduleRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.repository.MedicalRecordRepository;
import com.medclinic.entity.DoctorSchedule;
import com.medclinic.entity.TimeSlot;
import com.medclinic.entity.User;
import com.medclinic.enums.ShiftType;
import com.medclinic.enums.SlotStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class StartExaminationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired private TimeSlotRepository timeSlotRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;

    @Test
    @WithMockUser(username = "doctor1", roles = "DOCTOR")
    public void postWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/doctor/start-examination/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("doctor")
    void postWithCsrfStartsCheckedInAppointment() throws Exception {
        var doctor = doctorProfileRepository.findByUserId(userRepository.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
        User patient = userRepository.findByUsername("patient").orElseThrow();
        LocalDate date = LocalDate.now().plusDays(60);
        DoctorSchedule schedule = doctorScheduleRepository.save(DoctorSchedule.builder().doctor(doctor)
                .workDate(date).shift(ShiftType.MORNING).maxPatients(8).build());
        TimeSlot slot = timeSlotRepository.save(TimeSlot.builder().schedule(schedule)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(8, 30)).status(SlotStatus.BOOKED).build());
        Appointment appointment = appointmentRepository.save(Appointment.builder().patient(patient).doctor(doctor)
                .appointmentDate(date).timeSlot(slot).reason("Kiểm tra").bookingCode("TEST-" + UUID.randomUUID().toString().substring(0, 8))
                .status(AppointmentStatus.CHECKED_IN).build());

        mockMvc.perform(post("/doctor/start-examination/{id}", appointment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/examine/" + appointment.getId()));
        org.junit.jupiter.api.Assertions.assertEquals(AppointmentStatus.IN_PROGRESS,
                appointmentRepository.findById(appointment.getId()).orElseThrow().getStatus());

        mockMvc.perform(get("/doctor/examine/{id}", appointment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"examinationForm\"")))
                .andExpect(content().string(containsString("id=\"medicineTable\"")))
                .andExpect(content().string(containsString("id=\"btnAddMedicine\"")))
                .andExpect(content().string(containsString("id=\"btnSubmitExamine\"")));
    }

    @Test
    @WithUserDetails("doctor")
    void completedAppointmentRedirectsWithoutOfferingANewExamination() throws Exception {
        var doctor = doctorProfileRepository.findByUserId(userRepository.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
        var patient = userRepository.findByUsername("patient").orElseThrow();
        LocalDate date = LocalDate.now().plusDays(62);
        var schedule = doctorScheduleRepository.save(DoctorSchedule.builder().doctor(doctor)
                .workDate(date).shift(ShiftType.MORNING).maxPatients(8).build());
        var slot = timeSlotRepository.save(TimeSlot.builder().schedule(schedule)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30)).status(SlotStatus.BOOKED).build());
        var appointment = appointmentRepository.save(Appointment.builder().patient(patient).doctor(doctor)
                .appointmentDate(date).timeSlot(slot).reason("Completed fixture")
                .bookingCode("CLOSED-" + UUID.randomUUID().toString().substring(0, 8))
                .status(AppointmentStatus.COMPLETED).build());
        mockMvc.perform(get("/doctor/examine/{id}", appointment.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/desk?closed=true"));
        mockMvc.perform(get("/doctor/desk?closed=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ca khám đã hoàn tất và bệnh án đã được lưu.")));
        mockMvc.perform(get("/doctor/examine/{id}", appointment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("staff").roles("STAFF")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("doctor")
    void invalidStartReturnsMessageAndLeavesAppointmentWithoutMedicalRecord() throws Exception {
        var doctor = doctorProfileRepository.findByUserId(userRepository.findByUsername("doctor").orElseThrow().getId()).orElseThrow();
        User patient = userRepository.findByUsername("patient").orElseThrow();
        LocalDate date = LocalDate.now().plusDays(61);
        DoctorSchedule schedule = doctorScheduleRepository.save(DoctorSchedule.builder().doctor(doctor)
                .workDate(date).shift(ShiftType.MORNING).maxPatients(8).build());
        TimeSlot slot = timeSlotRepository.save(TimeSlot.builder().schedule(schedule)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).status(SlotStatus.BOOKED).build());
        Appointment appointment = appointmentRepository.save(Appointment.builder().patient(patient).doctor(doctor)
                .appointmentDate(date).timeSlot(slot).reason("Chưa check-in").bookingCode("INVALID-" + UUID.randomUUID().toString().substring(0, 8))
                .status(AppointmentStatus.CONFIRMED).build());

        mockMvc.perform(post("/doctor/start-examination/{id}", appointment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/desk"))
                .andExpect(flash().attribute("errorMessage", "Chỉ có thể bắt đầu khám bệnh nhân đã check-in"));

        org.junit.jupiter.api.Assertions.assertEquals(AppointmentStatus.CONFIRMED,
                appointmentRepository.findById(appointment.getId()).orElseThrow().getStatus());
        assertTrue(medicalRecordRepository.findByAppointmentId(appointment.getId()).isEmpty());
    }
}
