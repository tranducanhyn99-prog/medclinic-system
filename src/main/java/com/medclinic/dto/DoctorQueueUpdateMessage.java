package com.medclinic.dto;

import com.medclinic.enums.AppointmentStatus;
import com.medclinic.event.DoctorQueueChangedEvent;

import java.time.Instant;

public record DoctorQueueUpdateMessage(
        Long doctorId,
        Long appointmentId,
        AppointmentStatus status,
        DoctorQueueChangedEvent.ChangeType changeType,
        Instant occurredAt
) {
}
