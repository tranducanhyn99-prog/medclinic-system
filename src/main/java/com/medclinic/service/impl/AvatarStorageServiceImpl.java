package com.medclinic.service.impl;

import com.medclinic.exception.BusinessLogicException;
import com.medclinic.service.AvatarStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AvatarStorageServiceImpl implements AvatarStorageService {
    private static final long MAX_SIZE = 5L * 1024L * 1024L;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path storageDir;

    public AvatarStorageServiceImpl(@Value("${app.storage.avatar-dir:uploads/avatars}") String storageDir) {
        this.storageDir = Path.of(storageDir).toAbsolutePath().normalize();
    }

    @Override
    public String storeAvatar(MultipartFile file, String currentAvatarUrl) {
        if (file == null || file.isEmpty()) {
            return currentAvatarUrl;
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessLogicException("Ảnh đại diện tối đa 5 MB.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new BusinessLogicException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WebP.");
        }
        try {
            Files.createDirectories(storageDir);
            String filename = UUID.randomUUID() + extension;
            Path target = safeResolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/user-content/avatars/" + filename;
        } catch (IOException exception) {
            throw new BusinessLogicException("Không thể lưu ảnh đại diện. Vui lòng thử lại.");
        }
    }

    @Override
    public AvatarResource loadAvatar(String filename) {
        try {
            Path file = safeResolve(filename);
            if (!Files.isRegularFile(file)) {
                throw new BusinessLogicException("Không tìm thấy ảnh đại diện.");
            }
            Resource resource = new UrlResource(file.toUri());
            String detected = Files.probeContentType(file);
            MediaType mediaType = detected == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(detected);
            return new AvatarResource(resource, mediaType);
        } catch (IOException exception) {
            throw new BusinessLogicException("Không thể đọc ảnh đại diện.");
        }
    }

    private Path safeResolve(String filename) {
        if (filename == null || !filename.matches("[0-9a-fA-F-]+\\.(jpg|png|webp)")) {
            throw new BusinessLogicException("Tên ảnh đại diện không hợp lệ.");
        }
        Path resolved = storageDir.resolve(filename).normalize();
        if (!resolved.startsWith(storageDir)) {
            throw new BusinessLogicException("Đường dẫn ảnh đại diện không hợp lệ.");
        }
        return resolved;
    }

    @Override
    public void deleteAvatar(String currentAvatarUrl) {
        if (currentAvatarUrl == null || !currentAvatarUrl.startsWith("/user-content/avatars/")) {
            return;
        }
        String filename = currentAvatarUrl.substring("/user-content/avatars/".length());
        try {
            Files.deleteIfExists(safeResolve(filename));
        } catch (IOException ignored) {
            // The new avatar is already stored; stale file cleanup is best-effort only.
        }
    }
}
