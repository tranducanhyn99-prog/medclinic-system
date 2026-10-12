package com.medclinic;

import com.medclinic.dto.BankTransferInstruction;
import com.medclinic.enums.BankTransferStatus;
import com.medclinic.service.BankTransferPaymentService;
import com.medclinic.repository.UserRepository;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Renders server-side UI states with a mocked bank; never sends or confirms a transfer. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DeepUiStateIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @MockitoBean private BankTransferPaymentService bank;

    @ParameterizedTest
    @EnumSource(BankTransferStatus.class)
    void transferStatesRenderWithLatePaymentReconciliationOnlyWhenAllowed(BankTransferStatus state) throws Exception {
        var instruction = new BankTransferInstruction(99L, 88L, "INV-DEMO", "TEST", "Ngân hàng DEMO",
                "000000", "DEMO", "MCDEMO", new BigDecimal("353000.00"), "/images/logo.svg",
                state, LocalDateTime.of(2026, 10, 2, 12, 0), state == BankTransferStatus.CONFIRMED ? LocalDateTime.now() : null);
        when(bank.findInstruction(99L)).thenReturn(instruction);
        var staff = new CustomUserDetails(users.findByUsername("staff").orElseThrow());
        String html = mvc.perform(get("/staff/billing/bank/payment/99").with(user(staff)))
                .andExpect(status().isOk())
                .andDo(result -> UxReviewFixture.write("bank-" + state.name().toLowerCase(java.util.Locale.ROOT), result.getResponse()))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(html.contains("353 000 đ"));
        assertTrue(html.contains(staff.getFullName()));
        assertTrue(html.contains("data-initial-status=\"" + state.name() + "\""));
        assertEquals(state == BankTransferStatus.PENDING || state == BankTransferStatus.EXPIRED, html.contains("id=\"paymentManual\""));
        if (state == BankTransferStatus.EXPIRED) assertTrue(html.contains("Mã chuyển khoản đã hết hạn"));
        if (state == BankTransferStatus.REJECTED) assertTrue(html.contains("Giao dịch chưa được chấp nhận"));
        verify(bank, never()).confirmManual(any(), any(), any());
        verify(bank, never()).createInstruction(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "42"})
    void invalidMedicineHasLinkedLabelsAndRetainsEditIdentityWithoutPersisting(String id) throws Exception {
        var admin = new CustomUserDetails(users.findByUsername("admin").orElseThrow());
        String html = mvc.perform(post("/admin/medicines").with(user(admin)).with(csrf())
                        .param("id", id).param("name", "").param("unit", "viên").param("stockQuantity", "-1"))
                .andExpect(status().isOk()).andExpect(view().name("admin/medicines"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(html.contains("for=\"name\""));
        assertTrue(html.contains("id=\"name\""));
        assertTrue(html.contains("is-invalid"));
        assertTrue(html.contains("data-field-error"));
        assertFalse(html.contains("for=\"medName\""));
        assertTrue(html.contains("name=\"id\" value=\"" + id + "\""));
        if (!id.isEmpty()) assertTrue(html.contains("Sửa thông tin thuốc"));
    }

    @Test
    void scheduleUsesVietnameseDaysAndTextualSlotLegend() throws Exception {
        var doctor = new CustomUserDetails(users.findByUsername("doctor").orElseThrow());
        String html = mvc.perform(get("/doctor/schedule").with(user(doctor)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(html.contains("Khung giờ: dấu tích là còn trống, biểu tượng người là đã đặt."));
        assertTrue(html.contains("— Còn trống"));
        assertTrue(html.contains("Thứ ") || html.contains("Chủ nhật"));
        assertFalse(html.contains("Monday,") || html.contains("Friday,"));
    }
}
