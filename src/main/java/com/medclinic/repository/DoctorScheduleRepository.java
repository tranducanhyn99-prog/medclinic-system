package com.medclinic.repository;

import com.medclinic.entity.DoctorSchedule;
import com.medclinic.enums.ShiftType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    Optional<DoctorSchedule> findByDoctorIdAndWorkDateAndShift(Long doctorId, LocalDate workDate, ShiftType shift);

    @EntityGraph(attributePaths = "timeSlots")
    @Query("""
            select distinct schedule from DoctorSchedule schedule
            where schedule.doctor.id = :doctorId and schedule.workDate between :startDate and :endDate
            order by schedule.workDate, schedule.shift
            """)
    List<DoctorSchedule> findByDoctorIdAndWorkDateBetweenWithTimeSlots(Long doctorId, LocalDate startDate, LocalDate endDate);
}

