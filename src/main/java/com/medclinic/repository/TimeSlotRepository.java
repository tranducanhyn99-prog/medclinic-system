package com.medclinic.repository;

import com.medclinic.entity.TimeSlot;
import com.medclinic.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT t FROM TimeSlot t " +
           "JOIN t.schedule s " +
           "WHERE s.doctor.id = :doctorId " +
           "AND s.workDate = :workDate " +
           "AND t.status = :status " +
           "ORDER BY t.startTime ASC")
    List<TimeSlot> findAvailableSlotsByDoctorAndDate(@Param("doctorId") Long doctorId,
                                                    @Param("workDate") LocalDate workDate,
                                                    @Param("status") SlotStatus status);

    @Query("SELECT t FROM TimeSlot t " +
           "JOIN t.schedule s " +
           "WHERE s.doctor.id = :doctorId " +
           "AND s.workDate = :workDate " +
           "ORDER BY t.startTime ASC")
    List<TimeSlot> findAllSlotsByDoctorAndDate(@Param("doctorId") Long doctorId,
                                               @Param("workDate") LocalDate workDate);
}
