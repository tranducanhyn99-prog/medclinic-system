package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.service.impl.AppointmentServiceImpl;
import com.medclinic.service.impl.DailyQueueCounterAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private DailyQueueCounterRepository dailyQueueCounterRepository;

    @Mock
    private DailyQueueCounterAllocator dailyQueueCounterAllocator;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User patient;
    private DoctorProfile doctor;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .id(1L)
                .username("patient1")
                .fullName("Nguyễn Văn A")
                .email("patient1@example.com")
                .isActive(true)
                .build();

        doctor = DoctorProfile.builder()
                .id(1L)
                .title("BS.CKI")
                .build();
        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .workDate(LocalDate.now().plusDays(1)).build();

        timeSlot = TimeSlot.builder()
                .schedule(schedule)
                .id(100L)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("Đặt lịch khám thành công với slot AVAILABLE")
    void testBookAppointmentSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(timeSlotRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.saveAndFlush(any(TimeSlot.class))).thenReturn(timeSlot);

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(500L);
            return apt;
        });

        Appointment result = appointmentService.bookAppointment(1L, 1L, LocalDate.now().plusDays(1), 100L, "Đau đầu");

        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        assertTrue(result.getBookingCode().startsWith("MED-"));
        assertEquals(SlotStatus.BOOKED, timeSlot.getStatus());
        verify(timeSlotRepository, times(1)).saveAndFlush(timeSlot);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi slot không thuộc bác sĩ đã chọn")
    void testBookAppointmentSlotDoesNotBelongToDoctor() {
        DoctorProfile anotherDoctor = DoctorProfile.builder().id(2L).build();
        timeSlot.setSchedule(DoctorSchedule.builder()
                .doctor(anotherDoctor)
                .workDate(LocalDate.now().plusDays(1)).build());
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(timeSlotRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(timeSlot));

        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                appointmentService.bookAppointment(1L, 1L, LocalDate.now().plusDays(1), 100L, "Đau đầu"));

        assertTrue(ex.getMessage().contains("không thuộc"));
        verify(timeSlotRepository, never()).saveAndFlush(any(TimeSlot.class));
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi khung giờ đã bị đặt trước")
    void testBookAppointmentSlotAlreadyBooked() {
        timeSlot.setStatus(SlotStatus.BOOKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(timeSlotRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(timeSlot));

        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                appointmentService.bookAppointment(1L, 1L, LocalDate.now().plusDays(1), 100L, "Đau họng")
        );

        assertTrue(ex.getMessage().contains("không khả dụng") || ex.getMessage().contains("đã được đặt"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi xảy ra xung đột khóa Optimistic Lock")
    void testBookAppointmentOptimisticLockFailure() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(timeSlotRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(timeSlot));
        doThrow(new OptimisticLockingFailureException("Version conflict")).when(timeSlotRepository).saveAndFlush(any(TimeSlot.class));

        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                appointmentService.bookAppointment(1L, 1L, LocalDate.now().plusDays(1), 100L, "Sốt cao")
        );

        assertTrue(ex.getMessage().contains("người đặt trước"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Hủy lịch hẹn thành công và trả slot về AVAILABLE")
    void testCancelAppointmentSuccess() {
        Appointment apt = Appointment.builder()
                .id(500L)
                .patient(patient)
                .status(AppointmentStatus.CONFIRMED)
                .timeSlot(timeSlot)
                .build();
        timeSlot.setStatus(SlotStatus.BOOKED);

        when(appointmentRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(apt));

        appointmentService.cancelAppointment(500L, 1L);

        assertEquals(AppointmentStatus.CANCELLED, apt.getStatus());
        assertEquals(SlotStatus.AVAILABLE, timeSlot.getStatus());
        verify(timeSlotRepository, times(1)).save(timeSlot);
        verify(appointmentRepository, times(1)).save(apt);
    }

    @Test
    @DisplayName("Check-in lịch hẹn thành công và tự động sinh số thứ tự STT")
    void testCheckInAppointmentSuccess() {
        Appointment apt = Appointment.builder()
                .id(500L)
                .patient(patient)
                .appointmentDate(LocalDate.now())
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(apt));
        when(dailyQueueCounterAllocator.allocateNext(LocalDate.now())).thenReturn(3L);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment checkedIn = appointmentService.checkInAppointment(500L);

        assertEquals(AppointmentStatus.CHECKED_IN, checkedIn.getStatus());
        assertEquals("STT-03", checkedIn.getQueueNumber());
        verify(appointmentRepository, times(1)).save(apt);
    }

    @Test
    @DisplayName("Hàng chờ bác sĩ chỉ lấy ca đã check-in hoặc đang khám")
    void testFindTodayQueueByDoctor() {
        java.util.List<AppointmentStatus> queueStatuses = java.util.List.of(
                AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueSequenceAsc(
                1L, LocalDate.now(), queueStatuses)).thenReturn(java.util.List.of());

        assertTrue(appointmentService.findTodayQueueByDoctor(1L).isEmpty());

        verify(appointmentRepository).findByDoctorIdAndAppointmentDateAndStatusInOrderByQueueSequenceAsc(
                1L, LocalDate.now(), queueStatuses);
    }
}
