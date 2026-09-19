package com.medclinic.event;

import java.time.LocalDate;

/** Published inside the booking transaction and delivered only after it commits. */
public record AppointmentEmailEvent(Type type, String email, String patientName, String bookingCode,
                                    String doctorName, LocalDate appointmentDate, String timeRange) {
    public enum Type { BOOKED, CANCELLED }
}
