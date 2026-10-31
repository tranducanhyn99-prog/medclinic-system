package com.medclinic.service;

import com.medclinic.dto.SlotDto;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.DoctorSchedule;
import com.medclinic.entity.TimeSlot;
import com.medclinic.enums.ShiftType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.DoctorScheduleRepository;
import com.medclinic.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private java.time.Clock businessClock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @org.springframework.beans.factory.annotation.Autowired
    public void setBusinessClock(java.time.Clock clock) { this.businessClock = clock; }

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Transactional
    public DoctorSchedule createOrGetSchedule(Long doctorId, LocalDate workDate, ShiftType shift) {
        Optional<DoctorSchedule> existing = doctorScheduleRepository.findByDoctorIdAndWorkDateAndShift(doctorId, workDate, shift);
        if (existing.isPresent()) {
            return existing.get();
        }

        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ"));

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setWorkDate(workDate);
        schedule.setShift(shift);

        List<TimeSlot> timeSlots = generateTimeSlots(schedule, shift);
        schedule.setTimeSlots(timeSlots);

        return doctorScheduleRepository.save(schedule);
    }

    private List<TimeSlot> generateTimeSlots(DoctorSchedule schedule, ShiftType shift) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime startTime = shift == ShiftType.MORNING ? LocalTime.of(8, 0) : LocalTime.of(13, 30);

        for (int i = 0; i < 8; i++) {
            TimeSlot slot = new TimeSlot();
            slot.setSchedule(schedule);
            slot.setStartTime(startTime);
            slot.setEndTime(startTime.plusMinutes(30));
            slot.setStatus(SlotStatus.AVAILABLE);
            slots.add(slot);

            startTime = startTime.plusMinutes(30);
        }
        return slots;
    }

    @Transactional(readOnly = true)
    public List<DoctorSchedule> findByDoctorAndDateRange(Long doctorId, LocalDate from, LocalDate to) {
        return doctorScheduleRepository.findByDoctorIdAndWorkDateBetweenWithTimeSlots(doctorId, from, to);
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        LocalDate today = LocalDate.now(businessClock);
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ"));
        if (!com.medclinic.util.DoctorAvailability.acceptsBookings(doctor)
                || date == null || date.isBefore(today) || date.isAfter(today.plusDays(14))) {
            return List.of();
        }
        LocalTime now = LocalTime.now(businessClock);
        return timeSlotRepository.findAvailableSlotsByDoctorAndDate(doctorId, date, SlotStatus.AVAILABLE)
                .stream().filter(slot -> !date.equals(today) || slot.getStartTime().isAfter(now)).toList();
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getDisplaySlotDtos(Long doctorId, LocalDate date) {
        LocalDate today = LocalDate.now(businessClock);
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ"));
        if (!com.medclinic.util.DoctorAvailability.acceptsBookings(doctor)
                || date == null || date.isBefore(today) || date.isAfter(today.plusDays(14))) {
            return List.of();
        }
        LocalTime now = LocalTime.now(businessClock);
        return timeSlotRepository.findAllSlotsByDoctorAndDate(doctorId, date).stream()
                .map(slot -> {
                    String displayStatus;
                    if (slot.getStatus() != SlotStatus.AVAILABLE) {
                        displayStatus = "BOOKED";
                    } else if (date.equals(today) && !slot.getStartTime().isAfter(now)) {
                        displayStatus = "EXPIRED";
                    } else {
                        displayStatus = "AVAILABLE";
                    }
                    return SlotDto.builder()
                            .id(slot.getId())
                            .startTime(slot.getStartTime().toString())
                            .endTime(slot.getEndTime().toString())
                            .status(displayStatus)
                            .build();
                })
                .toList();
    }

}
