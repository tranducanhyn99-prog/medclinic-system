package com.medclinic;

import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Specialty;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.SpecialtyRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)class DoctorServiceExtendedTest {
    @Mock DoctorProfileRepository doctorProfileRepository;
    @Mock UserRepository userRepository;
    @Mock SpecialtyRepository specialtyRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DoctorService service;

    private Specialty specialty(long id) {
        Specialty s = new Specialty(); s.setId(id); s.setName("S" + id); return s;
    }

    private DoctorProfile doctor(long id, long specialtyId, String name, String title, String fee) {
        DoctorProfile d = new DoctorProfile(); d.setId(id); d.setSpecialty(specialty(specialtyId));
        User u = new User(); u.setId(id); u.setFullName(name); d.setUser(u);
        d.setTitle(title); d.setConsultationFee(new BigDecimal(fee)); d.setExperienceYears(3);
        return d;
    }

    @Test
    void readsDoctorsAndHandlesMissingRecords() {
        DoctorProfile d = doctor(1L, 10L, "Nguyễn An", "BS", "100000");
        when(doctorProfileRepository.findAll()).thenReturn(List.of(d));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(d));
        when(doctorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(d));

        assertEquals(List.of(d), service.findAll());
        assertSame(d, service.findById(1L));        assertSame(d, service.findByUserId(1L));

        when(doctorProfileRepository.findById(9L)).thenReturn(Optional.empty());
        when(doctorProfileRepository.findByUserId(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(9L));
        assertThrows(ResourceNotFoundException.class, () -> service.findByUserId(9L));
    }

    @Test
    void filtersBySpecialtyFeeAndKeyword() {
        DoctorProfile a = doctor(1L, 10L, "Nguyễn An", "BS.CKI", "100000");
        DoctorProfile b = doctor(2L, 20L, "Trần Bình", "ThS.BS", "250000");
        DoctorProfile c = doctor(3L, 10L, "Lê Chi", "PGS.TS", "150000");
        when(doctorProfileRepository.findAll()).thenReturn(List.of(a, b, c));

        assertEquals(3, service.filterDoctors(null, null, null).size());
        assertEquals(List.of(a, c), service.filterDoctors(10L, null, " "));
        assertEquals(List.of(a, c), service.filterDoctors(null, new BigDecimal("150000"), null));
        assertEquals(List.of(a), service.filterDoctors(null, null, "nguyễn"));
        assertEquals(List.of(b), service.filterDoctors(null, null, "ths"));
        assertTrue(service.filterDoctors(10L, new BigDecimal("90000"), "xyz").isEmpty());
    }

    @Test
    void updatesDoctorProfile() {
        DoctorProfile current = doctor(1L, 10L, "Nguyễn An", "BS", "100000");
        Specialty newSpecialty = specialty(20L);
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(current));
        when(specialtyRepository.findById(20L)).thenReturn(Optional.of(newSpecialty));
        when(doctorProfileRepository.save(any(DoctorProfile.class))).thenAnswer(i -> i.getArgument(0));

        DoctorProfile updated = service.update(1L, 20L, " ThS.BS ", 5, new BigDecimal("180000"), "Bio");
        assertEquals(20L, updated.getSpecialty().getId());
        assertEquals("ThS.BS", updated.getTitle());
        assertEquals(5, updated.getExperienceYears());
        assertEquals(new BigDecimal("180000"), updated.getConsultationFee());
    }

    @Test
    void rejectsInvalidUpdateAndMissingSpecialty() {
        assertThrows(BusinessLogicException.class,
                () -> service.update(1L, 1L, " ", 1, BigDecimal.ONE, null));
        assertThrows(BusinessLogicException.class,
                () -> service.update(1L, 1L, "BS", -1, BigDecimal.ONE, null));
        assertThrows(BusinessLogicException.class,
                () -> service.update(1L, 1L, "BS", 1, new BigDecimal("-1"), null));

        DoctorProfile current = doctor(1L, 10L, "Nguyễn An", "BS", "100000");
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(current));
        when(specialtyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L, 99L, "BS", 1, BigDecimal.ONE, null));
    }

    private AdminDoctorCreateDto dto() {
        AdminDoctorCreateDto dto = new AdminDoctorCreateDto();
        dto.setUsername("doctor1"); dto.setPassword("password1"); dto.setFullName("Bác sĩ Nguyễn");        dto.setEmail("doctor@example.com"); dto.setPhone("0912345678"); dto.setLicenseNumber("GPHN-001");
        dto.setSpecialtyId(1L); dto.setTitle("BS"); dto.setExperienceYears(3);
        dto.setConsultationFee(new BigDecimal("150000")); dto.setBio("Bio");
        return dto;
    }

    @Test
    void rejectsDuplicateRegistrationFields() {
        AdminDoctorCreateDto dto = dto();
        when(userRepository.existsByUsername("doctor1")).thenReturn(true);
        assertThrows(BusinessLogicException.class, () -> service.registerDoctor(dto));

        reset(userRepository);
        when(userRepository.existsByEmail("doctor@example.com")).thenReturn(true);
        assertThrows(BusinessLogicException.class, () -> service.registerDoctor(dto));

        reset(userRepository);
        when(userRepository.existsByPhone("0912345678")).thenReturn(true);
        assertThrows(BusinessLogicException.class, () -> service.registerDoctor(dto));

        reset(userRepository);
        when(doctorProfileRepository.findByLicenseNumber("GPHN-001"))
                .thenReturn(Optional.of(new DoctorProfile()));
        assertThrows(BusinessLogicException.class, () -> service.registerDoctor(dto));
    }

    @Test
    void rejectsRegistrationWhenSpecialtyOrRoleIsMissing() {
        AdminDoctorCreateDto dto = dto();
        when(specialtyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.registerDoctor(dto));
        reset(specialtyRepository);
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty(1L)));
        when(roleRepository.findByName(RoleType.ROLE_DOCTOR)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.registerDoctor(dto));
    }

    @Test
    void rejectsFractionalAndOverflowConsultationFeesBeforeSaving() {
        for (String value : new String[]{"1000.50", "10000000000"}) {
            AdminDoctorCreateDto dto = dto();
            dto.setConsultationFee(new BigDecimal(value));
            assertThrows(BusinessLogicException.class, () -> service.registerDoctor(dto));
            assertThrows(BusinessLogicException.class, () -> service.update(1L, 1L, "BS", 0, dto.getConsultationFee(), null));
        }
        verifyNoInteractions(userRepository, doctorProfileRepository);
    }
}
