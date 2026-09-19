package com.medclinic.repository;

import com.medclinic.entity.Appointment;
import com.medclinic.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "medicalRecord", "medicalRecord.prescription", "medicalRecord.prescription.items", "medicalRecord.prescription.items.medicine"})
    Optional<Appointment> findById(Long id);

    Optional<Appointment> findByBookingCode(String bookingCode);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);


    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueSequenceAsc(Long doctorId, LocalDate appointmentDate, List<AppointmentStatus> statuses);
    /** Retained for compatibility with older callers; production queue reads use queueSequence. */
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueNumberAsc(Long doctorId, LocalDate appointmentDate, List<AppointmentStatus> statuses);
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByTimeSlot_StartTimeAsc(Long doctorId, LocalDate appointmentDate);

    List<Appointment> findByAppointmentDateOrderByTimeSlot_StartTimeAsc(LocalDate appointmentDate);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    long countByAppointmentDate(LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a " +
           "WHERE a.appointmentDate = :date " +
           "AND a.status IN :statuses " +
           "ORDER BY a.timeSlot.startTime ASC")
    List<Appointment> findTodayQueue(@Param("date") LocalDate date,
                                     @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.queueNumber IS NOT NULL")
    long countQueueForDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(MAX(a.queueSequence), 0) FROM Appointment a WHERE a.appointmentDate = :date")
    long maxQueueSequenceForDate(@Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a " +
           "WHERE a.appointmentDate = :date AND (LOWER(a.bookingCode) = LOWER(:keyword) " +
           "OR a.patient.phone = :keyword " +
           "OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Appointment> searchForReception(@Param("keyword") String keyword, @Param("date") LocalDate date);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatusGroup();

    @Query("SELECT a.doctor.specialty.name, COUNT(a) FROM Appointment a GROUP BY a.doctor.specialty.name")
    List<Object[]> countAppointmentsBySpecialtyGroup();
}

