package com.medclinic.repository;

import com.medclinic.entity.AuthVerificationChallenge;
import com.medclinic.enums.AuthChallengeType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface AuthVerificationChallengeRepository extends JpaRepository<AuthVerificationChallenge, Long> {
    Optional<AuthVerificationChallenge> findByMagicTokenHash(String magicTokenHash);
    List<AuthVerificationChallenge> findByUserIdAndTypeAndConsumedAtIsNull(Long userId, AuthChallengeType type);
    long countByUserIdAndTypeAndCreatedAtAfter(Long userId, AuthChallengeType type, LocalDateTime after);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AuthVerificationChallenge c join fetch c.user where c.id = :id")
    Optional<AuthVerificationChallenge> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AuthVerificationChallenge c join fetch c.user where c.magicTokenHash = :hash")
    Optional<AuthVerificationChallenge> findByMagicTokenHashForUpdate(@Param("hash") String hash);
}
