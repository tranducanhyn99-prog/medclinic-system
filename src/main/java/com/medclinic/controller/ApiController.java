package com.medclinic.controller;

import com.medclinic.dto.SlotDto;
import com.medclinic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class ApiController {

    private final ScheduleService scheduleService;

    @GetMapping("/doctors/{doctorId}/slots")
    public List<SlotDto> getDoctorSlots(@PathVariable Long doctorId, @RequestParam String date) {
        LocalDate scheduleDate = LocalDate.parse(date);
        return scheduleService.getAvailableSlots(doctorId, scheduleDate).stream()
                .map(slot -> SlotDto.builder()
                        .id(slot.getId())
                        .startTime(slot.getStartTime().toString())
                        .endTime(slot.getEndTime().toString())
                        .status(slot.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }
}
