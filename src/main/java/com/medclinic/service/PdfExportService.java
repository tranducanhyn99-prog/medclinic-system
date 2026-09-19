package com.medclinic.service;

public interface PdfExportService {
    byte[] exportPrescriptionPdf(Long appointmentId, Long requesterId, String requesterRole);
    byte[] exportInvoicePdf(Long invoiceId, Long requesterId, String requesterRole);
}
