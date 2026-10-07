package com.medclinic;

import com.medclinic.entity.Appointment;
import com.medclinic.entity.Invoice;
import com.medclinic.entity.User;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.PdfExportService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfUnicodeFontTest {
    @Test
    void invoiceEmbedsBundledUnicodeFontWithoutSystemFont() {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = Appointment.builder().patient(User.builder().id(7L).fullName("Nguyễn Thị Ánh").build()).build();
        Invoice invoice = Invoice.builder().id(4L).invoiceCode("HD-ĐẶC-BIỆT").appointment(appointment)
                .consultationFee(BigDecimal.TEN).medicineFee(BigDecimal.ZERO).totalAmount(BigDecimal.TEN).build();
        when(invoices.findById(4L)).thenReturn(Optional.of(invoice));

        byte[] pdf = new PdfExportService(appointments, invoices).exportInvoicePdf(4L, 7L, "ROLE_PATIENT");

        String rawPdf = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(rawPdf.startsWith("%PDF"));
        assertTrue(rawPdf.contains("/FontFile2"), "Font Unicode phải được nhúng trong PDF");
    }
}
