package com.medclinic.service.impl;

import com.medclinic.entity.AuthVerificationChallenge;
import com.medclinic.entity.User;
import com.medclinic.enums.AuthChallengeType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.AuthVerificationChallengeRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.AuthVerificationService;
import com.medclinic.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthVerificationServiceImpl implements AuthVerificationService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_CHALLENGES_PER_WINDOW = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthVerificationChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.auth.otp-validity-seconds:300}")
    private long validitySeconds;

    @Value("${app.website:http://localhost:8080}")
    private String applicationUrl;

    @Override
    @Transactional
    public ChallengeIssued issueChallenge(Long userId, AuthChallengeType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy tài khoản cần xác thực."));
        if (Boolean.TRUE.equals(user.getOtpBypass())) {
            throw new BusinessLogicException("Tài khoản này không yêu cầu OTP.");
        }
        return createChallenge(user, type);
    }

    @Override
    @Transactional
    public ChallengeIssued resendChallenge(Long challengeId) {
        AuthVerificationChallenge current = challengeRepository.findByIdForUpdate(challengeId)
                .orElseThrow(() -> new BusinessLogicException("Phiên xác thực không tồn tại."));
        if (current.getConsumedAt() != null) {
            throw new BusinessLogicException("Phiên xác thực này đã được sử dụng.");
        }
        long recentChallenges = challengeRepository.countByUserIdAndTypeAndCreatedAtAfter(
                current.getUser().getId(), current.getType(), LocalDateTime.now().minusSeconds(validitySeconds));
        if (recentChallenges >= MAX_CHALLENGES_PER_WINDOW) {
            throw new BusinessLogicException("Bạn đã gửi lại OTP quá nhiều lần. Vui lòng thử lại sau.");
        }
        current.setConsumedAt(LocalDateTime.now());
        challengeRepository.save(current);
        return createChallenge(current.getUser(), current.getType());
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeIssued getChallengeInfo(Long challengeId) {
        AuthVerificationChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessLogicException("Phiên xác thực không tồn tại."));
        validateUsable(challenge);
        return toIssued(challenge);
    }

    @Override
    @Transactional(noRollbackFor = BusinessLogicException.class)
    public VerificationResult verifyCode(Long challengeId, String code) {
        AuthVerificationChallenge challenge = challengeRepository.findByIdForUpdate(challengeId)
                .orElseThrow(() -> new BusinessLogicException("Phiên xác thực không tồn tại."));
        validateUsable(challenge);
        int attempts = challenge.getAttempts() == null ? 0 : challenge.getAttempts();
        if (attempts >= MAX_ATTEMPTS) {
            throw new BusinessLogicException("Bạn đã nhập sai OTP quá nhiều lần. Vui lòng gửi lại mã mới.");
        }
        if (code == null || !code.matches("\\d{6}") || !passwordEncoder.matches(code, challenge.getCodeHash())) {
            challenge.setAttempts(attempts + 1);
            challengeRepository.save(challenge);
            throw new BusinessLogicException("Mã OTP không chính xác.");
        }
        return consume(challenge);
    }

    @Override
    @Transactional
    public VerificationResult verifyMagicToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessLogicException("Liên kết xác thực không hợp lệ.");
        }
        AuthVerificationChallenge challenge = challengeRepository
                .findByMagicTokenHashForUpdate(hashToken(rawToken))
                .orElseThrow(() -> new BusinessLogicException("Liên kết xác thực không hợp lệ hoặc đã hết hạn."));
        validateUsable(challenge);
        return consume(challenge);
    }

    private VerificationResult consume(AuthVerificationChallenge challenge) {
        challenge.setConsumedAt(LocalDateTime.now());
        User user = challenge.getUser();
        if (challenge.getType() == AuthChallengeType.REGISTRATION) {
            user.setEmailVerified(true);
            user.setIsActive(true);
            userRepository.save(user);
        }
        challengeRepository.save(challenge);
        return new VerificationResult(user, challenge.getType());
    }

    private ChallengeIssued createChallenge(User user, AuthChallengeType type) {
        LocalDateTime now = LocalDateTime.now();
        for (AuthVerificationChallenge open : challengeRepository
                .findByUserIdAndTypeAndConsumedAtIsNull(user.getId(), type)) {
            open.setConsumedAt(now);
            challengeRepository.save(open);
        }

        String rawCode = String.format("%06d", RANDOM.nextInt(1_000_000));
        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
        AuthVerificationChallenge challenge = AuthVerificationChallenge.builder()
                .user(user)
                .type(type)
                .codeHash(passwordEncoder.encode(rawCode))
                .magicTokenHash(hashToken(rawToken))
                .expiresAt(now.plusSeconds(validitySeconds))
                .attempts(0)
                .build();
        challenge = challengeRepository.save(challenge);
        String confirmUrl = applicationUrl + "/auth/verify-email?token=" + rawToken;
        scheduleEmail(user, type, rawCode, confirmUrl);
        return toIssued(challenge);
    }

    private void validateUsable(AuthVerificationChallenge challenge) {
        if (challenge.getConsumedAt() != null || challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("Mã xác thực không hợp lệ hoặc đã hết hạn. Vui lòng gửi lại OTP.");
        }
    }

    private void scheduleEmail(User user, AuthChallengeType type, String rawCode, String confirmUrl) {
        Runnable action = () -> emailService.sendAuthenticationOtpEmail(
                user.getEmail(), user.getFullName(), rawCode, confirmUrl,
                Math.max(1L, validitySeconds / 60L), type.getDisplayName());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private ChallengeIssued toIssued(AuthVerificationChallenge challenge) {
        return new ChallengeIssued(challenge.getId(), maskEmail(challenge.getUser().getEmail()),
                challenge.getExpiresAt(), challenge.getType());
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + email.substring(Math.max(0, at));
        }
        return email.substring(0, 1) + "***" + email.substring(at);
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
