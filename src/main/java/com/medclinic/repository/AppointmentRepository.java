package com.medclinic.repository;

import com.medclinic.entity.Appointment;
import com.medclinic.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByBookingCode(String bookingCode);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);


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

    @Query("SELECT a FROM Appointment a " +
           "WHERE LOWER(a.bookingCode) = LOWER(:keyword) " +
           "OR a.patient.phone = :keyword " +
           "OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointment> searchForReception(@Param("keyword") String keyword);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatusGroup();

    @Query("SELECT a.doctor.specialty.name, COUNT(a) FROM Appointment a GROUP BY a.doctor.specialty.name")
    List<Object[]> countAppointmentsBySpecialtyGroup();
}

