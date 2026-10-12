package com.medclinic.service;

import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.*;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.InventoryTransactionType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.event.DoctorQueueChangedEvent;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InventoryTransactionRepository;
import com.medclinic.repository.MedicalRecordRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.util.PrescriptionValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private java.time.Clock businessClock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @org.springframework.beans.factory.annotation.Autowired
    public void setBusinessClock(java.time.Clock clock) { this.businessClock = clock; }

    private boolean earlyClinicalProcessing;
    @org.springframework.beans.factory.annotation.Autowired
    public void configureClinicalDemo(org.springframework.core.env.Environment environment) {
        earlyClinicalProcessing = com.medclinic.util.ClinicalDatePolicy.earlyProcessingEnabled(environment);
    }

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InvoiceService invoiceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @Transactional(readOnly = true)
    public Appointment getAppointmentForExamination(Long appointmentId, Long doctorUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
        verifyDoctor(appointment, doctorUserId);
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) requireClinicalDate(appointment);
        return appointment;
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @Transactional
    public Appointment startExamination(Long appointmentId, Long doctorUserId) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
        verifyDoctor(appointment, doctorUserId);
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessLogicException("Chỉ có thể bắt đầu khám bệnh nhân đã check-in");
        }
        requireClinicalDate(appointment);
        if (appointment.getStatus() == AppointmentStatus.CHECKED_IN) {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
            Appointment saved = appointmentRepository.save(appointment);
            publishQueueChange(saved, DoctorQueueChangedEvent.ChangeType.EXAMINATION_STARTED);
            return saved;
        }
        return appointment;
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @Transactional
    public MedicalRecord saveMedicalRecordAndPrescription(Long appointmentId, Long doctorUserId, String symptoms, String diagnosis, String doctorNotes, LocalDate reExamDate, List<PrescriptionItemDto> prescriptionItems) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
        verifyDoctor(appointment, doctorUserId);
        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessLogicException("Ca khám phải được bắt đầu trước khi hoàn tất");
        }
        requireClinicalDate(appointment);
        if (medicalRecordRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BusinessLogicException("Ca khám này đã có bệnh án");
        }
        if (symptoms == null || symptoms.isBlank() || symptoms.length() > 5000
                || diagnosis == null || diagnosis.isBlank() || diagnosis.length() > 5000
                || (doctorNotes != null && doctorNotes.length() > 10000)
                || (reExamDate != null && !reExamDate.isAfter(LocalDate.now(businessClock)))) {
            throw new BusinessLogicException("Triệu chứng, chẩn đoán hoặc ngày tái khám không hợp lệ.");
        }
        if (prescriptionItems != null && (prescriptionItems.size() > PrescriptionValidation.MAX_ITEMS
                || prescriptionItems.stream().anyMatch(dto -> dto == null || !PrescriptionValidation.validRow(
                    dto.getMedicineId(), dto.getQuantity(), dto.getDosage(), dto.getFrequency(), dto.getInstructions())))) {
            throw new BusinessLogicException("Đơn thuốc không hợp lệ: tối đa 100 dòng, số lượng phải lớn hơn 0, liều dùng và tần suất tối đa 100 ký tự, hướng dẫn tối đa 255 ký tự.");
        }

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
            Map<Long, Integer> quantitiesByMedicine = new TreeMap<>();
            for (PrescriptionItemDto dto : prescriptionItems) {
                try {
                    quantitiesByMedicine.merge(dto.getMedicineId(), dto.getQuantity(), Math::addExact);
                } catch (ArithmeticException ex) {
                    throw new BusinessLogicException("Tổng số lượng của một thuốc vượt giới hạn cho phép.");
                }
            }
            Map<Long, Medicine> lockedMedicines = new HashMap<>();
            for (Map.Entry<Long, Integer> entry : quantitiesByMedicine.entrySet()) {
                Medicine medicine = medicineRepository.findByIdForUpdate(entry.getKey())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc ID " + entry.getKey()));
                if (!Boolean.TRUE.equals(medicine.getIsActive())) {
                    throw new BusinessLogicException("Thuốc " + medicine.getName() + " không còn được phép kê đơn");
                }
                if (medicine.getStockQuantity() < entry.getValue()) {
                    throw new BusinessLogicException("Thuốc " + medicine.getName() + " không đủ số lượng tồn kho (Còn lại: " + medicine.getStockQuantity() + ")");
                }
                int stockBefore = medicine.getStockQuantity();
                int stockAfter = stockBefore - entry.getValue();
                medicine.setStockQuantity(stockAfter);
                medicineRepository.save(medicine);
                inventoryTransactionRepository.save(InventoryTransaction.builder()
                        .medicine(medicine).type(InventoryTransactionType.DISPENSE)
                        .quantityChange(-entry.getValue()).stockBefore(stockBefore).stockAfter(stockAfter)
                        .referenceType("APPOINTMENT").referenceId(appointment.getId())
                        .note("Xuất kho theo đơn thuốc " + appointment.getBookingCode())
                        .performedBy(appointment.getDoctor().getUser()).build());
                lockedMedicines.put(entry.getKey(), medicine);
            }
            for (PrescriptionItemDto dto : prescriptionItems) {
                Medicine medicine = lockedMedicines.get(dto.getMedicineId());
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(prescription);
                item.setMedicine(medicine);
                item.setQuantity(dto.getQuantity());
                item.setUnitPriceAtTime(medicine.getUnitPrice());
                item.setDosage(dto.getDosage());
                item.setFrequency(dto.getFrequency());
                item.setInstructions(dto.getInstructions());
                items.add(item);
            }
        }

        prescription.setItems(items);
        medicalRecord.setPrescription(prescription);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);
        invoiceService.createInvoiceForAppointment(appointmentId, prescription.calculateTotalMedicinePrice());
        publishQueueChange(appointment, DoctorQueueChangedEvent.ChangeType.EXAMINATION_COMPLETED);
        return saved;
    }

    @PreAuthorize("hasAnyRole('DOCTOR','PATIENT','ADMIN')")
    @Transactional(readOnly = true)
    public List<MedicalRecord> getPatientMedicalHistory(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public MedicalRecord findByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh án cho cuộc hẹn này"));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional(readOnly = true)
    public Page<MedicalRecord> getCompletedRecords(Long doctorUserId, int page) {
        return medicalRecordRepository.findByDoctor_User_IdAndAppointment_Status(doctorUserId, AppointmentStatus.COMPLETED,
                PageRequest.of(Math.max(0, Math.min(1_000_000, page)), 25,
                        Sort.by(Sort.Direction.DESC, "appointment.appointmentDate", "id")));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional(readOnly = true)
    public MedicalRecord getCompletedRecordForDoctor(Long appointmentId, Long doctorUserId) {
        MedicalRecord record = findByAppointmentId(appointmentId);
        verifyDoctor(record.getAppointment(), doctorUserId);
        if (record.getAppointment().getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessLogicException("Ca khám chưa hoàn tất. Hãy tiếp tục tại bàn khám.");
        }
        return record;
    }

    private void publishQueueChange(Appointment appointment, DoctorQueueChangedEvent.ChangeType changeType) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getUser() == null
                || appointment.getDoctor().getUser().getUsername() == null
                || appointment.getDoctor().getUser().getUsername().isBlank()) {
            return;
        }
        applicationEventPublisher.publishEvent(new DoctorQueueChangedEvent(
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getUsername(),
                appointment.getId(), appointment.getStatus(), changeType, Instant.now()));
    }

    private void verifyDoctor(Appointment appointment, Long doctorUserId) {
        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new BusinessLogicException("Bạn không có quyền thao tác ca khám này");
        }
    }

    private void requireClinicalDate(Appointment appointment) {
        com.medclinic.util.ClinicalDatePolicy.requireProcessableDate(
                appointment.getAppointmentDate(), businessClock, earlyClinicalProcessing);
    }
}
