package com.medclinic.service;

import com.medclinic.entity.User;
import com.medclinic.enums.AuthChallengeType;

import java.time.LocalDateTime;

public interface AuthVerificationService {
    ChallengeIssued issueChallenge(Long userId, AuthChallengeType type);
    ChallengeIssued resendChallenge(Long challengeId);
    VerificationResult verifyCode(Long challengeId, String code);
    VerificationResult verifyMagicToken(String rawToken);
    ChallengeIssued getChallengeInfo(Long challengeId);

    record ChallengeIssued(Long challengeId, String maskedEmail,
                           LocalDateTime expiresAt, AuthChallengeType type) {}

    record VerificationResult(User user, AuthChallengeType type) {}
}
