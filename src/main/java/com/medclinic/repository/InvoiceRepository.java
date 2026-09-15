package com.medclinic.repository;

import com.medclinic.entity.Invoice;
import com.medclinic.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByAppointmentId(Long appointmentId);
    Optional<Invoice> findByInvoiceCode(String invoiceCode);
    List<Invoice> findByPaymentStatus(PaymentStatus status);
    List<Invoice> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);
    List<Invoice> findAllByOrderByCreatedAtDesc();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    BigDecimal calculateTotalPaidRevenue();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i " +
           "WHERE i.paymentStatus = 'PAID' AND i.paidAt >= :sinceDate")
    BigDecimal calculateRevenueSince(@Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT i FROM Invoice i ORDER BY i.createdAt DESC")
    List<Invoice> findRecentInvoices();
}

