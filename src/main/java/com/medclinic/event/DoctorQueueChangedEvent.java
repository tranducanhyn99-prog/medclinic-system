package com.medclinic.event;

import com.medclinic.enums.AppointmentStatus;

import java.time.Instant;

public record DoctorQueueChangedEvent(
        Long doctorId,
        String doctorUsername,
        Long appointmentId,
        AppointmentStatus status,
        ChangeType changeType,
        Instant occurredAt
) {
    public enum ChangeType {
        CHECKED_IN,
        EXAMINATION_STARTED,
        EXAMINATION_COMPLETED
    }
}
