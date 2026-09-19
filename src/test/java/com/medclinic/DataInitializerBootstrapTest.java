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

    @Test
    void sampleDataInitializerIsLimitedToDevProfile() {
        Profile profile = DataInitializer.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }

    @Test
    void devBootstrapSeedsCompleteAndUniqueDoctorProfiles() {
        var doctors = doctorProfileRepository.findAll();
        assertTrue(doctors.size() >= 6);
        assertEquals(doctors.size(), new HashSet<>(doctors.stream().map(DoctorProfile::getLicenseNumber).toList()).size());
        assertTrue(doctors.stream().allMatch(doctor -> doctor.getLicenseNumber() != null && !doctor.getLicenseNumber().isBlank()
                && doctor.getTitle() != null && doctor.getExperienceYears() != null && doctor.getConsultationFee() != null));
    }
}
