package com.medclinic.repository;

import com.medclinic.entity.Invoice;
import com.medclinic.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Override
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    Optional<Invoice> findById(Long id);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    Optional<Invoice> findByAppointmentId(Long appointmentId);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    List<Invoice> findByPaymentStatus(PaymentStatus status);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    List<Invoice> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    List<Invoice> findAllByOrderByCreatedAtDesc();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    BigDecimal calculateTotalPaidRevenue();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i " +
           "WHERE i.paymentStatus = 'PAID' AND i.paidAt >= :sinceDate")
    BigDecimal calculateRevenueSince(@Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT i FROM Invoice i ORDER BY i.createdAt DESC")
    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor", "appointment.doctor.user"})
    List<Invoice> findRecentInvoices();
}

