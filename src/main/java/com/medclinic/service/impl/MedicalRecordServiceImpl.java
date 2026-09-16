package com.medclinic.service.impl;

import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.*;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.MedicalRecordRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;

    @Override
    @Transactional
    public MedicalRecord saveMedicalRecordAndPrescription(Long appointmentId, Long doctorUserId, String symptoms, String diagnosis, String doctorNotes, LocalDate reExamDate, List<PrescriptionItemDto> prescriptionItems) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new BusinessLogicException("Bạn không có quyền hoàn tất ca khám này");
        }
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN
                && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessLogicException("Chỉ có thể khám bệnh nhân đã check-in");
        }
        if (medicalRecordRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BusinessLogicException("Ca khám này đã có bệnh án");
        }


        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setAppointment(appointment);
        medicalRecord.setDoctor(appointment.getDoctor());
        medicalRecord.setPatient(appointment.getPatient());
        medicalRecord.setSymptoms(symptoms);
        medicalRecord.setDiagnosis(diagnosis);
        medicalRecord.setDoctorNotes(doctorNotes);
        medicalRecord.setReExamDate(reExamDate);

        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(medicalRecord);
        
        List<PrescriptionItem> items = new ArrayList<>();
        if (prescriptionItems != null && !prescriptionItems.isEmpty()) {
            for (PrescriptionItemDto dto : prescriptionItems) {
                Medicine medicine = medicineRepository.findById(dto.getMedicineId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc ID " + dto.getMedicineId()));
                
                if (medicine.getStockQuantity() < dto.getQuantity()) {
                    throw new BusinessLogicException("Thuốc " + medicine.getName() + " không đủ số lượng tồn kho (Còn lại: " + medicine.getStockQuantity() + ")");
                }
                
                medicine.setStockQuantity(medicine.getStockQuantity() - dto.getQuantity());
                medicineRepository.save(medicine);
                
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(prescription);
                item.setMedicine(medicine);
                item.setQuantity(dto.getQuantity());
                item.setDosage(dto.getDosage());
                item.setFrequency(dto.getFrequency());
                item.setInstructions(dto.getInstructions());
                items.add(item);
            }
        }
        
        prescription.setItems(items);
        medicalRecord.setPrescription(prescription);

        return medicalRecordRepository.save(medicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecord> getPatientMedicalHistory(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecord findByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh án cho cuộc hẹn này"));
    }
}
