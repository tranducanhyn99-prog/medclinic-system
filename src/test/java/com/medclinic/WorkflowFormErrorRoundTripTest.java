package com.medclinic;

import com.medclinic.controller.DoctorDeskController;
import com.medclinic.controller.PatientAppointmentController;
import com.medclinic.dto.BookingFormDto;
import com.medclinic.entity.Role;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkflowFormErrorRoundTripTest {
    @Test
    void bookingConflictRendersFormWithSubmittedValuesAndDoesNotCreateAppointment() {
        AppointmentService appointments = mock(AppointmentService.class);
        DoctorService doctors = mock(DoctorService.class);
        SpecialtyService specialties = mock(SpecialtyService.class);
        PatientAppointmentController controller = new PatientAppointmentController(appointments, doctors, specialties,
                mock(MedicalRecordService.class));
        BookingFormDto form = new BookingFormDto();
        form.setSpecialtyId(3L); form.setDoctorId(4L); form.setDate(LocalDate.now().plusDays(1));
        form.setSlotId(5L); form.setReason("Đau đầu");
        when(specialties.findAllActive()).thenReturn(List.of());
        when(doctors.findAll()).thenReturn(List.of());
        doThrow(new BusinessLogicException("Khung giờ này đã được đặt")).when(appointments)
                .bookAppointment(9L, 4L, form.getDate(), 5L, "Đau đầu");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.processBooking(user(9L, RoleType.ROLE_PATIENT), form,
                new BeanPropertyBindingResult(form, "bookingForm"), model, new RedirectAttributesModelMap());

        assertEquals("patient/booking", view);
        assertSame(form, model.getAttribute("bookingForm"));
        assertEquals("Khung giờ này đã được đặt", model.getAttribute("errorMessage"));
        assertNotNull(model.getAttribute("specialties"));
        assertNotNull(model.getAttribute("doctors"));
        verify(appointments).bookAppointment(9L, 4L, form.getDate(), 5L, "Đau đầu");
    }

    @Test
    void failedStartExaminationReturnsToDeskWithErrorMessage() {
        MedicalRecordService records = mock(MedicalRecordService.class);
        DoctorDeskController controller = new DoctorDeskController(mock(DoctorService.class), mock(AppointmentService.class),
                records, mock(MedicineService.class), mock(ScheduleService.class), java.time.Clock.systemUTC());
        doThrow(new BusinessLogicException("Chỉ có thể bắt đầu khám bệnh nhân đã check-in"))
                .when(records).startExamination(11L, 7L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.startExamination(11L, user(7L, RoleType.ROLE_DOCTOR), redirect);

        assertEquals("redirect:/doctor/desk", view);
        assertEquals("Chỉ có thể bắt đầu khám bệnh nhân đã check-in", redirect.getFlashAttributes().get("errorMessage"));
        verify(records).startExamination(11L, 7L);
        verifyNoMoreInteractions(records);
    }

    private CustomUserDetails user(Long id, RoleType role) {
        return new CustomUserDetails(User.builder().id(id).username("u" + id).isActive(true)
                .role(Role.builder().name(role).build()).build());
    }
}
