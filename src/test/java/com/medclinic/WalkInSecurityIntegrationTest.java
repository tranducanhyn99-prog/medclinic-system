package com.medclinic;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.DoctorSchedule;
import com.medclinic.entity.TimeSlot;
import com.medclinic.enums.Gender;
import com.medclinic.enums.ShiftType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.DoctorScheduleRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class WalkInSecurityIntegrationTest {
    @Autowired private AppointmentService appointments;
    @Autowired private DoctorProfileRepository doctors;
    @Autowired private DoctorScheduleRepository schedules;
    @Autowired private TimeSlotRepository slots;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "STAFF")
    void newWalkInIsDisabledAndNeverUsesTheDefaultCredential() {
        DoctorProfile doctor = doctors.findAll().getFirst();
        LocalDate date = LocalDate.now().plusDays(80);
        DoctorSchedule schedule = schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date)
                .shift(ShiftType.AFTERNOON).maxPatients(8).build());
        TimeSlot slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(13, 30)).status(SlotStatus.AVAILABLE).build());
        String phone = "099" + Long.toUnsignedString(System.nanoTime()).substring(0, 7);

        appointments.bookWalkInAppointment("Walk in", phone, LocalDate.of(1990, 1, 1), Gender.MALE,
                "Address", doctor.getId(), date, slot.getId(), "Khám tổng quát");

        var patient = users.findByPhone(phone).orElseThrow();
        assertFalse(patient.getIsActive());
        assertFalse(passwordEncoder.matches("123456", patient.getPassword()));
        assertTrue(patient.getEmail().endsWith("@invalid.medclinic.local"));
    }
}
