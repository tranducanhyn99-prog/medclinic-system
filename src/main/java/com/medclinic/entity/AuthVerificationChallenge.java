package com.medclinic.entity;

import com.medclinic.enums.AuthChallengeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_verification_challenges", indexes = {
        @Index(name = "idx_auth_challenge_magic_token", columnList = "magic_token_hash"),
        @Index(name = "idx_auth_challenge_user_created", columnList = "user_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthVerificationChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthChallengeType type;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "magic_token_hash", nullable = false, unique = true, length = 64)
    private String magicTokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime consumedAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer attempts = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
