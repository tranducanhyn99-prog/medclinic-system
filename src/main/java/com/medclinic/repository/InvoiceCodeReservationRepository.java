package com.medclinic.repository;

import com.medclinic.entity.InvoiceCodeReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceCodeReservationRepository extends JpaRepository<InvoiceCodeReservation, String> {
    /**
     * Deliberately uses INSERT rather than save: an assigned String id makes
     * Spring Data choose merge(), which could silently update a prior reservation.
     */
    @Modifying
    @Query(value = "INSERT INTO invoice_code_reservations (invoice_code) VALUES (:code)", nativeQuery = true)
    void insert(@Param("code") String code);
}
