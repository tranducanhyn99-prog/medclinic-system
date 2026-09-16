package com.medclinic;

import com.medclinic.controller.PdfExportController;
import com.medclinic.entity.Appointment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Invoice;
import com.medclinic.entity.Role;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.PdfExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportControllerTest {

    @Mock private PdfExportService pdfExportService;
    @Mock private AppointmentService appointmentService;
    @Mock private InvoiceService invoiceService;
    @InjectMocks private PdfExportController pdfExportController;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        User patient = user(1L, RoleType.ROLE_PATIENT);
        User doctorUser = user(2L, RoleType.ROLE_DOCTOR);
        appointment = Appointment.builder()
                .id(10L)
                .patient(patient)
                .doctor(DoctorProfile.builder().user(doctorUser).build())
                .build();
    }

    @Test
    @DisplayName("Bệnh nhân không sở hữu không thể tải đơn thuốc")
    void rejectsPrescriptionDownloadForAnotherPatient() {
        when(appointmentService.findById(10L)).thenReturn(appointment);
        CustomUserDetails anotherPatient = new CustomUserDetails(user(3L, RoleType.ROLE_PATIENT));

        assertThrows(BusinessLogicException.class,
                () -> pdfExportController.exportPrescription(10L, anotherPatient));

        verify(pdfExportService, never()).exportPrescriptionPdf(10L);
    }

    @Test
    @DisplayName("Lễ tân được phép tải hóa đơn")
    void allowsInvoiceDownloadForStaff() {
        Invoice invoice = Invoice.builder().id(20L).appointment(appointment).build();
        when(invoiceService.findById(20L)).thenReturn(invoice);
        when(pdfExportService.exportInvoicePdf(20L)).thenReturn(new byte[]{1, 2, 3});
        CustomUserDetails staff = new CustomUserDetails(user(4L, RoleType.ROLE_STAFF));

        pdfExportController.exportInvoice(20L, staff);

        verify(pdfExportService).exportInvoicePdf(20L);
    }

    private User user(Long id, RoleType roleType) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .role(Role.builder().name(roleType).build())
                .isActive(true)
                .build();
    }
}
