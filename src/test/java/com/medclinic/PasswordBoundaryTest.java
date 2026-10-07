package com.medclinic;

import com.medclinic.config.SecurityConfig;
import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.dto.UserRegisterDto;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.service.EmailService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.PasswordResetService;
import com.medclinic.service.UserService;
import com.medclinic.service.impl.*;
import com.medclinic.util.PasswordPolicy;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordBoundaryTest {
    @Test
    void bcryptDoesNotAcceptDifferentSuffixOrUnicodeByteOverflow() {
        PasswordEncoder encoder = new SecurityConfig(null, null, null).passwordEncoder();
        for (String boundary : new String[] {"a".repeat(72), "á".repeat(36)}) {
            String hash = encoder.encode(boundary);
            assertTrue(encoder.matches(boundary, hash));
            assertFalse(encoder.matches(boundary + "b", hash));
        }
        assertTrue(encoder.matches("123456", encoder.encode("123456")), "Existing short demo hashes remain usable");
    }

    @Test
    void dtoValidationRejectsByteOverflowAndAllowsUnicodeAtBoundary() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var patient = new UserRegisterDto();
            var doctor = new AdminDoctorCreateDto();
            for (String invalid : new String[] {"a".repeat(73), "á".repeat(40), "short", "        "}) {
                patient.setPassword(invalid); doctor.setPassword(invalid);
                assertFalse(validator.validateProperty(patient, "password").isEmpty());
                assertFalse(validator.validateProperty(doctor, "password").isEmpty());
            }
            patient.setPassword("á".repeat(36)); doctor.setPassword("a".repeat(72));
            assertTrue(validator.validateProperty(patient, "password").isEmpty());
            assertTrue(validator.validateProperty(doctor, "password").isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ascii", "unicode", "blank", "short"})
    void invalidPasswordsNeverReachPersistenceOrTokenConsumption(String kind) {
        String password = switch (kind) {
            case "ascii" -> "a".repeat(73);
            case "unicode" -> "á".repeat(40);
            case "blank" -> "        ";
            default -> "short";
        };
        var users = mock(UserRepository.class);
        var roles = mock(RoleRepository.class);
        var profiles = mock(DoctorProfileRepository.class);
        var specialties = mock(SpecialtyRepository.class);
        var tokens = mock(PasswordResetTokenRepository.class);
        var encoder = mock(PasswordEncoder.class);
        var patientDto = new UserRegisterDto();
        patientDto.setPassword(password); patientDto.setConfirmPassword(password);
        var doctorDto = new AdminDoctorCreateDto(); doctorDto.setPassword(password);
        var patientService = new UserService(users, roles, encoder);
        assertEquals(PasswordPolicy.MESSAGE, assertThrows(BusinessLogicException.class,
                () -> patientService.registerPatient(patientDto)).getMessage());
        assertThrows(BusinessLogicException.class, () -> patientService.changePassword(1L, "old-password", password));
        assertThrows(BusinessLogicException.class, () -> new PasswordResetService(users, tokens, encoder, mock(EmailService.class))
                .resetPassword("test-token", password, password));
        assertThrows(BusinessLogicException.class, () -> new DoctorService(profiles, users, specialties, roles, encoder)
                .registerDoctor(doctorDto));
        verifyNoInteractions(users, roles, profiles, specialties, tokens, encoder);
    }
}
