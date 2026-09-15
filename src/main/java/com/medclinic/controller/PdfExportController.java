package com.medclinic.controller;

import com.medclinic.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class PdfExportController {

    private final PdfExportService pdfExportService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/prescription/{appointmentId}")
    public ResponseEntity<InputStreamResource> exportPrescription(@PathVariable Long appointmentId) {
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
    public ResponseEntity<InputStreamResource> exportInvoice(@PathVariable Long invoiceId) {
        byte[] pdfBytes = pdfExportService.exportInvoicePdf(invoiceId);
        ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa_don_" + invoiceId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bais));
    }
}
