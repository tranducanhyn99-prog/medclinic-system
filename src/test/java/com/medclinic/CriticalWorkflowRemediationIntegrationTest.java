package com.medclinic;

import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.Appointment;
import com.medclinic.entity.BankTransferPayment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Invoice;
import com.medclinic.entity.PasswordResetToken;
import com.medclinic.entity.Specialty;
import com.medclinic.entity.TimeSlot;
import com.medclinic.entity.User;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.enums.AuthChallengeType;
import com.medclinic.enums.BankTransferStatus;
import com.medclinic.enums.Gender;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.enums.SlotStatus;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.AuthVerificationChallengeRepository;
import com.medclinic.repository.BankTransferPaymentRepository;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.repository.PasswordResetTokenRepository;
import com.medclinic.repository.SpecialtyRepository;
import com.medclinic.repository.TimeSlotRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.AuthVerificationService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.EmailService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.MedicalRecordService;
import com.medclinic.service.PasswordResetService;
import com.medclinic.service.ScheduleService;
import com.medclinic.service.UserService;
import com.medclinic.util.Sha256;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CriticalWorkflowRemediationIntegrationTest {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorService doctorService;
    @Autowired private ScheduleService scheduleService;
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private PasswordResetService passwordResetService;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private AuthVerificationService authVerificationService;
    @Autowired private AuthVerificationChallengeRepository challengeRepository;
    @Autowired private InvoiceService invoiceService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private BankTransferPaymentRepository bankTransferPaymentRepository;
    @Autowired private DoctorProfileRepository doctorProfileRepository;
    @Autowired private SpecialtyRepository specialtyRepository;
    @Autowired private TimeSlotRepository timeSlotRepository;
    @MockitoBean private EmailService emailService;

    @BeforeEach
    void resetMocks() {
        reset(emailService);
    }

    @AfterEach
    void restoreClock() {
        appointmentService.setBusinessClock(Clock.system(VN_ZONE));
    }

    @Test
    void unverifiedRegisteredPatientCanResumeVerificationViaLoginAfterSessionLoss() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "resume_" + suffix;
        String email = username + "@example.test";
        String phone = "091" + String.format("%07d", Math.floorMod(suffix.hashCode(), 10_000_000));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", username)
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("fullName", "Resume Patient")
                        .param("email", email)
                        .param("phone", phone)
                        .param("gender", "FEMALE")
                        .param("dateOfBirth", "1998-05-10"))
                .andExpect(status().is3xxRedirection());

        reset(emailService);

        MvcResult relogin = mockMvc.perform(post("/auth/login").with(csrf())
                        .param("username", username)
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirect = relogin.getResponse().getRedirectedUrl();
        assertNotNull(redirect);
        assertTrue(redirect.startsWith("/auth/otp?challenge="),
                "Expected redirect to /auth/otp?challenge=..., but got: " + redirect);
        long challengeId = Long.parseLong(redirect.substring(redirect.indexOf('=') + 1));

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendAuthenticationOtpEmail(eq(email), eq("Resume Patient"),
                codeCaptor.capture(), anyString(), eq(5L), eq("đăng ký tài khoản"));

        MockHttpSession freshSession = (MockHttpSession) relogin.getRequest().getSession(false);
        assertNotNull(freshSession);
        mockMvc.perform(post("/auth/otp").session(freshSession).with(csrf())
                        .param("challengeId", Long.toString(challengeId))
                        .param("code", codeCaptor.getValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"));

        User activated = userRepository.findByUsername(username).orElseThrow();
        assertTrue(activated.getIsActive());
        assertTrue(activated.getEmailVerified());
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    void walkInPatientCanClaimAccountByPhoneAndRetainAppointmentHistory() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String phone = "094" + String.format("%07d", Math.floorMod(suffix.hashCode(), 10_000_000));
        DoctorProfile doctor = doctorProfileRepository.findAll().getFirst();
        LocalDate today = LocalDate.now(VN_ZONE);
        appointmentService.setBusinessClock(Clock.fixed(today.atTime(7, 30).atZone(VN_ZONE).toInstant(), VN_ZONE));
        TimeSlot slot = timeSlotRepository.findAvailableSlotsByDoctorAndDate(
                doctor.getId(), today, SlotStatus.AVAILABLE).getFirst();

        Appointment walkInAppt = appointmentService.bookWalkInAppointment(
                "Bệnh nhân Vãng lai", phone, LocalDate.of(1995, 6, 15),
                Gender.MALE, "123 Lê Lợi", doctor.getId(), today, slot.getId(), "Khám tổng quát");
        Long originalPatientId = walkInAppt.getPatient().getId();

        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("walkin_claim_" + suffix);
        registerDto.setPassword("password123");
        registerDto.setConfirmPassword("password123");
        registerDto.setFullName("Bệnh nhân Chính Thức");
        registerDto.setEmail("walkin_claim_" + suffix + "@example.test");
        registerDto.setPhone(phone);
        registerDto.setGender(Gender.MALE);
        registerDto.setDateOfBirth(LocalDate.of(1995, 6, 15));
        registerDto.setAddress("123 Lê Lợi, Q1");

        User claimed = userService.registerPatient(registerDto);
        assertEquals(originalPatientId, claimed.getId(), "Must upgrade the walk-in User record in-place");
        assertEquals("walkin_claim_" + suffix, claimed.getUsername());
        assertEquals("walkin_claim_" + suffix + "@example.test", claimed.getEmail());
        assertFalse(claimed.getIsActive());
        assertFalse(claimed.getEmailVerified());

        List<Appointment> patientAppointments = appointmentService.findByPatient(claimed.getId());
        assertTrue(patientAppointments.stream().anyMatch(a -> a.getId().equals(walkInAppt.getId())));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void newlyCreatedDoctorAutomaticallyReceivesBookableFourteenDaySchedule() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Specialty specialty = specialtyRepository.findAll().getFirst();

        AdminDoctorCreateDto dto = new AdminDoctorCreateDto();
        dto.setUsername("dr_new_" + suffix);
        dto.setPassword("password123");
        dto.setFullName("BS. Nguyễn Mới " + suffix);
        dto.setEmail("dr_new_" + suffix + "@medclinic.com");
        dto.setPhone("097" + String.format("%07d", Math.floorMod(suffix.hashCode(), 10_000_000)));
        dto.setSpecialtyId(specialty.getId());
        dto.setLicenseNumber("CCHN-" + suffix);
        dto.setTitle("ThS.BS");
        dto.setExperienceYears(8);
        dto.setConsultationFee(new BigDecimal("250000"));
        dto.setBio("Chuyên gia nội tổng quát");

        DoctorProfile created = doctorService.registerDoctor(dto);
        LocalDate tomorrow = LocalDate.now(VN_ZONE).plusDays(1);
        List<TimeSlot> tomorrowSlots = scheduleService.getAvailableSlots(created.getId(), tomorrow);
        assertEquals(16, tomorrowSlots.size(), "New doctor must have 16 bookable slots (8 morning + 8 afternoon) for tomorrow");
    }

    @Test
    void passwordResetIncrementsSecurityVersionAndInvalidatesPendingChallenges() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("pwreset_" + suffix);
        registerDto.setPassword("password123");
        registerDto.setConfirmPassword("password123");
        registerDto.setFullName("Password Reset User");
        registerDto.setEmail("pwreset_" + suffix + "@example.test");
        registerDto.setPhone("098" + String.format("%07d", Math.floorMod(suffix.hashCode(), 10_000_000)));
        registerDto.setGender(Gender.MALE);
        registerDto.setDateOfBirth(LocalDate.of(1992, 1, 1));
        User user = userService.registerPatient(registerDto);
        user.setIsActive(true);
        user.setEmailVerified(true);
        user = userRepository.saveAndFlush(user);

        long versionBefore = user.getSecurityVersion();
        var challenge = authVerificationService.issueChallenge(user.getId(), AuthChallengeType.LOGIN);

        String rawToken = "reset-" + UUID.randomUUID();
        passwordResetTokenRepository.saveAndFlush(PasswordResetToken.builder()
                .user(user)
                .tokenHash(Sha256.hex(rawToken))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build());

        passwordResetService.resetPassword(rawToken, "newpassword123", "newpassword123");

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(versionBefore + 1, updated.getSecurityVersion(),
                "Password reset must increment securityVersion to revoke active sessions");
        assertNotNull(challengeRepository.findById(challenge.challengeId()).orElseThrow().getConsumedAt(),
                "Password reset must invalidate open OTP challenges");
    }

    @Test
    @WithUserDetails("patient")
    void patientCannotReadAnotherPatientsMedicalHistoryViaService() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertThrows(AccessDeniedException.class,
                () -> medicalRecordService.getPatientMedicalHistory(admin.getId()));
    }

    @Test
    void cashPaymentExpiresPendingBankTransferInstructions() {
        DoctorProfile doctor = doctorProfileRepository.findAll().getFirst();
        TimeSlot slot = timeSlotRepository.findAll().getFirst();
        User patient = userRepository.findByUsername("patient").orElseThrow();
        String token = UUID.randomUUID().toString().substring(0, 8);
        Appointment appointment = appointmentRepository.saveAndFlush(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .timeSlot(slot)
                .appointmentDate(LocalDate.now(VN_ZONE))
                .bookingCode("CASH-EXP-" + token)
                .status(AppointmentStatus.COMPLETED)
                .build());
        Invoice invoice = invoiceRepository.saveAndFlush(Invoice.builder()
                .appointment(appointment)
                .invoiceCode("INV-EXP-" + token)
                .consultationFee(new BigDecimal("150000"))
                .medicineFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("150000"))
                .paymentStatus(PaymentStatus.UNPAID)
                .build());
        BankTransferPayment pendingQr = bankTransferPaymentRepository.saveAndFlush(BankTransferPayment.builder()
                .invoice(invoice)
                .transferCode("MCEXP" + token.toUpperCase())
                .amount(new BigDecimal("150000"))
                .bankId("970436")
                .status(BankTransferStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());

        invoiceService.processPayment(invoice.getId(), PaymentMethod.CASH);

        BankTransferPayment afterCash = bankTransferPaymentRepository.findById(pendingQr.getId()).orElseThrow();
        assertEquals(BankTransferStatus.EXPIRED, afterCash.getStatus(),
                "Cash payment must expire any open PENDING bank transfer instruction for the invoice");
    }
}
