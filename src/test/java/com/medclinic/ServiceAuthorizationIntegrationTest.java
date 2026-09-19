package com.medclinic;

import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.service.DoctorService;
import com.medclinic.service.MedicalRecordService;
import com.medclinic.service.MedicineService;
import com.medclinic.service.SpecialtyService;
import com.medclinic.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("dev")
class ServiceAuthorizationIntegrationTest {
    @Autowired private SpecialtyService specialties;
    @Autowired private MedicineService medicines;
    @Autowired private DoctorService doctors;
    @Autowired private UserService users;
    @Autowired private MedicalRecordService records;

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientCannotUseAdministrativeOrDoctorServiceEntryPoints() {
        assertThrows(AccessDeniedException.class, () -> specialties.save("Khoa trái phép", null, null));
        assertThrows(AccessDeniedException.class, () -> medicines.save("Thuốc trái phép", "viên", BigDecimal.ONE, null, 1));
        assertThrows(AccessDeniedException.class, () -> doctors.registerDoctor(new AdminDoctorCreateDto()));
        assertThrows(AccessDeniedException.class, () -> users.toggleUserStatus(1L));
        assertThrows(AccessDeniedException.class, () -> records.startExamination(1L, 1L));
    }
}
