package com.medclinic;

import com.medclinic.entity.PasswordResetToken;
import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.PasswordResetTokenRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.EmailService;
import com.medclinic.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @InjectMocks private PasswordResetServiceImpl passwordResetService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = User.builder().id(7L).email("patient@example.com").fullName("Bệnh nhân A").build();
        Field website = PasswordResetServiceImpl.class.getDeclaredField("applicationUrl");
        website.setAccessible(true);
        website.set(passwordResetService, "http://localhost:8080");
    }

    @Test
    @DisplayName("Yêu cầu khôi phục chỉ lưu token dạng băm")
    void requestPasswordResetStoresHashedToken() {
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        passwordResetService.requestPasswordReset("PATIENT@example.com");

        verify(passwordResetTokenRepository).deleteByUserId(7L);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertEquals(user, tokenCaptor.getValue().getUser());
        assertEquals(64, tokenCaptor.getValue().getTokenHash().length());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
        verify(emailService).sendPasswordResetEmail(any(), any(), org.mockito.ArgumentMatchers.contains("token="));
    }

    @Test
    @DisplayName("Token hợp lệ chỉ dùng một lần để đổi mật khẩu")
    void resetPasswordMarksTokenAsUsed() {
        String rawToken = "safe-reset-token";
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHashForUpdate(hash(rawToken))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded-password");

        passwordResetService.resetPassword(rawToken, "newpassword", "newpassword");

        assertEquals("encoded-password", user.getPassword());
        assertTrue(token.getUsedAt() != null);
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    @DisplayName("Token hết hạn không thể đổi mật khẩu")
    void resetPasswordRejectsExpiredToken() {
        String rawToken = "expired-token";
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHashForUpdate(hash(rawToken))).thenReturn(Optional.of(token));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> passwordResetService.resetPassword(rawToken, "newpassword", "newpassword"));

        assertTrue(exception.getMessage().contains("hết hạn"));
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
