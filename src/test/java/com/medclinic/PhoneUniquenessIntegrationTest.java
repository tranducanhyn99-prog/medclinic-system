package com.medclinic;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:phone_review;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class PhoneUniquenessIntegrationTest {
    @Autowired UserService service;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired MockMvc mvc;

    @Test
    void directRegistrationRequestRejectsPipePhoneBeforeCreatingAnAccount() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/register")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .param("username", "pipe-phone").param("password", "password123")
                .param("confirmPassword", "password123").param("fullName", "Phone review")
                .param("email", "pipe-phone@example.test").param("phone", "0|12345678"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("auth/register"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeHasFieldErrors("registerDto", "phone"));
        assertFalse(users.existsByUsername("pipe-phone"));
    }

    @Test
    void concurrentRegistrationsAllowExactlyOneOwnerAndReportConflict() throws Exception {
        String phone = randomPhone();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = executor.submit(() -> registerAfterStart(phone, start));
            Future<Boolean> second = executor.submit(() -> registerAfterStart(phone, start));
            start.countDown();
            assertNotEquals(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
        }
        assertTrue(users.findByPhone(phone).isPresent());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "customUserDetailsService")
    void profileKeepsOwnPhoneRejectsOtherOwnerAndNormalizesOptionalBlank() {
        User first = service.registerPatient(dto(randomPhone()));
        User second = service.registerPatient(dto(randomPhone()));
        assertEquals(first.getPhone(), service.updateUserProfile(first.getId(), "Patient", first.getPhone(), null, null, null).getPhone());
        assertThrows(BusinessLogicException.class,
                () -> service.updateUserProfile(first.getId(), "Patient", second.getPhone(), null, null, null));
        assertNull(service.updateUserProfile(first.getId(), "Patient", "  ", null, null, null).getPhone());
        assertNull(service.updateUserProfile(second.getId(), "Patient", null, null, null, null).getPhone());
    }

    @Test
    void databaseRejectsBypassingUniquePhoneButAllowsMultipleAbsentPhones() {
        String phone = randomPhone();
        users.saveAndFlush(user(phone));
        assertThrows(DataIntegrityViolationException.class, () -> users.saveAndFlush(user(phone)));
        assertNotNull(users.saveAndFlush(user(null)).getId());
        assertNotNull(users.saveAndFlush(user(null)).getId());
    }

    private boolean registerAfterStart(String phone, CountDownLatch start) throws InterruptedException {
        assertTrue(start.await(10, TimeUnit.SECONDS));
        try { service.registerPatient(dto(phone)); return true; }
        catch (BusinessLogicException exception) {
            assertTrue(exception.getMessage().contains("đã được đăng ký"));
            return false;
        }
    }

    private UserRegisterDto dto(String phone) {
        var dto = new UserRegisterDto();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        dto.setUsername("phone-" + suffix); dto.setEmail(suffix + "@example.test");
        dto.setPassword("password123"); dto.setConfirmPassword("password123");
        dto.setFullName("Phone review"); dto.setPhone(phone);
        return dto;
    }

    private User user(String phone) {
        var dto = dto(phone);
        return User.builder().username(dto.getUsername()).email(dto.getEmail()).fullName("Patient")
                .password("encoded").phone(phone).role(roles.findByName(RoleType.ROLE_PATIENT).orElseThrow()).build();
    }

    private String randomPhone() { return "09" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000)); }
}
