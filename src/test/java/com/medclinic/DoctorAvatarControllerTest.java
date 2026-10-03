package com.medclinic;

import com.medclinic.controller.DoctorAvatarController;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.User;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.service.AvatarStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorAvatarControllerTest {
    private DoctorProfileRepository doctors;
    private AvatarStorageService storage;
    private ResourceLoader resources;
    private DoctorAvatarController controller;
    private User user;

    @BeforeEach void setUp() {
        doctors = mock(DoctorProfileRepository.class);
        storage = mock(AvatarStorageService.class);
        resources = mock(ResourceLoader.class);
        controller = new DoctorAvatarController(doctors, storage, resources);
        user = User.builder().isActive(true).avatarUrl("/images/doctors/portrait.jpg").build();
        when(doctors.findById(1L)).thenReturn(Optional.of(DoctorProfile.builder().user(user).build()));
    }

    @Test void seedPortraitIsResizedAndCachedButInactiveDoctorIsNeverServed() throws Exception {
        when(resources.getResource("classpath:static/images/doctors/portrait.jpg"))
                .thenReturn(image(1024, 512));
        var response = controller.avatar(1L, 128);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getCacheControl().contains("max-age=300"));
        var thumbnail = ImageIO.read(new ByteArrayInputStream((byte[]) response.getBody()));
        assertEquals(128, thumbnail.getWidth());
        assertEquals(64, thumbnail.getHeight());
        controller.avatar(1L, 128);
        verify(resources, times(1)).getResource(anyString());
        user.setIsActive(false);
        assertEquals(404, controller.avatar(1L, 128).getStatusCode().value());
    }

    @Test void uploadedPortraitIsResolvedThroughDoctorAndTransparentPngIsConverted() throws Exception {
        user.setAvatarUrl("/user-content/avatars/doctor.png");
        when(storage.loadAvatar("doctor.png")).thenReturn(new AvatarStorageService.AvatarResource(image(400, 800), MediaType.IMAGE_PNG));
        var response = controller.avatar(1L, 256);
        var thumbnail = ImageIO.read(new ByteArrayInputStream((byte[]) response.getBody()));
        assertEquals(128, thumbnail.getWidth());
        assertEquals(256, thumbnail.getHeight());
        verifyNoInteractions(resources);
    }

    @Test void webpRetainsOriginalMediaTypeWithoutUnsupportedDecoder() {
        user.setAvatarUrl("/user-content/avatars/doctor.webp");
        var original = new ByteArrayResource(new byte[]{1, 2});
        when(storage.loadAvatar("doctor.webp")).thenReturn(new AvatarStorageService.AvatarResource(original, MediaType.parseMediaType("image/webp")));
        var response = controller.avatar(1L, 128);
        assertEquals(MediaType.parseMediaType("image/webp"), response.getHeaders().getContentType());
        assertSame(original, response.getBody());
    }

    @Test void missingDoctorUnsupportedSizeAndUntrustedSourceCannotExposeFiles() {
        assertEquals(404, controller.avatar(99L, 128).getStatusCode().value());
        assertEquals(400, controller.avatar(1L, 4096).getStatusCode().value());
        for (String path : new String[]{"https://example.com/patient.jpg", "/images/doctors/../../secret.png", "/private/patient.png"}) {
            user.setAvatarUrl(path);
            assertEquals(404, controller.avatar(1L, 128).getStatusCode().value());
        }
        verifyNoInteractions(storage, resources);
    }

    @Test void missingOrUndecodablePortraitReturnsNotFound() {
        user.setAvatarUrl(null);
        assertEquals(404, controller.avatar(1L, 128).getStatusCode().value());
        user.setAvatarUrl("/images/doctors/portrait.jpg");
        when(resources.getResource(anyString())).thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));
        assertEquals(404, controller.avatar(1L, 128).getStatusCode().value());
    }

    private ByteArrayResource image(int width, int height) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", output);
        return new ByteArrayResource(output.toByteArray());
    }
}
