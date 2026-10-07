package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.SlotStatus;
import com.medclinic.repository.*;
import com.medclinic.service.ScheduleService;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingTimeBoundaryTest {
    @Test
    void businessDateMovesToTomorrowAtVietnamMidnightWhileUtcIsStillYesterday() {
        var doctorRepo = mock(DoctorProfileRepository.class);
        var slotRepo = mock(TimeSlotRepository.class);
        var service = new ScheduleService(mock(DoctorScheduleRepository.class), slotRepo, doctorRepo);
        service.setBusinessClock(Clock.fixed(Instant.parse("2026-10-01T17:00:01Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
        var doctor = DoctorProfile.builder().user(User.builder().build()).specialty(Specialty.builder().build()).build();
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(doctor));
        var slot = TimeSlot.builder().startTime(LocalTime.of(8, 0)).status(SlotStatus.AVAILABLE).build();
        when(slotRepo.findAvailableSlotsByDoctorAndDate(1L, LocalDate.of(2026, 10, 2), SlotStatus.AVAILABLE)).thenReturn(List.of(slot));
        assertTrue(service.getAvailableSlots(1L, LocalDate.of(2026, 10, 1)).isEmpty());
        assertEquals(List.of(slot), service.getAvailableSlots(1L, LocalDate.of(2026, 10, 2)));
        assertTrue(service.getAvailableSlots(1L, LocalDate.of(2026, 10, 17)).isEmpty());
    }

    @Test
    void availableSlotsExcludeStartedAndPastTimesInVietnam() {
        var doctorRepo = mock(DoctorProfileRepository.class);
        var slotRepo = mock(TimeSlotRepository.class);
        var service = new ScheduleService(mock(DoctorScheduleRepository.class), slotRepo, doctorRepo);
        service.setBusinessClock(Clock.fixed(Instant.parse("2026-10-01T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(DoctorProfile.builder()
                .user(User.builder().build()).specialty(Specialty.builder().build()).build()));
        var past = TimeSlot.builder().startTime(LocalTime.of(9, 0)).build();
        var started = TimeSlot.builder().startTime(LocalTime.of(10, 0)).build();
        var future = TimeSlot.builder().startTime(LocalTime.of(10, 30)).build();
        when(slotRepo.findAvailableSlotsByDoctorAndDate(1L, LocalDate.of(2026, 10, 1), SlotStatus.AVAILABLE))
                .thenReturn(List.of(past, started, future));
        assertEquals(List.of(future), service.getAvailableSlots(1L, LocalDate.of(2026, 10, 1)));
    }
}
