package com.medclinic.service;

import com.medclinic.entity.Appointment;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(Long patientId, Long doctorId, LocalDate date, Long timeSlotId, String reason);
    Appointment bookWalkInAppointment(String fullName, String phone, Long doctorId, LocalDate date, Long timeSlotId, String reason);
    void cancelAppointment(Long appointmentId, Long patientId);
    Appointment checkInAppointment(Long appointmentId);
    Appointment findById(Long id);
    Appointment findByBookingCode(String bookingCode);
    List<Appointment> findByPatient(Long patientId);
    List<Appointment> findTodayQueueByDoctor(Long doctorId);
    List<Appointment> findTodayAppointments();
    List<Appointment> searchForReception(String keyword);
}
