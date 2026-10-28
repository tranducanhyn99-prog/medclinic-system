package com.medclinic.service;

import com.medclinic.entity.*;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.enums.Gender;
import com.medclinic.event.AppointmentEmailEvent;
import com.medclinic.event.DoctorQueueChangedEvent;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.*;
import com.medclinic.service.impl.DailyQueueCounterAllocator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private java.time.Clock businessClock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @org.springframework.beans.factory.annotation.Autowired
    public void setBusinessClock(java.time.Clock clock) { this.businessClock = clock; }

    private boolean earlyClinicalProcessing;
    @org.springframework.beans.factory.annotation.Autowired
    public void configureClinicalDemo(org.springframework.core.env.Environment environment) {
        earlyClinicalProcessing = com.medclinic.util.ClinicalDatePolicy.earlyProcessingEnabled(environment);
    }

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final DailyQueueCounterAllocator dailyQueueCounterAllocator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PreAuthorize("#patientId == principal.id or hasAnyRole('STAFF','ADMIN')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Appointment bookAppointment(Long patientId, Long doctorId, LocalDate date, Long timeSlotId, String reason) {
        LocalDate today = LocalDate.now(businessClock);
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        if (!com.medclinic.util.DoctorAvailability.acceptsBookings(doctor)) {
            throw new BusinessLogicException("Bác sĩ hoặc chuyên khoa hiện không nhận lịch khám.");
        }
        if (date == null || date.isBefore(today) || date.isAfter(today.plusDays(14))) {
            throw new BusinessLogicException("Ngày khám phải nằm trong 14 ngày kể từ hôm nay.");
        }
        TimeSlot slot = timeSlotRepository.findByIdForUpdate(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ"));


        if (!slot.getSchedule().getDoctor().getId().equals(doctorId)
                || !slot.getSchedule().getWorkDate().equals(date)) {
            throw new BusinessLogicException("Khung giờ không thuộc bác sĩ hoặc ngày khám đã chọn.");
        }
        if (date.equals(today) && !slot.getStartTime().isAfter(LocalTime.now(businessClock))) {
            throw new BusinessLogicException("Khung giờ đã bắt đầu hoặc đã qua, vui lòng chọn khung giờ khác.");
        }
        if (reason == null || reason.trim().isEmpty() || reason.trim().length() > 1000) {
            throw new BusinessLogicException("Lý do khám là bắt buộc và không được vượt quá 1000 ký tự.");
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

        String bookingCode = generateUniqueBookingCode();

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setTimeSlot(slot);
        appointment.setReason(reason.trim());
        appointment.setBookingCode(bookingCode);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Appointment saved = appointmentRepository.save(appointment);
        publishEmail(saved, AppointmentEmailEvent.Type.BOOKED);
        return saved;
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @Transactional
    public Appointment bookWalkInAppointment(String fullName, String phone, LocalDate dateOfBirth, Gender gender, String address,
                                             Long doctorId, LocalDate date, Long timeSlotId, String reason) {
        requireToday(date);
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BusinessLogicException("Họ tên bệnh nhân không được để trống");
        }
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now(businessClock))) {
            throw new BusinessLogicException("Ngày sinh không hợp lệ");
        }
        phone = com.medclinic.util.PhoneNumbers.normalize(phone, true);
        User patient = userRepository.findByPhone(phone).orElse(null);
        if (patient == null) {
            patient = new User();
            patient.setFullName(fullName.trim());
            patient.setPhone(phone);
            patient.setUsername("walkin-" + UUID.randomUUID());
            patient.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            patient.setEmail("walkin-" + UUID.randomUUID() + "@invalid.medclinic.local");
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setAddress(address == null ? null : address.trim());
            // A reception record is not an issued login account. It remains disabled
            // until the patient completes verified account registration/reset later.
            patient.setEmailVerified(false);
            patient.setIsActive(false);
            Role role = roleRepository.findByName(RoleType.ROLE_PATIENT)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ROLE_PATIENT"));
            patient.setRole(role);
            try {
                patient = userRepository.saveAndFlush(patient);
            } catch (org.springframework.dao.DataIntegrityViolationException exception) {
                throw new BusinessLogicException("Số điện thoại vừa được đăng ký ở một phiên khác. Vui lòng tìm lại hồ sơ bệnh nhân.");
            }
        }

        Appointment appointment = bookAppointment(patient.getId(), doctorId, date, timeSlotId, reason);
        return checkInAppointment(appointment.getId());
    }

    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @Transactional
    public void cancelAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
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
        publishEmail(appointment, AppointmentEmailEvent.Type.CANCELLED);
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @Transactional
    public Appointment checkInAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessLogicException("Chỉ có thể check-in cho cuộc hẹn đã xác nhận");
        }

        com.medclinic.util.ClinicalDatePolicy.requireProcessableDate(
                appointment.getAppointmentDate(), businessClock, earlyClinicalProcessing);
        long nextSequence = dailyQueueCounterAllocator.allocateNext(appointment.getAppointmentDate());
        String queueNumber = String.format("STT-%02d", nextSequence);
        appointment.setQueueNumber(queueNumber);
        appointment.setQueueSequence(nextSequence);
        appointment.setStatus(AppointmentStatus.CHECKED_IN);

        Appointment saved = appointmentRepository.save(appointment);
        publishQueueChange(saved, DoctorQueueChangedEvent.ChangeType.CHECKED_IN);
        return saved;
    }

    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
    }

    @Transactional(readOnly = true)
    public Appointment findByBookingCode(String bookingCode) {
        return appointmentRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn với mã này"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findTodayQueueByDoctor(Long doctorId) {
        if (earlyClinicalProcessing) {
            return appointmentRepository.findByDoctorIdAndAppointmentDateBetweenAndStatusInOrderByAppointmentDateAscQueueSequenceAsc(
                    doctorId, LocalDate.now(businessClock),
                    com.medclinic.util.ClinicalDatePolicy.lastProcessableDate(businessClock, true),
                    List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS, AppointmentStatus.COMPLETED));
        }
        return appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueSequenceAsc(
                doctorId, LocalDate.now(businessClock), List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS, AppointmentStatus.COMPLETED));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findTodayAppointments() {
        if (earlyClinicalProcessing) {
            return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAscTimeSlot_StartTimeAsc(
                    LocalDate.now(businessClock), com.medclinic.util.ClinicalDatePolicy.lastProcessableDate(businessClock, true));
        }
        return appointmentRepository.findByAppointmentDateOrderByTimeSlot_StartTimeAsc(LocalDate.now(businessClock));
    }

    @Transactional(readOnly = true)
    public List<Appointment> searchForReception(String keyword, LocalDate date) {
        if (earlyClinicalProcessing) {
            return appointmentRepository.searchForReceptionBetween(keyword, LocalDate.now(businessClock),
                    com.medclinic.util.ClinicalDatePolicy.lastProcessableDate(businessClock, true));
        }
        return appointmentRepository.searchForReception(keyword, date);
    }

    private String generateUniqueBookingCode() {
        String prefix = "MED-" + LocalDate.now(businessClock).format(DateTimeFormatter.ofPattern("yyMMdd")) + "-";
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (appointmentRepository.findByBookingCode(code).isEmpty()) {
                return code;
            }
        }
        throw new BusinessLogicException("Không thể tạo mã đặt lịch duy nhất, vui lòng thử lại");
    }

    private void requireToday(LocalDate date) {
        if (!LocalDate.now(businessClock).equals(date)) {
            throw new BusinessLogicException("Chỉ tiếp nhận và cấp số thứ tự cho lịch khám hôm nay. Ngày khác vui lòng dùng chức năng đặt lịch.");
        }
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

    private void publishEmail(Appointment appointment, AppointmentEmailEvent.Type type) {
        String email = appointment.getPatient().getEmail();
        if (email == null || email.isBlank() || email.endsWith("@invalid.medclinic.local")) {
            return;
        }
        String doctorName = "Bác sĩ MedClinic";
        String timeRange = "";
        if (appointment.getDoctor() != null) {
            doctorName = appointment.getDoctor().getTitle();
            if (appointment.getDoctor().getUser() != null) {
                doctorName += " " + appointment.getDoctor().getUser().getFullName();
            }
        }
        if (appointment.getTimeSlot() != null) {
            timeRange = appointment.getTimeSlot().getTimeRangeFormatted();
        }
        applicationEventPublisher.publishEvent(new AppointmentEmailEvent(type, email,
                appointment.getPatient().getFullName(), appointment.getBookingCode(), doctorName,
                appointment.getAppointmentDate(), timeRange));
    }
}
