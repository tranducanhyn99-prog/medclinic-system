package com.medclinic.repository;

import com.medclinic.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.medclinic.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Override
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user", "appointment.doctor.specialty"})
    Optional<Invoice> findById(Long id);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user", "appointment.doctor.specialty"})
    Optional<Invoice> findByAppointmentId(Long appointmentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user", "appointment.doctor.specialty"})
    List<Invoice> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user", "appointment.doctor.specialty"})
    @Query("SELECT i FROM Invoice i WHERE (:status IS NULL OR i.paymentStatus = :status) " +
            "AND (:from IS NULL OR i.createdAt >= :from) AND (:until IS NULL OR i.createdAt < :until)")
    Page<Invoice> findForBilling(@Param("status") PaymentStatus status,
                               @Param("from") LocalDateTime from, @Param("until") LocalDateTime until,
                               Pageable pageable);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.paymentStatus = :status " +
            "AND (:from IS NULL OR i.createdAt >= :from) AND (:until IS NULL OR i.createdAt < :until)")
    long countForBilling(@Param("status") PaymentStatus status,
                         @Param("from") LocalDateTime from, @Param("until") LocalDateTime until);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    BigDecimal calculateTotalPaidRevenue();

}

