package com.medclinic.entity;

import com.medclinic.enums.BankTransferStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transfer_payments", indexes = {
        @Index(name = "idx_bank_transfer_invoice", columnList = "invoice_id"),
        @Index(name = "idx_bank_transfer_status_expiry", columnList = "status, expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransferPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "transfer_code", nullable = false, unique = true, length = 25)
    private String transferCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "bank_id", nullable = false, length = 20)
    private String bankId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BankTransferStatus status = BankTransferStatus.PENDING;

    @Column(name = "provider_event_id", unique = true, length = 100)
    private String providerEventId;

    @Column(name = "bank_transaction_id", unique = true, length = 100)
    private String bankTransactionId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;
}
