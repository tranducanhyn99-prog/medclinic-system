package com.medclinic.service;

import com.medclinic.util.Sha256;
import com.medclinic.entity.PasswordResetToken;
import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.PasswordResetTokenRepository;
import com.medclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_VALIDITY_HOURS = 24;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.website:http://localhost:8080}")
    private String applicationUrl;

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
                    .tokenHash(Sha256.hex(rawToken))
                    .expiresAt(LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS))
                    .build();
            passwordResetTokenRepository.save(token);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(),
                    applicationUrl + "/reset-password?token=" + rawToken);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        com.medclinic.util.PasswordPolicy.requireValid(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessLogicException("Mật khẩu xác nhận không khớp.");
        }
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessLogicException("Liên kết khôi phục mật khẩu không hợp lệ.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashForUpdate(Sha256.hex(rawToken))
                .orElseThrow(() -> new BusinessLogicException("Liên kết khôi phục mật khẩu không hợp lệ hoặc đã hết hạn."));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("Liên kết khôi phục mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        User user = token.getUser();
        LocalDateTime now = LocalDateTime.now();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setSecurityVersion(user.getSecurityVersion() + 1);
        token.setUsedAt(now);
        userRepository.save(user);
        userRepository.invalidateAuthChallenges(user.getId(), now);
        passwordResetTokenRepository.save(token);
    }

}
