package com.medclinic;

import com.medclinic.entity.*;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.MedicalRecordService;
import com.medclinic.service.impl.*;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClinicalDateBoundaryTest {
    // UTC is still October 4; reception uses October 5 in Vietnam.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T17:00:01Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    static Stream<LocalDate> invalidDates() { return Stream.of(null, TODAY.minusDays(1), TODAY.plusDays(1)); }

    @ParameterizedTest @MethodSource("invalidDates")
    void checkInCannotAllocateQueueOrPublishEventsForAnotherDay(LocalDate date) {
        var appointments = mock(AppointmentRepository.class);
        var queue = mock(DailyQueueCounterAllocator.class);
        var events = mock(ApplicationEventPublisher.class);
        var service = booking(appointments, mock(UserRepository.class), queue, events);
        var appointment = appointment(date, AppointmentStatus.CONFIRMED);
        when(appointments.findByIdForUpdate(1L)).thenReturn(Optional.of(appointment));
        assertThrows(BusinessLogicException.class, () -> service.checkInAppointment(1L));
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertNull(appointment.getQueueSequence());
        verify(appointments, never()).save(any());
        verifyNoInteractions(queue, events);
    }

    @ParameterizedTest @MethodSource("invalidDates")
    void walkInRejectsTheDateBeforeCreatingOrLookingUpAPatient(LocalDate date) {
        var appointments = mock(AppointmentRepository.class);
        var users = mock(UserRepository.class);
        var queue = mock(DailyQueueCounterAllocator.class);
        var events = mock(ApplicationEventPublisher.class);
        var service = booking(appointments, users, queue, events);
        assertThrows(BusinessLogicException.class, () -> service.bookWalkInAppointment(
                "DEMO", "0900000020", null, null, null, 1L, date, 1L, "DEMO"));
        verifyNoInteractions(users, appointments, queue, events);
    }

    @ParameterizedTest @MethodSource("invalidDates")
    void startRejectsOtherDaysEvenForLegacyCheckedInAndInProgressAppointments(LocalDate date) {
        for (var status : List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS)) {
            var f = examination(date, status);
            assertThrows(BusinessLogicException.class, () -> f.service.startExamination(1L, 10L));
            assertEquals(status, f.appointment.getStatus());
            verify(f.appointments, never()).save(any());
            verifyNoInteractions(f.events);
        }
    }

    @ParameterizedTest @MethodSource("invalidDates")
    void saveRejectsOtherDaysBeforeRecordStockInvoiceOrEventWrites(LocalDate date) {
        var f = examination(date, AppointmentStatus.IN_PROGRESS);
        assertThrows(BusinessLogicException.class, () -> f.service.saveMedicalRecordAndPrescription(
                1L, 10L, "Sốt", "Cảm cúm", null, null, List.of()));
        assertEquals(AppointmentStatus.IN_PROGRESS, f.appointment.getStatus());
        verify(f.appointments, never()).save(any());
        verifyNoInteractions(f.records, f.medicines, f.inventory, f.invoices, f.events);
    }

    @ParameterizedTest @MethodSource("invalidDates")
    void activeFormCannotBypassTheDateGuard(LocalDate date) {
        var f = examination(date, AppointmentStatus.IN_PROGRESS);
        when(f.appointments.findById(1L)).thenReturn(Optional.of(f.appointment));
        assertThrows(BusinessLogicException.class, () -> f.service.getAppointmentForExamination(1L, 10L));
    }

    private AppointmentService booking(AppointmentRepository appointments, UserRepository users,
            DailyQueueCounterAllocator queue, ApplicationEventPublisher events) {
        var service = new AppointmentService(appointments, mock(TimeSlotRepository.class), users,
                mock(RoleRepository.class), mock(DoctorProfileRepository.class), mock(PasswordEncoder.class), queue, events);
        service.setBusinessClock(CLOCK);
        return service;
    }
    private Appointment appointment(LocalDate date, AppointmentStatus status) {
        return Appointment.builder().id(1L).appointmentDate(date).status(status)
                .doctor(DoctorProfile.builder().id(20L).user(User.builder().id(10L).username("doctor").build()).build()).build();
    }
    private Fixture examination(LocalDate date, AppointmentStatus status) {
        var appointments = mock(AppointmentRepository.class);
        var records = mock(MedicalRecordRepository.class);
        var medicines = mock(MedicineRepository.class);
        var inventory = mock(InventoryTransactionRepository.class);
        var invoices = mock(InvoiceService.class);
        var events = mock(ApplicationEventPublisher.class);
        var service = new MedicalRecordService(records, appointments, medicines, inventory, invoices, events);
        service.setBusinessClock(CLOCK);
        var appointment = appointment(date, status);
        when(appointments.findByIdForUpdate(1L)).thenReturn(Optional.of(appointment));
        return new Fixture(service, appointment, appointments, records, medicines, inventory, invoices, events);
    }
    private record Fixture(MedicalRecordService service, Appointment appointment, AppointmentRepository appointments,
            MedicalRecordRepository records, MedicineRepository medicines, InventoryTransactionRepository inventory,
            InvoiceService invoices, ApplicationEventPublisher events) {}
}
