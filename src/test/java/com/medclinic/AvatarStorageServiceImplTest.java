package com.medclinic;

import com.medclinic.service.AvatarStorageService;
import com.medclinic.exception.BusinessLogicException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class AvatarStorageServiceImplTest {
    @TempDir Path directory;

    @Test
    void previousAvatarRemainsUntilProfileUpdateSucceeds() throws Exception {
        AvatarStorageService storage = new AvatarStorageService(directory.toString());
        var image = image("png", 2, 2);

        String oldUrl = storage.storeAvatar(image, null);
        String newUrl = storage.storeAvatar(image, oldUrl);
        Path oldFile = directory.resolve(oldUrl.substring(oldUrl.lastIndexOf('/') + 1));
        Path newFile = directory.resolve(newUrl.substring(newUrl.lastIndexOf('/') + 1));

        assertTrue(Files.exists(oldFile));
        assertTrue(Files.exists(newFile));

        storage.deleteAvatar(newUrl); // profile persistence failed
        assertTrue(Files.exists(oldFile));
        assertFalse(Files.exists(newFile));

        newUrl = storage.storeAvatar(image, oldUrl);
        storage.deleteAvatar(oldUrl); // profile persistence succeeded
        assertFalse(Files.exists(oldFile));
        assertTrue(Files.exists(directory.resolve(newUrl.substring(newUrl.lastIndexOf('/') + 1))));
    }

    @Test
    void acceptsRealJpegPngAndWebp() throws Exception {
        AvatarStorageService storage = new AvatarStorageService(directory.toString());
        assertTrue(storage.storeAvatar(image("png", 2, 2), null).endsWith(".png"));
        assertTrue(storage.storeAvatar(image("jpeg", 2, 2), null).endsWith(".jpg"));
        byte[] webp = Files.readAllBytes(Path.of("src/main/resources/static/images/mascots/nurse-directions.webp"));
        assertTrue(storage.storeAvatar(new MockMultipartFile("avatar", "real.webp", "image/webp", webp), null).endsWith(".webp"));
    }

    @Test
    void rejectsFakeMismatchTruncatedAndOversizedImagesWithoutReplacingOldAvatar() throws Exception {
        AvatarStorageService storage = new AvatarStorageService(directory.toString());
        String old = storage.storeAvatar(image("png", 2, 2), null);
        byte[] png = image("png", 2, 2).getBytes();
        byte[] webp = Files.readAllBytes(Path.of("src/main/resources/static/images/mascots/nurse-directions.webp"));
        for (var invalid : new MockMultipartFile[]{
                new MockMultipartFile("avatar", "fake.png", "image/png", new byte[]{1, 2, 3}),
                new MockMultipartFile("avatar", "wrong.png", "image/png", image("jpeg", 2, 2).getBytes()),
                new MockMultipartFile("avatar", "cut.png", "image/png", Arrays.copyOf(png, png.length / 2)),
                new MockMultipartFile("avatar", "tail.png", "image/png", Arrays.copyOf(png, png.length - 1)),
                new MockMultipartFile("avatar", "cut.jpg", "image/jpeg", new byte[]{(byte)0xff, (byte)0xd8, 1}),
                new MockMultipartFile("avatar", "cut.webp", "image/webp", Arrays.copyOf(webp, webp.length / 2)),
                new MockMultipartFile("avatar", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1])}) {
            assertThrows(BusinessLogicException.class, () -> storage.storeAvatar(invalid, old));
        }
        assertTrue(Files.exists(directory.resolve(old.substring(old.lastIndexOf('/') + 1))));
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }

    @Test
    void checksDimensionsBeforeAllocatingDecodedPixels() throws Exception {
        AvatarStorageService storage = new AvatarStorageService(directory.toString());
        assertTrue(assertThrows(BusinessLogicException.class,
                () -> storage.storeAvatar(image("png", 8193, 1), null)).getMessage().contains("8192"));
        byte[] png = image("png", 1, 1).getBytes();
        java.nio.ByteBuffer.wrap(png).putInt(16, 4001).putInt(20, 4000);
        var crc = new java.util.zip.CRC32(); crc.update(png, 12, 17);
        java.nio.ByteBuffer.wrap(png).putInt(29, (int) crc.getValue());
        assertTrue(assertThrows(BusinessLogicException.class,
                () -> storage.storeAvatar(new MockMultipartFile("avatar", "pixels.png", "image/png", png), null))
                .getMessage().contains("16 triệu"));
        assertFalse(Files.exists(directory.resolve("unused")));
        try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
    }

    private MockMultipartFile image(String format, int width, int height) throws Exception {
        var bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, bytes));
        return new MockMultipartFile("avatar", "avatar." + format, "image/" + format, bytes.toByteArray());
    }
}
