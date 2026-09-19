package com.medclinic;

import com.medclinic.entity.Appointment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.DoctorSchedule;
import com.medclinic.entity.Role;
import com.medclinic.entity.TimeSlot;
import com.medclinic.entity.User;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.ShiftType;
import com.medclinic.enums.SlotStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.DoctorScheduleRepository;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
class ConcurrentBookingIntegrationTest {

    @Autowired private AppointmentService appointmentService;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired private TimeSlotRepository timeSlotRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void concurrentBookingOfTheSameSlotResultsInOneSuccess() throws Exception {
        DoctorProfile doctor = doctorProfileRepository.findAll().getFirst();
        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        String token = String.valueOf(System.nanoTime());
        LocalDate appointmentDate = LocalDate.now().plusDays(30);
        DoctorSchedule schedule = doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .workDate(appointmentDate)
                .shift(ShiftType.MORNING)
                .maxPatients(8)
                .build());

        TimeSlot slot = timeSlotRepository.save(TimeSlot.builder()
                .schedule(schedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .build());

        User patient1 = createPatient("patient1-" + token, patientRole);
        User patient2 = createPatient("patient2-" + token, patientRole);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            Future<Void> firstResult = executor.submit(() -> {
                bookAfterBarrier(patient1, doctor.getId(), appointmentDate, slot.getId(), ready, start, successCount, failureCount);
                return null;
            });
            Future<Void> secondResult = executor.submit(() -> {
                bookAfterBarrier(patient2, doctor.getId(), appointmentDate, slot.getId(), ready, start, successCount, failureCount);
                return null;
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            firstResult.get(15, TimeUnit.SECONDS);
            secondResult.get(15, TimeUnit.SECONDS);

            assertEquals(1, successCount.get());
            assertEquals(1, failureCount.get());
        } finally {
            executor.shutdownNow();
        }

        List<Appointment> appointments = appointmentRepository.findAll();
        long bookingsForSlot = appointments.stream()
                .filter(a -> a.getTimeSlot() != null && a.getTimeSlot().getId().equals(slot.getId()))
                .count();
        assertEquals(1, bookingsForSlot);
    }

    @Test
    void concurrentBookingOfDifferentSlotsOnSameDayResultsInSuccess() throws Exception {
        DoctorProfile doctor = doctorProfileRepository.findAll().getFirst();
        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        String token = String.valueOf(System.nanoTime());
        LocalDate appointmentDate = LocalDate.now().plusDays(31);
        DoctorSchedule schedule = doctorScheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .workDate(appointmentDate)
                .shift(ShiftType.MORNING)
                .maxPatients(8)
                .build());

        TimeSlot slot1 = timeSlotRepository.save(TimeSlot.builder()
                .schedule(schedule)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .build());

        TimeSlot slot2 = timeSlotRepository.save(TimeSlot.builder()
                .schedule(schedule)
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 0))
                .status(SlotStatus.AVAILABLE)
                .build());

        User patient1 = createPatient("patient3-" + token, patientRole);
        User patient2 = createPatient("patient4-" + token, patientRole);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            Future<Void> firstResult = executor.submit(() -> {
                bookAfterBarrier(patient1, doctor.getId(), appointmentDate, slot1.getId(), ready, start, successCount, failureCount);
                return null;
            });
            Future<Void> secondResult = executor.submit(() -> {
                bookAfterBarrier(patient2, doctor.getId(), appointmentDate, slot2.getId(), ready, start, successCount, failureCount);
                return null;
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            firstResult.get(15, TimeUnit.SECONDS);
            secondResult.get(15, TimeUnit.SECONDS);

            assertEquals(2, successCount.get());
            assertEquals(0, failureCount.get());
        } finally {
            executor.shutdownNow();
        }
    }

    private User createPatient(String patientSuffix, Role patientRole) {
        return userRepository.save(User.builder()
                .username("patient-" + patientSuffix)
                .password("encoded")
                .fullName("Patient " + patientSuffix)
                .email("patient-" + patientSuffix + "@example.test")
                .role(patientRole)
                .isActive(true)
                .build());
    }

    private void bookAfterBarrier(User patient, Long doctorId, LocalDate date, Long timeSlotId,
                                  CountDownLatch ready, CountDownLatch start,
                                  AtomicInteger successCount, AtomicInteger failureCount) {
        try {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(new CustomUserDetails(patient), "N/A",
                    List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))));
            SecurityContextHolder.setContext(context);

            appointmentService.bookAppointment(patient.getId(), doctorId, date, timeSlotId, "Checkup");
            successCount.incrementAndGet();
        } catch (BusinessLogicException e) {
            failureCount.incrementAndGet();
        } catch (Exception e) {
            throw new AssertionError("Booking failed for an unexpected reason", e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
