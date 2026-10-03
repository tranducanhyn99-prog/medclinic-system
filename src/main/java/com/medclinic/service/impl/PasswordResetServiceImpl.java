package com.medclinic.service.impl;

import com.medclinic.entity.PasswordResetToken;
import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.PasswordResetTokenRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.EmailService;
import com.medclinic.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_VALIDITY_HOURS = 24;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.website:http://localhost:8080}")
    private String applicationUrl;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hashToken(rawToken))
                    .expiresAt(LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS))
                    .build();
            passwordResetTokenRepository.save(token);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(),
                    applicationUrl + "/reset-password?token=" + rawToken);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        com.medclinic.util.PasswordPolicy.requireValid(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessLogicException("Mật khẩu xác nhận không khớp.");
        }
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessLogicException("Liên kết khôi phục mật khẩu không hợp lệ.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashForUpdate(hashToken(rawToken))
                .orElseThrow(() -> new BusinessLogicException("Liên kết khôi phục mật khẩu không hợp lệ hoặc đã hết hạn."));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("Liên kết khôi phục mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        token.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Không thể tạo mã bảo mật", exception);
        }
    }
}
