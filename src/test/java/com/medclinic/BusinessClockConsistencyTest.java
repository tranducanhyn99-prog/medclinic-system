package com.medclinic;

import com.medclinic.controller.DoctorDeskController;
import com.medclinic.service.*;
import com.medclinic.service.impl.InvoiceCodeReservationCoordinator;
import com.medclinic.service.impl.InvoiceCodeReservationWriter;
import com.medclinic.repository.*;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import java.time.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BusinessClockConsistencyTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"2026-10-10T16:30:00Z,2026-10-10", "2026-10-10T17:30:00Z,2026-10-11"})
    void dashboardScheduleAndInvoiceUseVietnameseDateAtDayBoundary(String instant, String expectedDate) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate localDay = LocalDate.parse(expectedDate);
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        DashboardService dashboard = new DashboardService(mock(UserRepository.class), mock(DoctorProfileRepository.class),
                appointments, mock(InvoiceRepository.class), clock);
        dashboard.countTodayAppointments();
        verify(appointments).countByAppointmentDate(localDay);
        var writer = mock(InvoiceCodeReservationWriter.class);
        assertTrue(new InvoiceCodeReservationCoordinator(writer, clock).reserveUniqueCode().startsWith("INV-"
                + localDay.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "-"));
        DoctorService doctors = mock(DoctorService.class);
        ScheduleService schedules = mock(ScheduleService.class);
        DoctorProfile profile = DoctorProfile.builder().id(5L).build();
        when(doctors.findByUserId(7L)).thenReturn(profile);
        DoctorDeskController controller = new DoctorDeskController(doctors, mock(AppointmentService.class),
                mock(MedicalRecordService.class), mock(MedicineService.class), schedules, clock);
        controller.showDoctorSchedule(new CustomUserDetails(User.builder().id(7L).build()), new ExtendedModelMap());
        verify(schedules).findByDoctorAndDateRange(5L, localDay, localDay.plusDays(14));
    }
}
