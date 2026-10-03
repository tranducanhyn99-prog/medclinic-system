package com.medclinic.controller;

import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.service.AvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Public portraits are resolved through an active doctor, never through a patient file name. */
@RestController
@RequiredArgsConstructor
public class DoctorAvatarController {
    private final DoctorProfileRepository doctorRepository;
    private final AvatarStorageService avatarStorageService;
    private final ResourceLoader resourceLoader;
    private final Map<String, byte[]> thumbnails = Collections.synchronizedMap(new LinkedHashMap<>(128, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) { return size() > 128; }
    });

    @GetMapping("/api/public/doctors/{id}/avatar")
    public ResponseEntity<?> avatar(@PathVariable Long id, @RequestParam(defaultValue = "128") int size) {
        if (!Set.of(128, 256, 512).contains(size)) return ResponseEntity.badRequest().build();
        var doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null || !Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            return ResponseEntity.notFound().build();
        }
        String url = doctor.getUser().getAvatarUrl();
        if (url == null || url.isBlank()) return ResponseEntity.notFound().build();
        try {
            String key = url + ":" + size;
            byte[] encoded = thumbnails.get(key);
            if (encoded == null) {
                org.springframework.core.io.Resource resource;
                if (url.startsWith("/user-content/avatars/")) {
                    var avatar = avatarStorageService.loadAvatar(url.substring("/user-content/avatars/".length()));
                    if (MediaType.parseMediaType("image/webp").equals(avatar.mediaType())) {
                        return ResponseEntity.ok().contentType(avatar.mediaType())
                                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                                .body(avatar.resource());
                    }
                    resource = avatar.resource();
                } else if (url.matches("/images/doctors/[A-Za-z0-9_-]+\\.(jpg|png)")) {
                    resource = resourceLoader.getResource("classpath:static" + url);
                } else {
                    return ResponseEntity.notFound().build();
                }
                try (var input = resource.getInputStream(); var stream = ImageIO.createImageInputStream(input)) {
                    var readers = ImageIO.getImageReaders(stream);
                    if (!readers.hasNext()) return ResponseEntity.notFound().build();
                    var reader = readers.next();
                    try {
                        reader.setInput(stream);
                        int width = reader.getWidth(0), height = reader.getHeight(0);
                        if ((long) width * height > 40_000_000L) return ResponseEntity.notFound().build();
                        var params = reader.getDefaultReadParam();
                        int sample = Math.max(1, Math.min(width, height) / size);
                        params.setSourceSubsampling(sample, sample, 0, 0);
                        BufferedImage original = reader.read(0, params);
                        double ratio = Math.min(1.0, (double) size / Math.max(original.getWidth(), original.getHeight()));
                        var resized = new BufferedImage(Math.max(1, (int) (original.getWidth() * ratio)),
                                Math.max(1, (int) (original.getHeight() * ratio)), BufferedImage.TYPE_INT_RGB);
                        var graphics = resized.createGraphics();
                        try {
                            graphics.setColor(java.awt.Color.WHITE);
                            graphics.fillRect(0, 0, resized.getWidth(), resized.getHeight());
                            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            graphics.drawImage(original, 0, 0, resized.getWidth(), resized.getHeight(), null);
                        } finally { graphics.dispose(); }
                        try (var output = new ByteArrayOutputStream()) {
                            ImageIO.write(resized, "jpg", output);
                            encoded = output.toByteArray();
                        }
                    } finally { reader.dispose(); }
                }
                thumbnails.put(key, encoded);
            }
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()).body(encoded);
        } catch (java.io.IOException | com.medclinic.exception.BusinessLogicException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
