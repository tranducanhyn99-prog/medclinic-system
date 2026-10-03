package com.medclinic;

import tools.jackson.databind.ObjectMapper;
import com.medclinic.controller.AdminController;
import com.medclinic.dto.AdminMedicineDto;
import com.medclinic.dto.AdminSpecialtyDto;
import com.medclinic.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Verifies server-side form retention independently of browser JavaScript. */
class AdminControllerFormRetentionTest {
    private final SpecialtyService specialties = mock(SpecialtyService.class);
    private final DoctorService doctors = mock(DoctorService.class);
    private final MedicineService medicines = mock(MedicineService.class);
    private final AdminController controller = new AdminController(mock(DashboardService.class), specialties, doctors,
            medicines, mock(UserService.class), new ObjectMapper());

    @Test
    void invalidSpecialtyRendersSameFormObjectAndReferenceData() {
        AdminSpecialtyDto form = new AdminSpecialtyDto();
        form.setName("");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "specialtyForm");
        errors.rejectValue("name", "NotBlank", "Tên chuyên khoa không được để trống");
        ExtendedModelMap model = new ExtendedModelMap();
        when(specialties.findAll()).thenReturn(List.of());

        String view = controller.addSpecialty(form, errors, model, new RedirectAttributesModelMap());

        assertEquals("admin/specialties", view);
        assertSame(form, model.getAttribute("specialtyForm"));
        verify(specialties).findAll();
        verifyNoMoreInteractions(specialties);
    }

    @Test
    void existingMedicineUsesUpdateAndRedirectsOnValidSubmission() {
        AdminMedicineDto form = new AdminMedicineDto();
        form.setId(42L);
        form.setName("Paracetamol");
        form.setUnit("viên");
        form.setUnitPrice(new BigDecimal("1500"));
        form.setUsageGuide("Sau ăn");
        form.setStockQuantity(10);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "medicineForm");

        String view = controller.saveMedicine(form, errors, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("redirect:/admin/medicines", view);
        verify(medicines).updateCatalog(42L, "Paracetamol", "viên", new BigDecimal("1500"), "Sau ăn", 50);
        verify(medicines, never()).save(anyString(), anyString(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void invalidMedicineRendersSameFormObjectAndReferenceData() {
        AdminMedicineDto form = new AdminMedicineDto();
        form.setName("Thuốc A");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "medicineForm");
        errors.rejectValue("unitPrice", "NotNull", "Đơn giá không được để trống");
        ExtendedModelMap model = new ExtendedModelMap();
        when(medicines.findAll()).thenReturn(List.of());

        String view = controller.saveMedicine(form, errors, model, new RedirectAttributesModelMap());

        assertEquals("admin/medicines", view);
        assertSame(form, model.getAttribute("medicineForm"));
        verify(medicines).findAll();
        verify(medicines, never()).save(anyString(), anyString(), any(), any(), anyInt(), anyInt());
    }
}
