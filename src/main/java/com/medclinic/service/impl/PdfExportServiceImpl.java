package com.medclinic.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medclinic.entity.*;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportServiceImpl implements PdfExportService {

    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportPrescriptionPdf(Long appointmentId, Long requesterId, String requesterRole) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hẹn"));
        if (!"ROLE_ADMIN".equals(requesterRole)
                && !appointment.getPatient().getId().equals(requesterId)
                && !appointment.getDoctor().getUser().getId().equals(requesterId)) {
            throw new BusinessLogicException("Bạn không có quyền tải đơn thuốc này");
        }
                
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

            Paragraph clinicName = new Paragraph("PHÒNG KHÁM MEDCLINIC", titleFont);
            clinicName.setAlignment(Element.ALIGN_CENTER);
            document.add(clinicName);

            Paragraph title = new Paragraph("ĐƠN THUỐC", boldFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(20);
            document.add(title);

            User patient = appointment.getPatient();
            document.add(new Paragraph("Họ tên bệnh nhân: " + patient.getFullName(), font));
            document.add(new Paragraph("Chẩn đoán: " + medicalRecord.getDiagnosis(), font));
            
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1, 2, 3});

            addCell(table, "STT", boldFont);
            addCell(table, "Tên thuốc", boldFont);
            addCell(table, "SL", boldFont);
            addCell(table, "Liều dùng", boldFont);
            addCell(table, "Cách dùng", boldFont);

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
            Paragraph doctorNote = new Paragraph("Lời dặn: " + (medicalRecord.getPrescription().getDoctorNotes() != null ? medicalRecord.getPrescription().getDoctorNotes() : ""), font);
            document.add(doctorNote);

            document.add(new Paragraph(" "));
            Paragraph doctorSign = new Paragraph("Bác sĩ khám bệnh", boldFont);
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
    public byte[] exportInvoicePdf(Long invoiceId, Long requesterId, String requesterRole) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
        if (!"ROLE_ADMIN".equals(requesterRole) && !"ROLE_STAFF".equals(requesterRole)
                && !invoice.getAppointment().getPatient().getId().equals(requesterId)) {
            throw new BusinessLogicException("Bạn không có quyền tải hóa đơn này");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font font = getFont(12, Font.NORMAL);
            Font boldFont = getFont(14, Font.BOLD);
            Font titleFont = getFont(18, Font.BOLD);

            Paragraph clinicName = new Paragraph("PHÒNG KHÁM MEDCLINIC", titleFont);
            clinicName.setAlignment(Element.ALIGN_CENTER);
            document.add(clinicName);

            Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN", boldFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(20);
            document.add(title);

            User patient = invoice.getAppointment().getPatient();
            document.add(new Paragraph("Mã hóa đơn: " + (invoice.getInvoiceCode() != null ? invoice.getInvoiceCode() : String.valueOf(invoice.getId())), font));
            document.add(new Paragraph("Họ tên bệnh nhân: " + patient.getFullName(), font));
            document.add(new Paragraph("Ngày thanh toán: " + (invoice.getPaidAt() != null ? invoice.getPaidAt().toString() : ""), font));
            document.add(new Paragraph("Hình thức thanh toán: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().getDisplayName() : ""), font));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addCell(table, "Khoản phí", boldFont);
            addCell(table, "Thành tiền (VND)", boldFont);

            addCell(table, "Phí khám", font);
            addCell(table, String.valueOf(invoice.getConsultationFee()), font);

            addCell(table, "Phí thuốc", font);
            addCell(table, String.valueOf(invoice.getMedicineFee()), font);

            addCell(table, "Tổng tiền", boldFont);
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
            ClassPathResource fontResource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            try (InputStream fontStream = fontResource.getInputStream()) {
                BaseFont baseFont = BaseFont.createFont("NotoSans-Regular.ttf", BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED, true, fontStream.readAllBytes(), null);
                return new Font(baseFont, size, style);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải font Unicode để tạo PDF", e);
        }
    }
    
    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
