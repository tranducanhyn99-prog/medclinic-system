package com.medclinic.repository;

import com.medclinic.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    @EntityGraph(attributePaths = {"appointment", "doctor", "doctor.user", "patient", "prescription", "prescription.items", "prescription.items.medicine"})
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    @EntityGraph(attributePaths = {"appointment", "doctor", "doctor.user", "patient", "prescription", "prescription.items", "prescription.items.medicine"})
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @EntityGraph(attributePaths = {"appointment", "doctor", "doctor.user", "patient", "prescription", "prescription.items", "prescription.items.medicine"})
    List<MedicalRecord> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
}

