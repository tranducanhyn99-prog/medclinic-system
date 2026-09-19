package com.medclinic;

import com.medclinic.controller.PdfExportController;
import com.medclinic.enums.RoleType;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.PdfExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportControllerTest {

    @Mock private PdfExportService pdfExportService;
    @InjectMocks private PdfExportController pdfExportController;

    @Test
    @DisplayName("Controller chuyển danh tính người gọi đến service PDF")
    void passesRequesterToPrescriptionService() {
        CustomUserDetails patient = new CustomUserDetails(user(3L, RoleType.ROLE_PATIENT));
        when(pdfExportService.exportPrescriptionPdf(10L, 3L, "ROLE_PATIENT")).thenReturn(new byte[]{1});
        pdfExportController.exportPrescription(10L, patient);
        verify(pdfExportService).exportPrescriptionPdf(10L, 3L, "ROLE_PATIENT");
    }

    @Test
    @DisplayName("Lễ tân được phép tải hóa đơn")
    void allowsInvoiceDownloadForStaff() {
        CustomUserDetails staff = new CustomUserDetails(user(4L, RoleType.ROLE_STAFF));
        when(pdfExportService.exportInvoicePdf(20L, 4L, "ROLE_STAFF")).thenReturn(new byte[]{1});

        pdfExportController.exportInvoice(20L, staff);

        verify(pdfExportService).exportInvoicePdf(20L, 4L, "ROLE_STAFF");
    }

    private com.medclinic.entity.User user(Long id, RoleType roleType) {
        return com.medclinic.entity.User.builder()
                .id(id)
                .username("user" + id)
                .role(com.medclinic.entity.Role.builder().name(roleType).build())
                .isActive(true)
                .build();
    }
}
