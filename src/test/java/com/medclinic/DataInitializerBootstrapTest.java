package com.medclinic;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.config.DataInitializer;
import com.medclinic.repository.DoctorProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Profile;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class DataInitializerBootstrapTest {
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private com.medclinic.repository.DoctorScheduleRepository schedules;
    @Autowired private java.time.Clock businessClock;

    @Test
    void sampleDataInitializerIsLimitedToDevOrDemoProfiles() {
        Profile profile = DataInitializer.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev", "demo"}, profile.value());
    }

    @Test
    void devBootstrapSeedsCompleteAndUniqueDoctorProfiles() {
        var doctors = doctorProfileRepository.findAll();
        assertTrue(doctors.size() >= 12);
        assertEquals(doctors.size(), new HashSet<>(doctors.stream().map(DoctorProfile::getLicenseNumber).toList()).size());
        assertTrue(doctors.stream().allMatch(doctor -> doctor.getLicenseNumber() != null && !doctor.getLicenseNumber().isBlank()
                && doctor.getTitle() != null && doctor.getExperienceYears() != null && doctor.getConsultationFee() != null));
    }

    @Test
    void seededSchedulesIncludeTheLastBookableBusinessDay() {
        var lastDay = java.time.LocalDate.now(businessClock).plusDays(14);
        for (var doctor : doctorProfileRepository.findAll()) {
            if (java.util.Set.of(
                    "doctor", "doc_mai", "doc_nam", "doc_ha", "doc_bao", "doc_tuan",
                    "doc_linh", "doc_khoa", "doc_lan", "doc_duc", "doc_trang", "doc_minh")
                    .contains(doctor.getUser().getUsername())) {
                assertEquals(2, schedules.findByDoctorIdAndWorkDateBetweenWithTimeSlots(doctor.getId(), lastDay, lastDay).size());
            }
        }
    }
}
