package com.medclinic.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.name:MedClinic - Phòng Khám Đa Khoa Tư Nhân}")
    private String clinicName = "MedClinic - Phòng Khám Đa Khoa Tư Nhân";
    @Value("${app.address:123 Đường Nguyễn Trãi, Quận 5, TP. Hồ Chí Minh}")
    private String clinicAddress = "123 Đường Nguyễn Trãi, Quận 5, TP. Hồ Chí Minh";
    @Value("${app.hotline:1900 6868}")
    private String clinicHotline = "1900 6868";
    @Value("${app.email:support@medclinic.vn}")
    private String clinicEmail = "support@medclinic.vn";

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
                String directions = java.util.stream.Stream.of(item.getFrequency(), item.getInstructions())
                        .filter(value -> value != null && !value.isBlank()).map(String::trim)
                        .distinct().collect(java.util.stream.Collectors.joining("; "));
                addCell(table, directions, font);
            }
            document.add(table);

            document.add(new Paragraph(" "));
            String notes = medicalRecord.getPrescription().getDoctorNotes();
            if (notes == null || notes.isBlank()) notes = medicalRecord.getDoctorNotes();
            Paragraph doctorNote = new Paragraph("Lời dặn: " + (notes != null ? notes : ""), font);
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

    @Transactional(readOnly = true)
    public byte[] exportInvoicePdf(Long invoiceId, Long requesterId, String requesterRole) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
        if (!"ROLE_ADMIN".equals(requesterRole) && !"ROLE_STAFF".equals(requesterRole)
                && !invoice.getAppointment().getPatient().getId().equals(requesterId)) {
            throw new BusinessLogicException("Bạn không có quyền tải hóa đơn này");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 36, 48);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            new InvoicePdfLayout(getFont(10, Font.NORMAL), getFont(10, Font.BOLD),
                    getFont(8, Font.NORMAL), getFont(18, Font.BOLD), getFont(22, Font.BOLD))
                    .write(document, writer, invoice, clinicName, clinicAddress, clinicHotline, clinicEmail);

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
