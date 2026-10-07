package com.medclinic.repository;

import com.medclinic.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    BigDecimal calculateTotalPaidRevenue();

}

