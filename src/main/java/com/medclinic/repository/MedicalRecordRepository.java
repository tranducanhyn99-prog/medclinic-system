package com.medclinic.repository;

import com.medclinic.entity.MedicalRecord;
import com.medclinic.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    @EntityGraph(attributePaths = {"appointment", "patient"})
    Page<MedicalRecord> findByDoctor_User_IdAndAppointment_Status(Long doctorUserId, AppointmentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"appointment", "doctor", "doctor.user", "doctor.specialty", "patient", "prescription", "prescription.items", "prescription.items.medicine"})
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    @EntityGraph(attributePaths = {"appointment", "doctor", "doctor.user", "doctor.specialty", "patient", "prescription", "prescription.items", "prescription.items.medicine"})
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

}

