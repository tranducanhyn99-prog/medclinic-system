package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.repository.*;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.EmailService;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("dev")
class AppointmentEmailIntegrationTest {
    @Autowired private AppointmentService appointmentService;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired private TimeSlotRepository timeSlotRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @MockitoBean private EmailService emailService;

    @Test
    void bookingAndCancellationNotifyOnlyAfterCommittedTransactions() {
        String suffix = UUID.randomUUID().toString();
        DoctorProfile doctor = doctorProfileRepository.findAll().getFirst();
        Role role = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        User patient = userRepository.save(User.builder()
                .username("email-" + suffix).password("encoded").fullName("Email Patient")
                .email("email-" + suffix + "@example.test").role(role).isActive(true).build());
        LocalDate date = LocalDate.now().plusDays(14);
        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndWorkDateAndShift(
                doctor.getId(), date, ShiftType.AFTERNOON).orElseGet(() -> doctorScheduleRepository.save(
                DoctorSchedule.builder().doctor(doctor).workDate(date).shift(ShiftType.AFTERNOON).maxPatients(8).build()));
        TimeSlot slot = timeSlotRepository.save(TimeSlot.builder().schedule(schedule)
                .startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(13, 30)).status(SlotStatus.AVAILABLE).build());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(patient), "N/A", List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))));
        try {
            Appointment appointment = appointmentService.bookAppointment(patient.getId(), doctor.getId(), date, slot.getId(), "Khám tổng quát");
            verify(emailService, timeout(3000)).sendBookingConfirmationEmail(eq(patient.getEmail()), eq(patient.getFullName()),
                    eq(appointment.getBookingCode()), anyString(), eq(date), anyString());

            appointmentService.cancelAppointment(appointment.getId(), patient.getId());
            verify(emailService, timeout(3000)).sendCancellationEmail(eq(patient.getEmail()), eq(patient.getFullName()),
                    eq(appointment.getBookingCode()));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
