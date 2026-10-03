package com.medclinic;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.DoctorSchedule;
import com.medclinic.entity.TimeSlot;
import com.medclinic.enums.ShiftType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.DoctorScheduleRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    @Mock DoctorScheduleRepository scheduleRepository;
    @Mock TimeSlotRepository timeSlotRepository;
    @Mock DoctorProfileRepository doctorRepository;
    @InjectMocks ScheduleServiceImpl service;
    @Test
    void returnsExistingScheduleWithoutCreatingAnother() {
        LocalDate date = LocalDate.of(2026, 9, 20);
        DoctorSchedule existing = new DoctorSchedule();
        when(scheduleRepository.findByDoctorIdAndWorkDateAndShift(1L, date, ShiftType.MORNING))
                .thenReturn(Optional.of(existing));

        assertSame(existing, service.createOrGetSchedule(1L, date, ShiftType.MORNING));
        verifyNoInteractions(doctorRepository);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void createsMorningAndAfternoonSchedulesWithEightAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 9, 20);
        DoctorProfile doctor = new DoctorProfile(); doctor.setId(1L);
        when(scheduleRepository.findByDoctorIdAndWorkDateAndShift(anyLong(), eq(date), any()))
                .thenReturn(Optional.empty());
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.save(any(DoctorSchedule.class))).thenAnswer(i -> i.getArgument(0));

        DoctorSchedule morning = service.createOrGetSchedule(1L, date, ShiftType.MORNING);
        DoctorSchedule afternoon = service.createOrGetSchedule(1L, date, ShiftType.AFTERNOON);

        assertEquals(8, morning.getTimeSlots().size());
        assertEquals(LocalTime.of(8, 0), morning.getTimeSlots().get(0).getStartTime());
        assertEquals(LocalTime.of(12, 0), morning.getTimeSlots().get(7).getEndTime());
        assertEquals(LocalTime.of(13, 30), afternoon.getTimeSlots().get(0).getStartTime());
        assertEquals(LocalTime.of(17, 30), afternoon.getTimeSlots().get(7).getEndTime());
        assertTrue(morning.getTimeSlots().stream().allMatch(s -> s.getStatus() == SlotStatus.AVAILABLE));
        assertTrue(morning.getTimeSlots().stream().allMatch(s -> s.getSchedule() == morning));
    }
    @Test
    void rejectsMissingDoctor() {
        LocalDate date = LocalDate.of(2026, 9, 20);
        when(scheduleRepository.findByDoctorIdAndWorkDateAndShift(9L, date, ShiftType.MORNING))
                .thenReturn(Optional.empty());
        when(doctorRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.createOrGetSchedule(9L, date, ShiftType.MORNING));
    }

    @Test
    void delegatesScheduleAndSlotQueries() {
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = from.plusDays(14);
        DoctorSchedule schedule = new DoctorSchedule();
        TimeSlot slot = new TimeSlot();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(DoctorProfile.builder()
                .user(com.medclinic.entity.User.builder().isActive(true).build())
                .specialty(com.medclinic.entity.Specialty.builder().isActive(true).build()).build()));
        when(scheduleRepository.findByDoctorIdAndWorkDateBetweenWithTimeSlots(1L, from, to))
                .thenReturn(List.of(schedule));
        when(timeSlotRepository.findAvailableSlotsByDoctorAndDate(1L, from, SlotStatus.AVAILABLE))
                .thenReturn(List.of(slot));
        when(timeSlotRepository.findAllSlotsByDoctorAndDate(1L, from)).thenReturn(List.of(slot));

        assertEquals(List.of(schedule), service.findByDoctorAndDateRange(1L, from, to));
        assertEquals(List.of(slot), service.getAvailableSlots(1L, from));
        assertEquals(List.of(slot), service.getAllSlots(1L, from));
    }
}
