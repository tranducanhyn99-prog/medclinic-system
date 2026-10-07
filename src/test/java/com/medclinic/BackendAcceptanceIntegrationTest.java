package com.medclinic;

import tools.jackson.databind.ObjectMapper;
import com.medclinic.dto.*;
import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.*;
import com.medclinic.security.HmacSignatureVerifier;
import com.medclinic.service.*;
import com.medclinic.service.BankTransferPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.payment.bank.enabled=true", "app.payment.bank.account-no=0123456789",
        "app.payment.bank.account-name=ACCEPTANCE TEST", "app.payment.bank.callback-secret=0123456789abcdef0123456789abcdef"})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class BackendAcceptanceIntegrationTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired DoctorProfileRepository doctors;
    @Autowired SpecialtyRepository specialties;
    @Autowired DoctorScheduleRepository schedules;
    @Autowired TimeSlotRepository slots;
    @Autowired AppointmentRepository appointments;
    @Autowired MedicalRecordRepository records;
    @Autowired MedicineRepository medicines;
    @Autowired InvoiceRepository invoices;
    @Autowired InventoryTransactionRepository inventory;
    @Autowired BankTransferPaymentRepository payments;
    @Autowired PasswordResetTokenRepository resetTokens;
    @Autowired AppointmentService booking;
    @Autowired ScheduleService scheduleService;
    @Autowired MedicalRecordService examinations;
    @Autowired BankTransferPaymentService bank;
    @Autowired AuthVerificationService auth;
    @Autowired PasswordResetService passwordReset;
    @Autowired PasswordEncoder encoder;
    @Autowired UserService userService;
    @MockitoBean EmailService email;

    @BeforeEach void resetMail() { reset(email); }

    @Test @WithUserDetails("staff")
    void generatedBankCodePassesHttpValidationHmacAmountAndRetry() throws Exception {
        Invoice invoice = invoice();
        var instruction = bank.createInstruction(invoice.getId());
        var callback = callback(instruction.transferCode(), instruction.amount());
        postCallback(callback, "00".repeat(32)).andExpect(status().isUnauthorized());
        var wrongAmount = callback(instruction.transferCode(), instruction.amount().subtract(BigDecimal.ONE));
        postCallback(wrongAmount, signature(wrongAmount)).andExpect(status().isUnprocessableEntity());
        assertEquals(PaymentStatus.UNPAID, invoices.findById(invoice.getId()).orElseThrow().getPaymentStatus());
        postCallback(callback, signature(callback)).andExpect(status().isOk()).andExpect(jsonPath("$.accepted").value(true));
        postCallback(callback, signature(callback)).andExpect(status().isOk());
        assertEquals(PaymentStatus.PAID, invoices.findById(invoice.getId()).orElseThrow().getPaymentStatus());
        assertEquals(1, payments.findAll().stream().filter(p -> callback.transactionId().equals(p.getBankTransactionId())).count());
    }

    @Test @WithUserDetails("staff")
    void legacyBankCodeStillConfirmsThroughHttp() throws Exception {
        Invoice invoice = invoice();
        var instruction = bank.createInstruction(invoice.getId());
        BankTransferPayment payment = payments.findById(instruction.paymentId()).orElseThrow();
        String legacy = "MC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        payment.setTransferCode(legacy); payments.save(payment);
        var callback = callback(legacy, instruction.amount());
        postCallback(callback, signature(callback)).andExpect(status().isOk());
    }

    @Test void lockedAccountCannotConsumeOtpOrMagicLinkOrCreateSession() throws Exception {
        for (boolean magic : new boolean[]{false, true}) {
            reset(email);
            User user = patient();
            var issued = auth.issueChallenge(user.getId(), AuthChallengeType.LOGIN);
            ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
            verify(email).sendAuthenticationOtpEmail(eq(user.getEmail()), anyString(), code.capture(), link.capture(), anyLong(), anyString());
            user.setIsActive(false); users.save(user);
            var result = magic ? mvc.perform(get("/auth/verify-email").param("token", link.getValue().split("token=")[1]))
                    : mvc.perform(post("/auth/otp").with(csrf()).param("challengeId", issued.challengeId().toString()).param("code", code.getValue()));
            var response = result.andExpect(status().is3xxRedirection()).andReturn();
            var session = response.getRequest().getSession(false);
            assertTrue(session == null || session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) == null);
            assertFalse(users.findById(user.getId()).orElseThrow().getIsActive());
        }
    }

    @Test @WithUserDetails("doctor")
    void blankClinicalInputDoesNotChangeAppointmentStockOrInvoice() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        mvc.perform(post("/doctor/examine/" + appointment.getId()).with(csrf())
                .param("symptoms", "  ").param("diagnosis", "").param("doctorNotes", "Giữ nội dung này"))
                .andExpect(status().isOk()).andExpect(view().name("doctor/examine"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-error-summary")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"#symptoms\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"#diagnosis\"")))
                .andExpect(result -> assertTrue(org.springframework.web.util.HtmlUtils.htmlUnescape(
                        result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("Giữ nội dung này")))
                .andDo(result -> UxReviewFixture.write("examination-errors", result.getResponse()));
        authenticate("doctor");
        assertThrows(BusinessLogicException.class, () -> examinations.saveMedicalRecordAndPrescription(appointment.getId(),
                appointment.getDoctor().getUser().getId(), "", "", null, null,
                List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1).build())));
        assertUnchanged(appointment, medicine.getId(), stock);
    }

    @Test @WithUserDetails("doctor")
    void missingOrShortMedicineListsReturnFormAndPreserveValues() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        for (boolean shortList : new boolean[]{false, true}) {
            var request = post("/doctor/examine/" + appointment.getId()).with(csrf())
                    .param("symptoms", "Triệu chứng giữ lại").param("diagnosis", "Chẩn đoán giữ lại")
                    .param("medicineIds", medicine.getId().toString()).param("dosages", "1 viên")
                    .param("frequencies", "2 lần/ngày").param("instructions", "");
            if (shortList) request.param("medicineIds", medicine.getId().toString(), medicine.getId().toString()).param("quantities", "1");
            mvc.perform(request).andExpect(status().isOk()).andExpect(view().name("doctor/examine"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Triệu chứng giữ lại")));
            assertUnchanged(appointment, medicine.getId(), stock);
        }
    }

    @Test @WithUserDetails("doctor")
    void oversizedPrescriptionFieldsReturnFormWithoutChangingClinicalOrFinancialData() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        for (String field : List.of("dosages", "frequencies", "instructions")) {
            var request = post("/doctor/examine/" + appointment.getId()).with(csrf())
                    .param("symptoms", "Triệu chứng giữ lại").param("diagnosis", "Chẩn đoán giữ lại")
                    .param("medicineIds", medicine.getId().toString()).param("quantities", "1")
                    .param("dosages", field.equals("dosages") ? "x".repeat(101) : "1 viên")
                    .param("frequencies", field.equals("frequencies") ? "x".repeat(101) : "2 lần/ngày")
                    .param("instructions", field.equals("instructions") ? "x".repeat(256) : "");
            mvc.perform(request).andExpect(status().isOk()).andExpect(view().name("doctor/examine"))
                    .andExpect(model().attributeHasFieldErrors("examinationForm", "prescriptionConsistent"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"prescriptionErrors\"")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"#prescriptionErrors\"")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Triệu chứng giữ lại")));
            assertUnchanged(appointment, medicine.getId(), stock);
        }
    }

    @Test @WithUserDetails("doctor")
    void singlePrescriptionRowPreservesCommasAndDatabaseLengthBoundaries() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        String dosage = "1 viên, ";
        dosage += "x".repeat(100 - dosage.length());
        String frequency = "Sáng, tối, ";
        frequency += "x".repeat(100 - frequency.length());
        String instructions = "Sau ăn, ";
        instructions += "x".repeat(255 - instructions.length());
        mvc.perform(post("/doctor/examine/" + appointment.getId()).with(csrf())
                .param("symptoms", "DEMO triệu chứng").param("diagnosis", "DEMO chẩn đoán")
                .param("medicineIds", medicine.getId().toString()).param("quantities", "1")
                .param("dosages", dosage).param("frequencies", frequency).param("instructions", instructions))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"));
        PrescriptionItem item = records.findByAppointmentId(appointment.getId()).orElseThrow().getPrescription().getItems().getFirst();
        assertEquals(dosage, item.getDosage());
        assertEquals(frequency, item.getFrequency());
        assertEquals(instructions, item.getInstructions());
        assertEquals(stock - 1, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertEquals(AppointmentStatus.COMPLETED, appointments.findById(appointment.getId()).orElseThrow().getStatus());
        assertEquals(PaymentStatus.UNPAID, invoices.findByAppointmentId(appointment.getId()).orElseThrow().getPaymentStatus());
    }

    @Test @WithUserDetails("doctor")
    void invalidServicePrescriptionRowsAndOverflowFailBeforeStockOrInvoiceWrites() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        PrescriptionItemDto valid = PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1)
                .dosage("1 viên").frequency("2 lần/ngày").instructions("").build();
        List<List<PrescriptionItemDto>> cases = new ArrayList<>();
        cases.add(Collections.singletonList(null));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(-1L).quantity(1).dosage("1 viên").frequency("2 lần/ngày").build()));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(0).dosage("1 viên").frequency("2 lần/ngày").build()));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1).frequency("2 lần/ngày").build()));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1).dosage("1 viên").frequency(" ").build()));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1).dosage("x".repeat(101)).frequency("2 lần/ngày").build()));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1).dosage("1 viên").frequency("x".repeat(101)).build()));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(1).dosage("1 viên").frequency("2 lần/ngày").instructions("x".repeat(256)).build()));
        cases.add(Collections.nCopies(101, valid));
        cases.add(List.of(PrescriptionItemDto.builder().medicineId(medicine.getId()).quantity(Integer.MAX_VALUE)
                .dosage("1 viên").frequency("2 lần/ngày").build(), valid));
        for (List<PrescriptionItemDto> rows : cases) {
            assertThrows(BusinessLogicException.class, () -> examinations.saveMedicalRecordAndPrescription(
                    appointment.getId(), appointment.getDoctor().getUser().getId(), "DEMO triệu chứng", "DEMO chẩn đoán", null, null, rows));
            assertUnchanged(appointment, medicine.getId(), stock);
        }
        mvc.perform(post("/doctor/examine/" + appointment.getId()).with(csrf())
                .param("symptoms", "Triệu chứng giữ lại").param("diagnosis", "Chẩn đoán giữ lại")
                .param("medicineIds", medicine.getId().toString(), medicine.getId().toString())
                .param("quantities", Integer.toString(Integer.MAX_VALUE), "1")
                .param("dosages", "1 viên", "1 viên").param("frequencies", "2 lần/ngày", "2 lần/ngày"))
                .andExpect(status().isOk()).andExpect(view().name("doctor/examine"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tổng số lượng của một thuốc vượt giới hạn")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Triệu chứng giữ lại")));
        assertUnchanged(appointment, medicine.getId(), stock);
    }

    @Test @WithUserDetails("doctor")
    void repeatedTextParametersPreserveRowBoundariesAndOptionalBlankInstructions() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        mvc.perform(post("/doctor/examine/" + appointment.getId()).with(csrf())
                .param("symptoms", "DEMO triệu chứng").param("diagnosis", "DEMO chẩn đoán")
                .param("medicineIds", medicine.getId().toString(), medicine.getId().toString())
                .param("quantities", "1", "2").param("dosages", "1 viên, sau ăn", "2 viên, sau ăn")
                .param("frequencies", "Sáng, tối", "Sáng, trưa").param("instructions", "", "Uống sau ăn, với nước"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"));
        var items = records.findByAppointmentId(appointment.getId()).orElseThrow().getPrescription().getItems();
        assertEquals(2, items.size());
        var first = items.stream().filter(i -> i.getQuantity() == 1).findFirst().orElseThrow();
        var second = items.stream().filter(i -> i.getQuantity() == 2).findFirst().orElseThrow();
        assertEquals("1 viên, sau ăn", first.getDosage());
        assertEquals("Sáng, tối", first.getFrequency());
        assertEquals("", first.getInstructions());
        assertEquals("2 viên, sau ăn", second.getDosage());
        assertEquals("Sáng, trưa", second.getFrequency());
        assertEquals("Uống sau ăn, với nước", second.getInstructions());
        assertEquals(stock - 3, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
    }

    @Test @WithUserDetails("patient")
    void inactiveDoctorAndSpecialtyAreHiddenAndRejectDirectBooking() throws Exception {
        DoctorProfile doctor = doctors.findAll().getFirst();
        User doctorUser = doctor.getUser(); Specialty specialty = doctor.getSpecialty();
        var date = LocalDate.now().plusDays(1);
        var slot = slots.findAvailableSlotsByDoctorAndDate(doctor.getId(), date, SlotStatus.AVAILABLE).getFirst();
        Long patientId = users.findByUsername("patient").orElseThrow().getId();
        for (boolean disableDoctor : new boolean[]{true, false}) {
            try {
                if (disableDoctor) { doctorUser.setIsActive(false); users.save(doctorUser); }
                else { specialty.setIsActive(false); specialties.save(specialty); }
                mvc.perform(get("/doctors/" + doctor.getId())).andExpect(status().isNotFound());
                mvc.perform(get("/api/public/doctors/" + doctor.getId() + "/slots").param("date", date.toString()))
                        .andExpect(status().isOk()).andExpect(content().json("[]"));
                authenticate("patient");
                assertThrows(BusinessLogicException.class, () -> booking.bookAppointment(patientId, doctor.getId(), date, slot.getId(), "Thử nghiệm"));
                assertEquals(SlotStatus.AVAILABLE, slots.findById(slot.getId()).orElseThrow().getStatus());
            } finally {
                doctorUser.setIsActive(true); users.save(doctorUser); specialty.setIsActive(true); specialties.save(specialty);
            }
        }
    }

    @Test @WithUserDetails("doctor")
    void oneMedicineWithOptionalEmptyInstructionsSavesThroughHttp() throws Exception {
        Appointment appointment = appointment(AppointmentStatus.IN_PROGRESS);
        Medicine medicine = medicines.findAll().getFirst();
        int stock = medicine.getStockQuantity();
        mvc.perform(post("/doctor/examine/" + appointment.getId()).with(csrf())
                .param("symptoms", "DEMO triệu chứng").param("diagnosis", "DEMO chẩn đoán")
                .param("medicineIds", medicine.getId().toString()).param("quantities", "2")
                .param("dosages", "DEMO liều thử").param("frequencies", "DEMO tần suất thử")
                .param("instructions", ""))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/desk"));
        assertEquals(AppointmentStatus.COMPLETED, appointments.findById(appointment.getId()).orElseThrow().getStatus());
        assertTrue(records.findByAppointmentId(appointment.getId()).isPresent());
        assertEquals(stock - 2, medicines.findById(medicine.getId()).orElseThrow().getStockQuantity());
        assertEquals(PaymentStatus.UNPAID, invoices.findByAppointmentId(appointment.getId()).orElseThrow().getPaymentStatus());
    }

    @Test @WithUserDetails("patient")
    void serverRejectsBookingBeyondFourteenDaysAndApiReturnsNoSlots() {
        var doctor = doctors.findAll().getFirst();
        var patient = users.findByUsername("patient").orElseThrow();
        assertThrows(BusinessLogicException.class, () -> booking.bookAppointment(patient.getId(), doctor.getId(), LocalDate.now().plusDays(15), -1L, "Test"));
        assertTrue(scheduleService.getAvailableSlots(doctor.getId(), LocalDate.now().plusDays(15)).isEmpty());
    }

    @Test void apiErrorsHaveCorrectStatusJsonAndNoInternalDetails() throws Exception {
        mvc.perform(get("/api/public/doctors/1/slots").param("date", "not-a-date"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get("/api/public/doctors/999999/slots").param("date", LocalDate.now().toString()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(post("/api/payments/bank/callback").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get("/doctors/999999")).andExpect(status().isNotFound());
    }

    @Test void concurrentPasswordResetConsumesTokenExactlyOnce() throws Exception {
        User user = patient();
        passwordReset.requestPasswordReset(user.getEmail());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(email).sendPasswordResetEmail(eq(user.getEmail()), anyString(), link.capture());
        String token = link.getValue().split("token=")[1];
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> attempts = new ArrayList<>();
            for (String password : List.of("acceptance-one", "acceptance-two")) attempts.add(pool.submit(() -> {
                ready.countDown(); assertTrue(start.await(10, TimeUnit.SECONDS));
                try { passwordReset.resetPassword(token, password, password); return true; }
                catch (BusinessLogicException ex) { return false; }
            }));
            assertTrue(ready.await(10, TimeUnit.SECONDS)); start.countDown();
            assertEquals(1, (attempts.get(0).get(20, TimeUnit.SECONDS) ? 1 : 0) + (attempts.get(1).get(20, TimeUnit.SECONDS) ? 1 : 0));
            assertNotNull(resetTokens.findAll().stream().filter(t -> t.getUser().getId().equals(user.getId())).findFirst().orElseThrow().getUsedAt());
        }
    }

    @Test void shortPasswordDoesNotConsumeResetToken() {
        assertThrows(BusinessLogicException.class, () -> passwordReset.resetPassword("unused", "1234567", "1234567"));
    }

    @Test @WithUserDetails("admin")
    void adminLockInvalidatesAnOutstandingChallengeEvenAfterUnlock() {
        User user = patient();
        var challenge = auth.issueChallenge(user.getId(), AuthChallengeType.LOGIN);
        userService.toggleUserStatus(user.getId());
        userService.toggleUserStatus(user.getId());
        assertThrows(BusinessLogicException.class, () -> auth.getChallengeInfo(challenge.challengeId()));
    }

    private void assertUnchanged(Appointment appointment, Long medicineId, int stock) {
        assertEquals(AppointmentStatus.IN_PROGRESS, appointments.findById(appointment.getId()).orElseThrow().getStatus());
        assertTrue(records.findByAppointmentId(appointment.getId()).isEmpty());
        assertTrue(invoices.findByAppointmentId(appointment.getId()).isEmpty());
        assertEquals(stock, medicines.findById(medicineId).orElseThrow().getStockQuantity());
        assertTrue(inventory.findAll().stream().noneMatch(t -> "APPOINTMENT".equals(t.getReferenceType())
                && appointment.getId().equals(t.getReferenceId())));
    }

    private void authenticate(String username) {
        var details = new com.medclinic.security.CustomUserDetails(users.findByUsername(username).orElseThrow());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities()));
    }

    private User patient() {
        String name = "accept_" + UUID.randomUUID().toString().substring(0, 8);
        return users.save(User.builder().username(name).email(name + "@example.test").fullName("Acceptance Demo")
                .password(encoder.encode("acceptance123")).role(roles.findByName(RoleType.ROLE_PATIENT).orElseThrow()).isActive(true).build());
    }

    private Appointment appointment(AppointmentStatus status) {
        User doctorUser = users.findByUsername("doctor").orElseThrow();
        DoctorProfile doctor = doctors.findByUserId(doctorUser.getId()).orElseThrow();
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var schedule = schedules.findByDoctorIdAndWorkDateAndShift(doctor.getId(), date, ShiftType.MORNING)
                .orElseGet(() -> schedules.save(DoctorSchedule.builder().doctor(doctor).workDate(date).shift(ShiftType.MORNING).maxPatients(8).build()));
        var slot = slots.save(TimeSlot.builder().schedule(schedule).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(8, 30)).status(SlotStatus.BOOKED).build());
        return appointments.save(Appointment.builder().patient(patient()).doctor(doctor).appointmentDate(date).timeSlot(slot)
                .bookingCode("AC-" + UUID.randomUUID().toString().substring(0, 8)).reason("Acceptance demo").status(status).build());
    }

    private Invoice invoice() {
        return invoices.save(Invoice.builder().appointment(appointment(AppointmentStatus.COMPLETED))
                .invoiceCode("AC-" + UUID.randomUUID().toString().substring(0, 8)).consultationFee(new BigDecimal("350000"))
                .medicineFee(BigDecimal.ZERO).totalAmount(new BigDecimal("350000")).paymentStatus(PaymentStatus.UNPAID).build());
    }

    private BankTransferCallbackDto callback(String code, BigDecimal amount) {
        return new BankTransferCallbackDto("event-" + UUID.randomUUID(), "txn-" + UUID.randomUUID(), code, amount, null);
    }

    private String signature(BankTransferCallbackDto callback) { return HmacSignatureVerifier.signSha256(SECRET, BankTransferPaymentService.canonical(callback)); }
    private org.springframework.test.web.servlet.ResultActions postCallback(BankTransferCallbackDto callback, String signature) throws Exception {
        return mvc.perform(post("/api/payments/bank/callback").header("X-MedClinic-Signature", signature)
                .contentType("application/json").content(json.writeValueAsString(callback)));
    }
}
