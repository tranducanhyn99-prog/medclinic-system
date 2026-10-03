package com.medclinic.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.medclinic.entity.*;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.util.InvoiceMoneyFormat;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Layout for the invoice export; prices come from the invoice/prescription snapshots. */
final class InvoicePdfLayout {
    private static final Color INK = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color TEAL = new Color(15, 100, 94);
    private static final Color LINE = new Color(218, 226, 230);
    private static final Color TINT = new Color(237, 246, 244);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Font normal;
    private final Font bold;
    private final Font small;
    private final Font title;
    private final Font brand;

    InvoicePdfLayout(Font normal, Font bold, Font small, Font title, Font brand) {
        this.normal = normal;
        this.bold = bold;
        this.small = small;
        this.title = title;
        this.brand = brand;
    }

    void write(Document document, PdfWriter writer, Invoice invoice, String clinicName,
               String address, String hotline, String email) throws DocumentException {
        normal.setColor(INK);
        bold.setColor(INK);
        small.setColor(MUTED);
        title.setColor(TEAL);
        brand.setColor(TEAL);
        String code = value(invoice.getInvoiceCode() != null ? invoice.getInvoiceCode() : invoice.getId());
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onEndPage(PdfWriter pdfWriter, Document page) {
                PdfContentByte canvas = pdfWriter.getDirectContent();
                canvas.saveState();
                canvas.setColorStroke(LINE);
                canvas.moveTo(page.left(), page.bottom() - 10);
                canvas.lineTo(page.right(), page.bottom() - 10);
                canvas.stroke();
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase("MedClinic - " + code, small), page.left(), page.bottom() - 25, 0);
                ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                        new Phrase("Trang " + pdfWriter.getPageNumber(), small), page.right(), page.bottom() - 25, 0);
                canvas.restoreState();
            }
        });
        document.addTitle("Hóa đơn thanh toán " + code);
        document.addAuthor(clinicName);
        document.open();

        PdfPTable header = table(new float[]{3, 2});
        PdfPCell clinic = plainCell();
        clinic.addElement(new Paragraph("MEDCLINIC", brand));
        clinic.addElement(new Paragraph(value(clinicName), small));
        clinic.addElement(new Paragraph(value(address), small));
        clinic.addElement(new Paragraph("Hotline: " + value(hotline), small));
        clinic.addElement(new Paragraph(value(email), small));
        header.addCell(clinic);
        PdfPCell reference = plainCell();
        reference.addElement(right("MÃ HÓA ĐƠN", small));
        reference.addElement(right(code, bold));
        reference.addElement(right("Ngày lập: " + dateTime(invoice.getCreatedAt()), small));
        reference.addElement(right(invoice.getPaymentStatus() == null ? "Chưa thanh toán"
                : invoice.getPaymentStatus().getDisplayName().toUpperCase(java.util.Locale.ROOT), bold));
        header.addCell(reference);
        header.setSpacingAfter(16);
        document.add(header);

        Paragraph heading = new Paragraph("HÓA ĐƠN THANH TOÁN", title);
        heading.setAlignment(Element.ALIGN_CENTER);
        heading.setSpacingAfter(4);
        document.add(heading);
        Paragraph caption = new Paragraph("DỊCH VỤ KHÁM BỆNH VÀ THUỐC", small);
        caption.setAlignment(Element.ALIGN_CENTER);
        caption.setSpacingAfter(18);
        document.add(caption);

        Appointment appointment = invoice.getAppointment();
        User patient = appointment.getPatient();
        DoctorProfile doctor = appointment.getDoctor();
        PdfPTable details = table(new float[]{1, 1});
        details.addCell(detail("Bệnh nhân", patient.getFullName()));
        details.addCell(detail("Mã lịch hẹn", appointment.getBookingCode()));
        details.addCell(detail("Điện thoại", patient.getPhone()));
        details.addCell(detail("Ngày khám", appointment.getAppointmentDate() == null ? "-"
                : appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        details.addCell(detail("Ngày sinh", patient.getDateOfBirth() == null ? "-"
                : patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        details.addCell(detail("Bác sĩ", doctor == null || doctor.getUser() == null ? "-" : doctor.getUser().getFullName()));
        PdfPCell patientAddress = detail("Địa chỉ", patient.getAddress());
        patientAddress.setColspan(2);
        details.addCell(patientAddress);
        details.setSpacingAfter(18);
        document.add(details);

        PdfPTable charges = table(new float[]{0.65f, 3.0f, 0.85f, 0.6f, 1.55f, 1.7f});
        String[] labels = {"STT", "Nội dung", "ĐVT", "SL", "Đơn giá", "Thành tiền"};
        for (int i = 0; i < labels.length; i++) {
            PdfPCell cell = chargeCell(labels[i], bold, i >= 4 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            cell.setBackgroundColor(TINT);
            charges.addCell(cell);
        }
        charges.setHeaderRows(1);
        charges.setSplitLate(false);
        charges.setSpacingAfter(14);
        int index = 1;
        charge(charges, index++, "Phí khám bệnh", "Lượt", "1", invoice.getConsultationFee(), invoice.getConsultationFee());
        MedicalRecord record = appointment.getMedicalRecord();
        Prescription prescription = record == null ? null : record.getPrescription();
        List<PrescriptionItem> items = prescription == null ? List.of() : prescription.getItems();
        // Legacy invoices may have only aggregate fees. Do not invent detail or a current medicine price.
        if (items != null && !items.isEmpty() && invoice.getMedicineFee() != null
                && prescription.calculateTotalMedicinePrice().compareTo(invoice.getMedicineFee()) == 0) {
            for (PrescriptionItem item : items) {
                Medicine medicine = item.getMedicine();
                charge(charges, index++, medicine == null ? "Thuốc theo đơn" : medicine.getName(),
                        medicine == null ? "-" : medicine.getUnit(), value(item.getQuantity()),
                        item.getUnitPriceAtTime(), item.calculateSubtotal());
            }
        } else if (invoice.getMedicineFee() != null && invoice.getMedicineFee().signum() != 0) {
            charge(charges, index, "Phí thuốc theo đơn", "Đơn", "1", invoice.getMedicineFee(), invoice.getMedicineFee());
        }
        document.add(charges);

        PdfPTable settlement = table(new float[]{1});
        settlement.setKeepTogether(true);
        PdfPCell settlementCell = plainCell();
        PdfPTable totals = table(new float[]{3, 2});
        total(totals, "Tổng phí khám", invoice.getConsultationFee(), false);
        total(totals, "Tổng tiền thuốc", invoice.getMedicineFee(), false);
        total(totals, "TỔNG THANH TOÁN (VND)", invoice.getTotalAmount(), true);
        settlementCell.addElement(totals);
        String words = InvoiceMoneyFormat.words(invoice.getTotalAmount());
        if (words != null) settlementCell.addElement(new Paragraph("Bằng chữ: " + words + ".", normal));
        settlementCell.addElement(new Paragraph("Phương thức: " + (invoice.getPaymentMethod() == null ? "-"
                : invoice.getPaymentMethod().getDisplayName()), normal));
        if (invoice.getPaidAt() != null) {
            settlementCell.addElement(new Paragraph("Thời gian thanh toán: " + dateTime(invoice.getPaidAt()), normal));
        } else {
            settlementCell.addElement(new Paragraph("Thời gian thanh toán: Chưa ghi nhận", normal));
        }
        if (invoice.getPaymentStatus() != PaymentStatus.PAID) {
            settlementCell.addElement(new Paragraph("Trạng thái: " + (invoice.getPaymentStatus() == null
                    ? "Chưa thanh toán" : invoice.getPaymentStatus().getDisplayName()), bold));
        }
        PdfPTable signatures = table(new float[]{1, 1});
        signatures.setSpacingBefore(22);
        signatures.addCell(signature("Người thanh toán"));
        signatures.addCell(signature("Người thu tiền"));
        settlementCell.addElement(signatures);
        Paragraph note = new Paragraph("Vui lòng giữ chứng từ này để đối chiếu khi cần hỗ trợ.", small);
        note.setSpacingBefore(8);
        settlementCell.addElement(note);
        settlement.addCell(settlementCell);
        document.add(settlement);
    }

    private PdfPTable table(float[] widths) throws DocumentException {
        PdfPTable table = new PdfPTable(widths.length);
        table.setWidths(widths);
        table.setWidthPercentage(100);
        return table;
    }

    private PdfPCell plainCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        return cell;
    }

    private PdfPCell detail(String label, Object value) {
        PdfPCell cell = plainCell();
        cell.setBackgroundColor(new Color(247, 249, 250));
        cell.setPadding(8);
        cell.addElement(new Paragraph(label.toUpperCase(java.util.Locale.ROOT), small));
        cell.addElement(new Paragraph(value(value), normal));
        return cell;
    }

    private PdfPCell chargeCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(value(text), font));
        cell.setPadding(8);
        cell.setBorderColor(LINE);
        cell.setBorderWidth(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private void charge(PdfPTable table, int index, String content, String unit, String quantity,
                        BigDecimal price, BigDecimal subtotal) {
        String[] row = {String.valueOf(index), content, unit, quantity,
                InvoiceMoneyFormat.number(price), InvoiceMoneyFormat.number(subtotal)};
        for (int i = 0; i < row.length; i++) table.addCell(chargeCell(row[i], normal,
                i >= 4 ? Element.ALIGN_RIGHT : i == 0 || i == 3 ? Element.ALIGN_CENTER : Element.ALIGN_LEFT));
    }

    private void total(PdfPTable table, String label, BigDecimal amount, boolean grandTotal) {
        PdfPCell left = chargeCell(label, grandTotal ? bold : normal, Element.ALIGN_LEFT);
        PdfPCell right = chargeCell(InvoiceMoneyFormat.number(amount), grandTotal ? bold : normal, Element.ALIGN_RIGHT);
        if (grandTotal) {
            left.setBackgroundColor(TINT);
            right.setBackgroundColor(TINT);
        } else {
            left.setBorder(Rectangle.NO_BORDER);
            right.setBorder(Rectangle.NO_BORDER);
        }
        table.addCell(left);
        table.addCell(right);
    }

    private PdfPCell signature(String label) {
        PdfPCell cell = plainCell();
        Paragraph name = new Paragraph(label, bold);
        name.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(name);
        Paragraph instruction = new Paragraph("(Ký, ghi rõ họ tên)", small);
        instruction.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(instruction);
        cell.setPaddingBottom(52);
        return cell;
    }

    private Paragraph right(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    private String dateTime(LocalDateTime time) { return time == null ? "-" : time.format(DATE_TIME); }
    private String value(Object text) { return text == null || text.toString().isBlank() ? "-" : text.toString(); }
}
