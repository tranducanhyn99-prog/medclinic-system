package com.medclinic.repository;

import com.medclinic.entity.BankTransferPayment;
import com.medclinic.enums.BankTransferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDateTime;

public interface BankTransferPaymentRepository extends JpaRepository<BankTransferPayment, Long> {
    Optional<BankTransferPayment> findByProviderEventId(String providerEventId);
    boolean existsByBankTransactionId(String bankTransactionId);
    boolean existsByTransferCode(String transferCode);

    @EntityGraph(attributePaths = {"invoice", "invoice.appointment", "invoice.appointment.patient"})
    Optional<BankTransferPayment> findFirstByInvoiceIdAndStatusOrderByCreatedAtDesc(
            Long invoiceId, BankTransferStatus status);

    @Override
    @EntityGraph(attributePaths = {"invoice", "invoice.appointment", "invoice.appointment.patient"})
    Optional<BankTransferPayment> findById(Long id);

    @Query("select p.invoice.id from BankTransferPayment p where p.transferCode = :transferCode")
    Optional<Long> findInvoiceIdByTransferCode(@Param("transferCode") String transferCode);

    @Query("select p.invoice.id from BankTransferPayment p where p.id = :id")
    Optional<Long> findInvoiceIdById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BankTransferPayment p where p.transferCode = :transferCode")
    Optional<BankTransferPayment> findByTransferCodeForUpdate(@Param("transferCode") String transferCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BankTransferPayment p where p.id = :id")
    Optional<BankTransferPayment> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("update BankTransferPayment p set p.status = com.medclinic.enums.BankTransferStatus.EXPIRED " +
            "where p.status = com.medclinic.enums.BankTransferStatus.PENDING and p.expiresAt < :cutoff")
    int expirePendingBefore(@Param("cutoff") LocalDateTime cutoff);
}
