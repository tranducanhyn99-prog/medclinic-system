package com.medclinic;

import com.medclinic.controller.ApiController;
import com.medclinic.entity.TimeSlot;
import com.medclinic.enums.SlotStatus;
import com.medclinic.service.ScheduleService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiControllerTest {

    @Test
    void mapsAvailableSlotsToPublicDto() {
        ScheduleService scheduleService = mock(ScheduleService.class);
        TimeSlot slot = new TimeSlot();
        slot.setId(7L);
        slot.setStartTime(LocalTime.of(8, 0));
        slot.setEndTime(LocalTime.of(8, 30));
        slot.setStatus(SlotStatus.AVAILABLE);
        when(scheduleService.getAvailableSlots(3L, LocalDate.of(2026, 9, 20)))
                .thenReturn(List.of(slot));

        var result = new ApiController(scheduleService).getDoctorSlots(3L, "2026-09-20");

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getId());
        assertEquals("08:00", result.get(0).getStartTime());
        assertEquals("08:30", result.get(0).getEndTime());
        assertEquals("AVAILABLE", result.get(0).getStatus());
        verify(scheduleService).getAvailableSlots(3L, LocalDate.of(2026, 9, 20));
    }

    @Test
    void propagatesInvalidDateFormat() {
        ScheduleService scheduleService = mock(ScheduleService.class);
        ApiController controller = new ApiController(scheduleService);

        assertThrows(java.time.format.DateTimeParseException.class,
                () -> controller.getDoctorSlots(3L, "20-09-2026"));
        verifyNoInteractions(scheduleService);
    }
}
