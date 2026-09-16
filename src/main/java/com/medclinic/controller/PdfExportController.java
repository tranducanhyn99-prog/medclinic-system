package com.medclinic.controller;

import com.medclinic.entity.Appointment;
import com.medclinic.entity.Invoice;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class PdfExportController {

    private final AppointmentService appointmentService;
    private final InvoiceService invoiceService;
    private final PdfExportService pdfExportService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/prescription/{appointmentId}")
    public ResponseEntity<InputStreamResource> exportPrescription(@PathVariable Long appointmentId,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Appointment appointment = appointmentService.findById(appointmentId);
        if (!canAccessPrescription(appointment, userDetails)) {
            throw new BusinessLogicException("Bạn không có quyền tải đơn thuốc này");
        }
        byte[] pdfBytes = pdfExportService.exportPrescriptionPdf(appointmentId);
        ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=don_thuoc_" + appointmentId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bais));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<InputStreamResource> exportInvoice(@PathVariable Long invoiceId,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Invoice invoice = invoiceService.findById(invoiceId);
        if (!canAccessInvoice(invoice, userDetails)) {
            throw new BusinessLogicException("Bạn không có quyền tải hóa đơn này");
        }
        byte[] pdfBytes = pdfExportService.exportInvoicePdf(invoiceId);
        ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa_don_" + invoiceId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bais));

    }
    private boolean canAccessPrescription(Appointment appointment, CustomUserDetails userDetails) {
        return hasRole(userDetails, RoleType.ROLE_ADMIN)
                || appointment.getPatient().getId().equals(userDetails.getId())
                || appointment.getDoctor().getUser().getId().equals(userDetails.getId());
    }

    private boolean canAccessInvoice(Invoice invoice, CustomUserDetails userDetails) {
        return hasRole(userDetails, RoleType.ROLE_ADMIN)
                || hasRole(userDetails, RoleType.ROLE_STAFF)
                || invoice.getAppointment().getPatient().getId().equals(userDetails.getId());
    }

    private boolean hasRole(CustomUserDetails userDetails, RoleType role) {
        return role.name().equals(userDetails.getRoleName());
    }
}
