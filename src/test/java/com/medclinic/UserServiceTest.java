package com.medclinic;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.Role;
import com.medclinic.entity.User;
import com.medclinic.enums.Gender;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role patientRole;
    private UserRegisterDto registerDto;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder()
                .id(1L)
                .name(RoleType.ROLE_PATIENT)
                .description("Bệnh nhân")
                .build();

        registerDto = new UserRegisterDto();
        registerDto.setUsername("nguyenvanan");
        registerDto.setPassword("123456");
        registerDto.setConfirmPassword("123456");
        registerDto.setFullName("Nguyễn Văn An");
        registerDto.setEmail("an.nguyen@example.com");
        registerDto.setPhone("0912345678");
        registerDto.setGender(Gender.MALE);
        registerDto.setDateOfBirth(LocalDate.of(1995, 5, 20));
        registerDto.setAddress("Hà Nội");
    }

    @Test
    @DisplayName("Đăng ký tài khoản bệnh nhân thành công")
    void testRegisterPatientSuccess() {
        when(userRepository.existsByUsername("nguyenvanan")).thenReturn(false);
        when(userRepository.existsByEmail("an.nguyen@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_PATIENT)).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode("123456")).thenReturn("hashedPassword123");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        User result = userService.registerPatient(registerDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("nguyenvanan", result.getUsername());
        assertEquals("hashedPassword123", result.getPassword());
        assertEquals(RoleType.ROLE_PATIENT, result.getRole().getName());
        assertFalse(result.getIsActive());
        assertFalse(result.getEmailVerified());
        assertFalse(result.getOtpBypass());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại khi mật khẩu xác nhận không khớp")
    void testRegisterPatientPasswordMismatch() {
        registerDto.setConfirmPassword("wrongPassword");

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            userService.registerPatient(registerDto);
        });

        assertEquals("Mật khẩu xác nhận không khớp!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại khi tên đăng nhập đã tồn tại")
    void testRegisterPatientDuplicateUsername() {
        when(userRepository.existsByUsername("nguyenvanan")).thenReturn(true);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            userService.registerPatient(registerDto);
        });

        assertTrue(exception.getMessage().contains("đã tồn tại"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đổi mật khẩu từ 8 ký tự trở lên")
    void rejectsShortNewPassword() {
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> userService.changePassword(1L, "old", "short"));
        assertTrue(exception.getMessage().contains("ít nhất 8"));
        verify(userRepository, never()).save(any(User.class));
    }
}
