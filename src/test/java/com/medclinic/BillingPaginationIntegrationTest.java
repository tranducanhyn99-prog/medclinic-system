package com.medclinic;

import com.medclinic.entity.Invoice;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.repository.BankTransferPaymentRepository;
import com.medclinic.service.InvoiceService;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.payment.bank.enabled=true", "app.payment.bank.account-no=0123456789",
        "app.payment.bank.account-name=PAGINATION TEST", "app.payment.bank.callback-secret=0123456789abcdef0123456789abcdef"})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class BillingPaginationIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired InvoiceService service;
    @Autowired UserRepository users;
    @Autowired DoctorProfileRepository doctors;
    @Autowired TimeSlotRepository slots;
    @Autowired MockMvc mvc;
    @Autowired BankTransferPaymentRepository bankPayments;

    @Test
    void tenThousandInvoicesStayBoundedAndFiltersSurviveCashCollection() throws Exception {
        String prefix = "BP-" + UUID.randomUUID().toString().substring(0, 8) + "-";
        LocalDate day = LocalDate.of(2040, 1, 1);
        Long patientId = users.findByUsername("patient").orElseThrow().getId();
        var staff = new CustomUserDetails(users.findByUsername("staff").orElseThrow());
        Long doctorId = doctors.findAll().getFirst().getId();
        Long slotId = slots.findAll().getFirst().getId();
        var appointmentRows = new ArrayList<Object[]>();
        var invoiceRows = new ArrayList<Object[]>();
        for (int i = 0; i < 10_000; i++) {
            appointmentRows.add(new Object[]{prefix + i, patientId, doctorId, slotId, day,
                    "COMPLETED", Timestamp.valueOf(day.atStartOfDay()), Timestamp.valueOf(day.atStartOfDay()), 0L});
            invoiceRows.add(new Object[]{prefix + i, prefix + i, i % 4 == 0 ? "PAID" : "UNPAID",
                    Timestamp.valueOf(day.atStartOfDay().plusSeconds(i))});
        }
        try {
            jdbc.batchUpdate("INSERT INTO appointments (booking_code,patient_id,doctor_id,time_slot_id,appointment_date,status,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,?,?)", appointmentRows);
            jdbc.batchUpdate("INSERT INTO invoices (appointment_id,invoice_code,consultation_fee,medicine_fee,total_amount,payment_method,payment_status,created_at) VALUES ((SELECT id FROM appointments WHERE booking_code=?),?,1000,0,1000,'CASH',?,?)", invoiceRows);
            long start = System.nanoTime();
            var first = service.findForBilling(PaymentStatus.UNPAID, day, day, 0, 25);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            assertEquals(7_500, first.getTotalElements());
            assertEquals(25, first.getContent().size());
            // To-one graph must remain usable after the repository transaction closes.
            assertNotNull(first.getContent().getFirst().getAppointment().getDoctor().getSpecialty().getName());
            var second = service.findForBilling(PaymentStatus.UNPAID, day, day, 1, 25);
            List<Long> firstIds = first.map(Invoice::getId).getContent();
            assertTrue(second.stream().noneMatch(invoice -> firstIds.contains(invoice.getId())));
            assertEquals(2_500, service.findForBilling(PaymentStatus.PAID, day, day, 0, 25).getTotalElements());
            assertEquals(10_000, service.findForBilling(null, day, day, 0, 25).getTotalElements());
            assertTrue(service.findForBilling(null, day.plusDays(1), day.plusDays(1), 0, 25).isEmpty());
            assertEquals(0, service.findForBilling(null, day.plusDays(1), day.plusDays(1), 100, 25).getNumber());
            assertEquals(0, service.findForBilling(PaymentStatus.UNPAID, day, day, -10, 1000).getNumber());
            assertEquals(100, service.findForBilling(PaymentStatus.UNPAID, day, day, -10, 1000).getSize());
            var last = service.findForBilling(PaymentStatus.UNPAID, day, day, Integer.MAX_VALUE, 25);
            assertEquals(last.getTotalPages() - 1, last.getNumber());
            assertEquals(1, service.findForBilling(PaymentStatus.UNPAID, day, day, 0, 0).getSize());

            var page = mvc.perform(get("/staff/billing").with(user(staff))
                    .param("from", day.toString()).param("to", day.toString()).param("page", "1"))
                    .andExpect(status().isOk()).andExpect(model().attribute("currentStatus", "UNPAID"))
                    .andReturn();
            String html = page.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(html.contains("Trang sau"));
            assertTrue(html.contains("from=2040-01-01"));
            assertTrue(html.contains("name=\"page\" value=\"1\""));
            Long paidId = second.getContent().getFirst().getId();
            mvc.perform(post("/staff/billing/pay/" + paidId).with(user(staff)).with(csrf())
                    .param("method", "CASH").param("from", day.toString()).param("to", day.toString()).param("page", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/staff/billing?status=UNPAID&from=2040-01-01&to=2040-01-01&page=1&size=25"));
            assertEquals(7_499, service.findForBilling(PaymentStatus.UNPAID, day, day, 0, 25).getTotalElements());
            assertEquals(2_501, service.findForBilling(PaymentStatus.PAID, day, day, 0, 25).getTotalElements());
            Long bankInvoiceId = first.getContent().getFirst().getId();
            String bankLocation = mvc.perform(post("/staff/billing/bank/" + bankInvoiceId)
                    .with(user(staff)).with(csrf()).param("status", "ALL")
                    .param("from", day.toString()).param("to", day.toString()).param("page", "2").param("size", "25"))
                    .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
            assertNotNull(bankLocation);
            assertTrue(bankLocation.contains("status=ALL&from=2040-01-01&to=2040-01-01&page=2&size=25"));
            String bankHtml = mvc.perform(get(bankLocation).with(user(staff)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(bankHtml.contains("name=\"status\" value=\"ALL\""));
            assertTrue(bankHtml.contains("href=\"/staff/billing?status=ALL"));
            Long paymentId = bankPayments.findFirstByInvoiceIdAndStatusOrderByCreatedAtDesc(bankInvoiceId,
                    com.medclinic.enums.BankTransferStatus.PENDING).orElseThrow().getId();
            mvc.perform(post("/staff/billing/bank/payment/" + paymentId + "/confirm")
                    .with(user(staff)).with(csrf()).param("bankTransactionId", "test-" + UUID.randomUUID())
                    .param("status", "ALL").param("from", day.toString()).param("to", day.toString()).param("page", "2").param("size", "25"))
                    .andExpect(redirectedUrl("/staff/billing?status=ALL&from=2040-01-01&to=2040-01-01&page=2&size=25"));
            assertEquals(7_498, service.findForBilling(PaymentStatus.UNPAID, day, day, 0, 25).getTotalElements());
            assertEquals(2_502, service.findForBilling(PaymentStatus.PAID, day, day, 0, 25).getTotalElements());
            mvc.perform(get("/staff/billing").with(user(staff)).param("status", "BAD"))
                    .andExpect(status().isOk()).andExpect(model().attribute("currentStatus", "UNPAID"));

            Path evidence = Path.of("target", "billing-pagination-measurement.json");
            Files.writeString(evidence, "{\"database\":\"H2\",\"fixtureInvoices\":10000,\"matchingUnpaid\":7500,\"returnedRows\":25,\"queryElapsedMs\":" + elapsedMs + "}");
        } finally {
            jdbc.update("DELETE FROM business_audit_events WHERE entity_type='INVOICE' AND entity_id IN (SELECT CAST(id AS varchar) FROM invoices WHERE invoice_code LIKE ?)", prefix + "%");
            jdbc.update("DELETE FROM bank_transfer_payments WHERE invoice_id IN (SELECT id FROM invoices WHERE invoice_code LIKE ?)", prefix + "%");
            jdbc.update("DELETE FROM invoices WHERE invoice_code LIKE ?", prefix + "%");
            jdbc.update("DELETE FROM appointments WHERE booking_code LIKE ?", prefix + "%");
        }
    }
}
