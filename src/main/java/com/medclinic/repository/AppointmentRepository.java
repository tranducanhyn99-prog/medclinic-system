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
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.specialty", "timeSlot", "medicalRecord", "medicalRecord.prescription", "medicalRecord.prescription.items", "medicalRecord.prescription.items.medicine"})
    Optional<Appointment> findById(Long id);

    Optional<Appointment> findByBookingCode(String bookingCode);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);


    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueSequenceAsc(Long doctorId, LocalDate appointmentDate, List<AppointmentStatus> statuses);
    List<Appointment> findByAppointmentDateOrderByTimeSlot_StartTimeAsc(LocalDate appointmentDate);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.specialty", "timeSlot"})
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAscTimeSlot_StartTimeAsc(
            LocalDate firstDate, LocalDate lastDate);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.specialty", "timeSlot"})
    List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatusInOrderByAppointmentDateAscQueueSequenceAsc(
            Long doctorId, LocalDate firstDate, LocalDate lastDate, List<AppointmentStatus> statuses);

    long countByAppointmentDate(LocalDate appointmentDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a " +
           "WHERE a.appointmentDate = :date AND (LOWER(a.bookingCode) = LOWER(:keyword) " +
           "OR a.patient.phone = :keyword " +
           "OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Appointment> searchForReception(@Param("keyword") String keyword, @Param("date") LocalDate date);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.user", "doctor.specialty", "timeSlot"})
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :firstDate AND :lastDate " +
           "AND (LOWER(a.bookingCode) = LOWER(:keyword) OR a.patient.phone = :keyword " +
           "OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.appointmentDate ASC, a.timeSlot.startTime ASC")
    List<Appointment> searchForReceptionBetween(@Param("keyword") String keyword,
            @Param("firstDate") LocalDate firstDate, @Param("lastDate") LocalDate lastDate);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatusGroup();

    @Query("SELECT a.doctor.specialty.name, COUNT(a) FROM Appointment a GROUP BY a.doctor.specialty.name")
    List<Object[]> countAppointmentsBySpecialtyGroup();
}

