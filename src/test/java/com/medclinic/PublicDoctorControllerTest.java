package com.medclinic;

import com.medclinic.controller.PublicDoctorController;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Specialty;
import com.medclinic.entity.User;
import com.medclinic.service.DoctorService;
import com.medclinic.service.SpecialtyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicDoctorControllerTest {

    @Mock private DoctorService doctorService;
    @Mock private SpecialtyService specialtyService;
    @InjectMocks private PublicDoctorController publicDoctorController;

    @Test
    void displaysDoctorsWithSelectedFilters() {
        Specialty specialty = Specialty.builder().id(2L).name("Nội khoa").build();
        DoctorProfile doctor = DoctorProfile.builder()
                .id(10L)
                .specialty(specialty)
                .user(User.builder().fullName("Nguyễn Văn An").build())
                .consultationFee(new BigDecimal("300000"))
                .experienceYears(6)
                .title("BS.CKI")
                .build();
        ExtendedModelMap model = new ExtendedModelMap();

        when(doctorService.filterDoctors(2L, new BigDecimal("300000"), " An ")).thenReturn(List.of(doctor));
        when(specialtyService.findAllActive()).thenReturn(List.of(specialty));

        String view = publicDoctorController.listDoctors(2L, new BigDecimal("300000"), " An ", model);

        assertEquals("doctors/list", view);
        assertEquals(List.of(doctor), model.getAttribute("doctors"));
        assertEquals("An", model.getAttribute("keyword"));
        verify(doctorService).filterDoctors(2L, new BigDecimal("300000"), " An ");
        verify(specialtyService).findAllActive();
    }

    @Test
    void displaysDoctorProfile() {
        DoctorProfile doctor = DoctorProfile.builder().id(10L).build();
        ExtendedModelMap model = new ExtendedModelMap();
        when(doctorService.findById(10L)).thenReturn(doctor);

        String view = publicDoctorController.doctorDetail(10L, model);

        assertEquals("doctors/detail", view);
        assertEquals(doctor, model.getAttribute("doctor"));
        verify(doctorService).findById(10L);
    }
}
