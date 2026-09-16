package com.medclinic.service.impl;

import com.medclinic.entity.*;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.*;
import com.medclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Appointment bookAppointment(Long patientId, Long doctorId, LocalDate date, Long timeSlotId, String reason) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        TimeSlot slot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ"));


        if (!slot.getSchedule().getDoctor().getId().equals(doctorId)
                || !slot.getSchedule().getWorkDate().equals(date)) {
            throw new BusinessLogicException("Khung giờ không thuộc bác sĩ hoặc ngày khám đã chọn.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessLogicException("Ngày khám không thể ở quá khứ.");
        }
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new BusinessLogicException("Khung giờ này đã được đặt hoặc không khả dụng.");
        }

        try {
            slot.setStatus(SlotStatus.BOOKED);
            timeSlotRepository.saveAndFlush(slot);
        } catch (OptimisticLockingFailureException e) {
            throw new BusinessLogicException("Khung giờ này vừa có người đặt trước! Vui lòng chọn khung giờ khác.");
        }

        String bookingCode = "MED-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setTimeSlot(slot);
        appointment.setReason(reason);
        appointment.setBookingCode(bookingCode);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment bookWalkInAppointment(String fullName, String phone, Long doctorId, LocalDate date, Long timeSlotId, String reason) {
        User patient = userRepository.findByPhone(phone).orElse(null);
        if (patient == null) {
            patient = new User();
            patient.setFullName(fullName);
            patient.setPhone(phone);
            patient.setUsername(phone);
            patient.setPassword("123456");
            patient.setEmail(phone + "@walkin.medclinic.vn");
            patient.setIsActive(true);
            Role role = roleRepository.findByName(RoleType.ROLE_PATIENT)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ROLE_PATIENT"));
            patient.setRole(role);
            patient = userRepository.save(patient);
        }

        Appointment appointment = bookAppointment(patient.getId(), doctorId, date, timeSlotId, reason);
        return checkInAppointment(appointment.getId());
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
        
        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessLogicException("Bạn không có quyền hủy cuộc hẹn này");
        }
        
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessLogicException("Chỉ có thể hủy cuộc hẹn ở trạng thái đã xác nhận");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        TimeSlot slot = appointment.getTimeSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            timeSlotRepository.save(slot);
        }
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment checkInAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessLogicException("Chỉ có thể check-in cho cuộc hẹn đã xác nhận");
        }

        long countToday = appointmentRepository.countQueueForDate(appointment.getAppointmentDate());
        
        String queueNumber = String.format("STT-%02d", countToday + 1);
        appointment.setQueueNumber(queueNumber);
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment findByBookingCode(String bookingCode) {
        return appointmentRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn với mã này"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findTodayQueueByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueNumberAsc(
                doctorId, LocalDate.now(), List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findTodayAppointments() {
        return appointmentRepository.findByAppointmentDateOrderByTimeSlot_StartTimeAsc(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> searchForReception(String keyword) {
        return appointmentRepository.searchForReception(keyword);
    }
}
