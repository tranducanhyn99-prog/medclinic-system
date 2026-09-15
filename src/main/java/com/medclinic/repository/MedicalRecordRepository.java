package com.medclinic.repository;

import com.medclinic.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<MedicalRecord> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
}

