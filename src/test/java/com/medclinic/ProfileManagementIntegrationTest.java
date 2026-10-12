package com.medclinic;

import com.medclinic.entity.User;
import com.medclinic.enums.Gender;
import com.medclinic.repository.UserRepository;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.AvatarStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.storage.avatar-dir=target/test-avatars")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ProfileManagementIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AvatarStorageService storage;

    @Test
    void userCanEditPersonalDetailsAndReplaceAvatar() throws Exception {
        User patient = userRepository.findByUsername("patient").orElseThrow();
        MockMultipartFile avatar = validPng();

        mockMvc.perform(multipart("/profile")
                        .file(avatar)
                        .param("fullName", "Hồ Sơ Đã Cập Nhật")
                        .param("phone", "0987654321")
                        .param("gender", "FEMALE")
                        .param("dateOfBirth", "2001-02-03")
                        .param("address", "Quận 5, TP.HCM")
                        .with(user(new CustomUserDetails(patient)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        User updated = userRepository.findById(patient.getId()).orElseThrow();
        assertEquals("Hồ Sơ Đã Cập Nhật", updated.getFullName());
        assertEquals("0987654321", updated.getPhone());
        assertEquals(Gender.FEMALE, updated.getGender());
        assertEquals(LocalDate.of(2001, 2, 3), updated.getDateOfBirth());
        assertEquals("Quận 5, TP.HCM", updated.getAddress());
        assertNotNull(updated.getAvatarUrl());
        assertTrue(updated.getAvatarUrl().startsWith("/user-content/avatars/"));

        String filename = updated.getAvatarUrl().substring("/user-content/avatars/".length());
        mockMvc.perform(get("/user-content/avatars/{filename}", filename)
                        .with(user(new CustomUserDetails(updated))))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        mockMvc.perform(get("/profile").with(user(new CustomUserDetails(updated))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hồ Sơ Đã Cập Nhật")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(updated.getAvatarUrl())));
    }

    @Test
    void rejectedUploadKeepsThePreviousAvatarUrlAndFile() throws Exception {
        User patient = userRepository.findByUsername("patient").orElseThrow();
        String oldUrl = storage.storeAvatar(validPng(), patient.getAvatarUrl());
        patient.setAvatarUrl(oldUrl);
        userRepository.saveAndFlush(patient);
        mockMvc.perform(multipart("/profile")
                        .file(new MockMultipartFile("avatar", "fake.png", "image/png", new byte[]{1, 2, 3}))
                        .param("fullName", patient.getFullName()).param("phone", patient.getPhone())
                        .with(user(new CustomUserDetails(patient))).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("errorMessage"));
        assertEquals(oldUrl, userRepository.findById(patient.getId()).orElseThrow().getAvatarUrl());
        assertTrue(Files.exists(Path.of("target/test-avatars", oldUrl.substring(oldUrl.lastIndexOf('/') + 1))));
    }

    private MockMultipartFile validPng() throws Exception {
        var bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes));
        return new MockMultipartFile("avatar", "avatar.png", "image/png", bytes.toByteArray());
    }
}
