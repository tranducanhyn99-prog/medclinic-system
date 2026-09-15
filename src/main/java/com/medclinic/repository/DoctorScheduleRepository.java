package com.medclinic.repository;

import com.medclinic.entity.DoctorSchedule;
import com.medclinic.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorIdAndWorkDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);
    List<DoctorSchedule> findByDoctorIdAndWorkDate(Long doctorId, LocalDate workDate);
    Optional<DoctorSchedule> findByDoctorIdAndWorkDateAndShift(Long doctorId, LocalDate workDate, ShiftType shift);
}

