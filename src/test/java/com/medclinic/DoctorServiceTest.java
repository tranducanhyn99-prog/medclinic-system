package com.medclinic;

import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.entity.Role;
import com.medclinic.entity.Specialty;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {
    @Mock private DoctorProfileRepository doctorProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private DoctorService doctorService;

    @Test
    void registersDoctorWithEncodedPasswordAndLicense() {
        AdminDoctorCreateDto dto = dto();
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(Specialty.builder().id(1L).build()));
        when(roleRepository.findByName(RoleType.ROLE_DOCTOR)).thenReturn(Optional.of(Role.builder().name(RoleType.ROLE_DOCTOR).build()));
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(doctorProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = doctorService.registerDoctor(dto);

        assertEquals("GPHN-001", result.getLicenseNumber());
        assertEquals("encoded", result.getUser().getPassword());
        verify(userRepository).save(any());
    }

    @Test
    void rejectsDuplicatePhoneBeforeCreatingUser() {
        AdminDoctorCreateDto dto = dto();
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> doctorService.registerDoctor(dto));
        verify(userRepository, never()).save(any());
    }

    private AdminDoctorCreateDto dto() {
        AdminDoctorCreateDto dto = new AdminDoctorCreateDto();
        dto.setUsername("doctor1"); dto.setPassword("password1"); dto.setFullName("Bác sĩ Nguyễn");
        dto.setEmail("doctor@example.com"); dto.setPhone("0912345678"); dto.setLicenseNumber("GPHN-001");
        dto.setSpecialtyId(1L); dto.setTitle("BS"); dto.setExperienceYears(3); dto.setConsultationFee(new BigDecimal("150000"));
        return dto;
    }
}
