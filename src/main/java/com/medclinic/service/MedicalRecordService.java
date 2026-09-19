package com.medclinic.service;

import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.Appointment;
import com.medclinic.entity.MedicalRecord;

import java.time.LocalDate;
import java.util.List;

public interface MedicalRecordService {
    Appointment getAppointmentForExamination(Long appointmentId, Long doctorUserId);
    Appointment startExamination(Long appointmentId, Long doctorUserId);

    MedicalRecord saveMedicalRecordAndPrescription(Long appointmentId, Long doctorUserId, String symptoms, String diagnosis, String doctorNotes, LocalDate reExamDate, List<PrescriptionItemDto> prescriptionItems);
    List<MedicalRecord> getPatientMedicalHistory(Long patientId);
    MedicalRecord findByAppointmentId(Long appointmentId);
}
