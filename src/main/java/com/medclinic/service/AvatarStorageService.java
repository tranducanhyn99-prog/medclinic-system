package com.medclinic.service;

import com.medclinic.exception.BusinessLogicException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AvatarStorageService {
    private static final long MAX_SIZE = 5L * 1024L * 1024L;
    private static final long MAX_PIXELS = 16_000_000;
    private static final int MAX_DIMENSION = 8192;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path storageDir;

    public AvatarStorageService(@Value("${app.storage.avatar-dir:uploads/avatars}") String storageDir) {
        this.storageDir = Path.of(storageDir).toAbsolutePath().normalize();
    }

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
        Path target = null;
        try (var input = file.getInputStream()) {
            byte[] bytes = input.readNBytes((int) MAX_SIZE + 1);
            if (bytes.length > MAX_SIZE) throw new BusinessLogicException("Ảnh đại diện tối đa 5 MB.");
            validateImage(bytes, contentType);
            Files.createDirectories(storageDir);
            String filename = UUID.randomUUID() + extension;
            target = safeResolve(filename);
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            return "/user-content/avatars/" + filename;
        } catch (IOException exception) {
            if (target != null) {
                try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            }
            throw new BusinessLogicException("Không thể lưu ảnh đại diện. Vui lòng thử lại.");
        }
    }

    private void validateImage(byte[] bytes, String contentType) {
        try (var stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new BusinessLogicException("File không phải ảnh JPG, PNG hoặc WebP hợp lệ.");
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                String expected = switch (contentType) {
                    case "image/jpeg" -> "jpeg";
                    case "image/png" -> "png";
                    case "image/webp" -> "webp";
                    default -> "";
                };
                if (!format.equals(expected)) throw new BusinessLogicException("Nội dung ảnh không khớp định dạng đã gửi.");
                if (format.equals("jpeg") && (bytes.length < 2 || bytes[bytes.length - 2] != (byte) 0xff
                        || bytes[bytes.length - 1] != (byte) 0xd9)) {
                    throw new BusinessLogicException("Ảnh JPG bị hỏng hoặc bị cắt.");
                }
                byte[] pngEnd = {0, 0, 0, 0, 'I', 'E', 'N', 'D', (byte) 0xae, 0x42, 0x60, (byte) 0x82};
                if (format.equals("png") && (bytes.length < pngEnd.length || !java.util.Arrays.equals(
                        java.util.Arrays.copyOfRange(bytes, bytes.length - pngEnd.length, bytes.length), pngEnd))) {
                    throw new BusinessLogicException("Ảnh PNG bị hỏng hoặc bị cắt.");
                }
                if (format.equals("webp")) {
                    long riffLength = Integer.toUnsignedLong(java.nio.ByteBuffer.wrap(bytes, 4, 4)
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt()) + 8;
                    if (riffLength != bytes.length) throw new BusinessLogicException("Ảnh WebP bị hỏng hoặc bị cắt.");
                    if (bytes.length > 20 && bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8'
                            && bytes[15] == 'X' && (bytes[20] & 2) != 0) {
                        throw new BusinessLogicException("Vui lòng dùng ảnh WebP tĩnh cho ảnh đại diện.");
                    }
                }
                reader.setInput(stream);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION
                        || (long) width * height > MAX_PIXELS) {
                    throw new BusinessLogicException("Ảnh tối đa 16 triệu pixel, mỗi chiều không quá 8192 pixel.");
                }
                reader.addIIOReadWarningListener((source, warning) -> {
                    throw new BusinessLogicException("Ảnh bị hỏng hoặc không đầy đủ. Vui lòng chọn ảnh khác.");
                });
                if (reader.read(0) == null) throw new BusinessLogicException("Không thể giải mã ảnh đại diện.");
            } finally {
                reader.dispose();
            }
        } catch (BusinessLogicException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new BusinessLogicException("Ảnh bị hỏng hoặc không thể giải mã. Vui lòng chọn ảnh khác.");
        }
    }

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

    public record AvatarResource(Resource resource, MediaType mediaType) {}
}
