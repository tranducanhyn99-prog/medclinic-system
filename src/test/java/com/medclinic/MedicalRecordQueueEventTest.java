package com.medclinic;

import com.medclinic.entity.Appointment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.MedicalRecord;
import com.medclinic.entity.User;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.event.DoctorQueueChangedEvent;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.MedicalRecordRepository;
import com.medclinic.repository.InventoryTransactionRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.impl.MedicalRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicalRecordQueueEventTest {

    @Test
    void startExaminationPublishesQueueChange() {
        Fixture fixture = fixture(AppointmentStatus.CHECKED_IN);
        when(fixture.appointmentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(fixture.appointment));
        when(fixture.appointmentRepository.save(fixture.appointment)).thenReturn(fixture.appointment);
        fixture.service.startExamination(1L, 10L);

        assertEquals(AppointmentStatus.IN_PROGRESS, fixture.appointment.getStatus());
        DoctorQueueChangedEvent event = captureEvent(fixture.eventPublisher);
        assertEquals(DoctorQueueChangedEvent.ChangeType.EXAMINATION_STARTED, event.changeType());
        assertEquals("doctor1", event.doctorUsername());
    }

    @Test
    void completingExaminationPublishesQueueChangeAfterBusinessWork() {
        Fixture fixture = fixture(AppointmentStatus.IN_PROGRESS);
        when(fixture.appointmentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(fixture.appointment));
        when(fixture.medicalRecordRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(fixture.medicalRecordRepository.save(any(MedicalRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        fixture.service.saveMedicalRecordAndPrescription(
                1L, 10L, "Sốt", "Cảm cúm", "Nghỉ ngơi", null, List.of());

        assertEquals(AppointmentStatus.COMPLETED, fixture.appointment.getStatus());
        verify(fixture.invoiceService).createInvoiceForAppointment(1L, BigDecimal.ZERO);
        DoctorQueueChangedEvent event = captureEvent(fixture.eventPublisher);
        assertEquals(DoctorQueueChangedEvent.ChangeType.EXAMINATION_COMPLETED, event.changeType());
    }
    private DoctorQueueChangedEvent captureEvent(ApplicationEventPublisher publisher) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        return (DoctorQueueChangedEvent) captor.getValue();
    }

    private Fixture fixture(AppointmentStatus status) {
        MedicalRecordRepository medicalRecordRepository = mock(MedicalRecordRepository.class);
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        MedicineRepository medicineRepository = mock(MedicineRepository.class);
        InventoryTransactionRepository inventoryTransactionRepository = mock(InventoryTransactionRepository.class);
        InvoiceService invoiceService = mock(InvoiceService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        MedicalRecordServiceImpl service = new MedicalRecordServiceImpl(
                medicalRecordRepository, appointmentRepository, medicineRepository, inventoryTransactionRepository, invoiceService, eventPublisher);

        User doctorUser = User.builder().id(10L).username("doctor1").build();
        DoctorProfile doctor = DoctorProfile.builder().id(20L).user(doctorUser).build();
        Appointment appointment = Appointment.builder()
                .id(1L).doctor(doctor).status(status).build();

        return new Fixture(service, medicalRecordRepository, appointmentRepository,
                invoiceService, eventPublisher, appointment);
    }

    private record Fixture(
            MedicalRecordServiceImpl service,
            MedicalRecordRepository medicalRecordRepository,
            AppointmentRepository appointmentRepository,
            InvoiceService invoiceService,
            ApplicationEventPublisher eventPublisher,
            Appointment appointment) {
    }
}
