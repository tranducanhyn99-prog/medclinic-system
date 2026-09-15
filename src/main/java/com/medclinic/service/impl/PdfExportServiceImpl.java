package com.medclinic.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medclinic.entity.*;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportServiceImpl implements PdfExportService {

    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportPrescriptionPdf(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
                
        MedicalRecord medicalRecord = appointment.getMedicalRecord();
        if (medicalRecord == null || medicalRecord.getPrescription() == null) {
            throw new ResourceNotFoundException("Không tìm thấy đơn thuốc");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font font = getFont(12, Font.NORMAL);
            Font boldFont = getFont(14, Font.BOLD);
            Font titleFont = getFont(18, Font.BOLD);

            Paragraph clinicName = new Paragraph("PHONG KHAM MEDCLINIC", titleFont);
            clinicName.setAlignment(Element.ALIGN_CENTER);
            document.add(clinicName);

            Paragraph title = new Paragraph("DON THUOC", boldFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(20);
            document.add(title);

            User patient = appointment.getPatient();
            document.add(new Paragraph("Ho ten benh nhan: " + patient.getFullName(), font));
            document.add(new Paragraph("Chan doan: " + medicalRecord.getDiagnosis(), font));
            
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1, 2, 3});

            addCell(table, "STT", boldFont);
            addCell(table, "Ten thuoc", boldFont);
            addCell(table, "SL", boldFont);
            addCell(table, "Lieu dung", boldFont);
            addCell(table, "Cach dung", boldFont);

            int stt = 1;
            for (PrescriptionItem item : medicalRecord.getPrescription().getItems()) {
                addCell(table, String.valueOf(stt++), font);
                addCell(table, item.getMedicine().getName(), font);
                addCell(table, String.valueOf(item.getQuantity()), font);
                addCell(table, item.getDosage(), font);
                addCell(table, item.getInstructions() != null ? item.getInstructions() : "", font);
            }
            document.add(table);

            document.add(new Paragraph(" "));
            Paragraph doctorNote = new Paragraph("Loi dan: " + (medicalRecord.getPrescription().getDoctorNotes() != null ? medicalRecord.getPrescription().getDoctorNotes() : ""), font);
            document.add(doctorNote);

            document.add(new Paragraph(" "));
            Paragraph doctorSign = new Paragraph("Bac si khao sat", boldFont);
            doctorSign.setAlignment(Element.ALIGN_RIGHT);
            document.add(doctorSign);
            
            Paragraph doctorName = new Paragraph(appointment.getDoctor().getUser().getFullName(), font);
            doctorName.setAlignment(Element.ALIGN_RIGHT);
            doctorName.setSpacingBefore(40);
            document.add(doctorName);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating prescription PDF", e);
            throw new RuntimeException("Lỗi tạo PDF đơn thuốc", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font font = getFont(12, Font.NORMAL);
            Font boldFont = getFont(14, Font.BOLD);
            Font titleFont = getFont(18, Font.BOLD);

            Paragraph clinicName = new Paragraph("PHONG KHAM MEDCLINIC", titleFont);
            clinicName.setAlignment(Element.ALIGN_CENTER);
            document.add(clinicName);

            Paragraph title = new Paragraph("HOA DON THANH TOAN", boldFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(20);
            document.add(title);

            User patient = invoice.getAppointment().getPatient();
            document.add(new Paragraph("Ma hoa don: " + (invoice.getInvoiceCode() != null ? invoice.getInvoiceCode() : String.valueOf(invoice.getId())), font));
            document.add(new Paragraph("Ho ten benh nhan: " + patient.getFullName(), font));
            document.add(new Paragraph("Ngay thanh toan: " + (invoice.getPaidAt() != null ? invoice.getPaidAt().toString() : ""), font));
            document.add(new Paragraph("Hinh thuc thanh toan: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().getDisplayName() : ""), font));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addCell(table, "Khoan phi", boldFont);
            addCell(table, "Thanh tien (VND)", boldFont);

            addCell(table, "Phi kham", font);
            addCell(table, String.valueOf(invoice.getConsultationFee()), font);

            addCell(table, "Phi thuoc", font);
            addCell(table, String.valueOf(invoice.getMedicineFee()), font);

            addCell(table, "Tong tien", boldFont);
            addCell(table, String.valueOf(invoice.getTotalAmount()), boldFont);

            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating invoice PDF", e);
            throw new RuntimeException("Lỗi tạo PDF hóa đơn", e);
        }
    }
    
    private Font getFont(int size, int style) {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, style);
        } catch (Exception e) {
            return new Font(Font.HELVETICA, size, style);
        }
    }
    
    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
