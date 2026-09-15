package com.medclinic.service;

import com.medclinic.entity.DoctorSchedule;
import com.medclinic.entity.TimeSlot;
import com.medclinic.enums.ShiftType;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    DoctorSchedule createOrGetSchedule(Long doctorId, LocalDate workDate, ShiftType shift);
    List<DoctorSchedule> findByDoctorAndDateRange(Long doctorId, LocalDate from, LocalDate to);
    List<TimeSlot> getAvailableSlots(Long doctorId, LocalDate date);
    List<TimeSlot> getAllSlots(Long doctorId, LocalDate date);
}
