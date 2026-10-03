package com.medclinic;

import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class MedicineStockConcurrencyIntegrationTest {
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private DoctorProfileRepository doctorProfiles;
    @Autowired private DoctorScheduleRepository schedules;
    @Autowired private TimeSlotRepository slots;
    @Autowired private AppointmentRepository appointments;
    @Autowired private MedicalRecordRepository records;
    @Autowired private MedicineRepository medicines;
    @Autowired private InventoryTransactionRepository inventoryTransactions;
    @Autowired private UserRepository users;

    @Test
    void concurrentPrescriptionsCannotOversellSingleRemainingUnit() throws Exception {
        User doctorUser = users.findByUsername("doctor").orElseThrow();
        DoctorProfile doctor = doctorProfiles.findByUserId(doctorUser.getId()).orElseThrow();
        User patient = users.findByUsername("patient").orElseThrow();
        Medicine medicine = medicines.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getIsActive())).findFirst().orElseThrow();
        medicine.setStockQuantity(1);
        medicines.saveAndFlush(medicine);

        LocalDate date = LocalDate.now().plusDays(90);
        DoctorSchedule schedule = schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date)
                .shift(ShiftType.MORNING).maxPatients(8).build());
        Appointment first = createInProgressAppointment(patient, doctor, schedule, date, LocalTime.of(8, 0));
        Appointment second = createInProgressAppointment(patient, doctor, schedule, date, LocalTime.of(8, 30));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger businessFailures = new AtomicInteger();
        try {
            Future<?> f1 = executor.submit(() -> completeAfterBarrier(first.getId(), doctorUser, medicine.getId(), ready, start, successes, businessFailures));
            Future<?> f2 = executor.submit(() -> completeAfterBarrier(second.getId(), doctorUser, medicine.getId(), ready, start, successes, businessFailures));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            f1.get(20, TimeUnit.SECONDS);
            f2.get(20, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertEquals(1, successes.get());
        assertEquals(1, businessFailures.get());
        assertEquals(0, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertEquals(1, inventoryTransactions.findAll().stream()
                .filter(tx -> tx.getMedicine().getId().equals(medicine.getId()) && tx.getType() == InventoryTransactionType.DISPENSE)
                .count());
        long recordCount = List.of(first, second).stream().filter(a -> records.findByAppointmentId(a.getId()).isPresent()).count();
        assertEquals(1, recordCount);
        long completedCount = List.of(first, second).stream()
                .map(a -> appointments.findById(a.getId()).orElseThrow().getStatus())
                .filter(s -> s == AppointmentStatus.COMPLETED).count();
        assertEquals(1, completedCount);
    }

    private Appointment createInProgressAppointment(User patient, DoctorProfile doctor, DoctorSchedule schedule,
                                                    LocalDate date, LocalTime start) {
        TimeSlot slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(start)
                .endTime(start.plusMinutes(30)).status(SlotStatus.BOOKED).build());
        return appointments.save(Appointment.builder().patient(patient).doctor(doctor).appointmentDate(date)
                .timeSlot(slot).bookingCode("SC-" + UUID.randomUUID().toString().substring(0, 8))
                .reason("Stock concurrency").status(AppointmentStatus.IN_PROGRESS).build());
    }

    private void completeAfterBarrier(Long appointmentId, User doctorUser, Long medicineId,
                                      CountDownLatch ready, CountDownLatch start,
                                      AtomicInteger successes, AtomicInteger businessFailures) {
        try {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(new CustomUserDetails(doctorUser), "N/A",
                    List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))));
            SecurityContextHolder.setContext(context);
            medicalRecordService.saveMedicalRecordAndPrescription(appointmentId, doctorUser.getId(), "S", "D", null, null,
                    List.of(PrescriptionItemDto.builder().medicineId(medicineId).quantity(1)
                            .dosage("1").frequency("1").instructions("").build()));
            successes.incrementAndGet();
        } catch (BusinessLogicException e) {
            businessFailures.incrementAndGet();
        } catch (Exception e) {
            throw new AssertionError("Unexpected concurrent stock failure", e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
