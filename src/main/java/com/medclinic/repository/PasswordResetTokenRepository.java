package com.medclinic.repository;

import com.medclinic.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t join fetch t.user where t.tokenHash = :hash")
    Optional<PasswordResetToken> findByTokenHashForUpdate(@Param("hash") String hash);
    void deleteByUserId(Long userId);
    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
