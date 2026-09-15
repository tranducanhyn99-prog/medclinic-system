package com.medclinic.service;

public interface PdfExportService {
    byte[] exportPrescriptionPdf(Long appointmentId);
    byte[] exportInvoicePdf(Long invoiceId);
}
