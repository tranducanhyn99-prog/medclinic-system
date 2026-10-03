package com.medclinic;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.medclinic.entity.*;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.impl.PdfExportServiceImpl;
import com.medclinic.util.InvoiceMoneyFormat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoicePdfPresentationTest {
    @Test void moneyUsesSpacesWithoutDroppingNonZeroFractionalAmounts() {
        assertEquals("353 000", InvoiceMoneyFormat.number(new BigDecimal("353000.00")));
        assertEquals("1 234 567 890", InvoiceMoneyFormat.number(new BigDecimal("1234567890.00")));
        assertEquals("1 234,5", InvoiceMoneyFormat.number(new BigDecimal("1234.50")));
        assertEquals("0", InvoiceMoneyFormat.number(new BigDecimal("0.00")));
        assertEquals("Ba trăm năm mươi ba nghìn đồng", InvoiceMoneyFormat.words(new BigDecimal("353000.00")));
        assertEquals("Một triệu không trăm lẻ một đồng", InvoiceMoneyFormat.words(new BigDecimal("1000001")));
        assertEquals("Hai mươi mốt nghìn không trăm mười lăm đồng", InvoiceMoneyFormat.words(new BigDecimal("21015")));
        assertNull(InvoiceMoneyFormat.words(new BigDecimal("1.50")));
    }

    @Test void paidInvoiceIncludesActualLineItemsHistoricPriceDatesAndCorrectTotal() throws Exception {
        Invoice invoice = fixture(1);
        byte[] pdf = export(invoice, 7L);
        visualFixture("paid", pdf);
        try (PdfReader reader = new PdfReader(pdf)) {
            assertEquals(1, reader.getNumberOfPages());
            String text = text(reader);
            assertTrue(text.contains("Nguyễn Thị Ánh"));
            assertTrue(text.contains("353 000"));
            assertTrue(text.contains("1 500"));
            assertTrue(text.contains("3 000"));
            assertTrue(text.contains("02/10/2026 07:43"));
            assertTrue(text.contains("Ba trăm năm mươi ba nghìn đồng"));
            assertTrue(text.contains("Paracetamol 500mg"));
            assertTrue(text.contains("ĐÃ THANH TOÁN"));
            assertFalse(text.contains("999 999"), "Must use prescription price, not today's medicine price");
            assertFalse(text.contains("353000.00"));
            assertFalse(text.contains("2026-10-02T"));
        }
    }

    @Test void unpaidLegacyInvoiceDoesNotClaimPaymentAndFallsBackToAggregateMedicineFee() throws Exception {
        Invoice invoice = fixture(1);
        invoice.setPaymentStatus(PaymentStatus.UNPAID);
        invoice.setPaidAt(null);
        invoice.getAppointment().setMedicalRecord(null);
        byte[] pdf = export(invoice, 7L);
        visualFixture("unpaid", pdf);
        try (PdfReader reader = new PdfReader(pdf)) {
            String text = text(reader);
            assertTrue(text.contains("CHƯA THANH TOÁN"));
            assertTrue(text.contains("Chưa ghi nhận"));
            assertTrue(text.contains("Phí thuốc theo đơn"));
            assertTrue(text.contains("353 000"));
            assertFalse(text.contains("ĐÃ THANH TOÁN"));
        }
    }

    @Test void longInvoicePaginatesRepeatsTableHeadersAndKeepsFinalTotal() throws Exception {
        Invoice invoice = fixture(45);
        byte[] pdf = export(invoice, 7L);
        visualFixture("long", pdf);
        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() > 1);
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            assertTrue(extractor.getTextFromPage(2).contains("Đơn giá"));
            String last = extractor.getTextFromPage(reader.getNumberOfPages());
            assertTrue(last.contains("485 000"));
            assertTrue(last.contains("Người thu tiền"));
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                assertTrue(extractor.getTextFromPage(page).contains("Trang " + page));
            }
        }
        assertThrows(BusinessLogicException.class, () -> export(invoice, 999L));
    }

    private String text(PdfReader reader) throws Exception {
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder result = new StringBuilder();
        for (int page = 1; page <= reader.getNumberOfPages(); page++) result.append(extractor.getTextFromPage(page));
        return result.toString();
    }

    private byte[] export(Invoice invoice, Long requester) {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        when(invoices.findById(4L)).thenReturn(Optional.of(invoice));
        return new PdfExportServiceImpl(mock(AppointmentRepository.class), invoices)
                .exportInvoicePdf(4L, requester, "ROLE_PATIENT");
    }

    private void visualFixture(String name, byte[] pdf) throws Exception {
        if (!Boolean.getBoolean("invoice.pdf.visualFixtures")) return;
        Path directory = Path.of("tmp", "pdfs");
        Files.createDirectories(directory);
        Files.write(directory.resolve("invoice-" + name + ".pdf"), pdf);
    }

    private Invoice fixture(int count) {
        User patient = User.builder().id(7L).fullName("Nguyễn Thị Ánh").phone("0998001002")
                .dateOfBirth(LocalDate.of(2000, 1, 2)).address("Địa chỉ DEMO, TP. Hồ Chí Minh").build();
        DoctorProfile doctor = DoctorProfile.builder().user(User.builder().fullName("Nguyễn Văn Hùng").build()).build();
        Appointment appointment = Appointment.builder().patient(patient).doctor(doctor).bookingCode("MED-DEMO-1002")
                .appointmentDate(LocalDate.of(2026, 10, 2)).build();
        Prescription prescription = Prescription.builder().items(new ArrayList<>()).build();
        for (int i = 0; i < count; i++) {
            prescription.getItems().add(PrescriptionItem.builder().medicine(Medicine.builder()
                    .name("Paracetamol 500mg - dòng " + (i + 1)).unit("Viên").unitPrice(new BigDecimal("999999")).build())
                    .quantity(2).unitPriceAtTime(new BigDecimal("1500.00")).build());
        }
        appointment.setMedicalRecord(MedicalRecord.builder().prescription(prescription).build());
        BigDecimal medicineFee = new BigDecimal("3000.00").multiply(BigDecimal.valueOf(count));
        return Invoice.builder().id(4L).invoiceCode("INV-20261002-DEMO").appointment(appointment)
                .consultationFee(new BigDecimal("350000.00")).medicineFee(medicineFee)
                .totalAmount(new BigDecimal("350000.00").add(medicineFee)).paymentStatus(PaymentStatus.PAID)
                .createdAt(LocalDateTime.of(2026, 10, 2, 7, 41)).paidAt(LocalDateTime.of(2026, 10, 2, 7, 43)).build();
    }
}
