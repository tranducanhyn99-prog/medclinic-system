package com.medclinic;

import com.medclinic.service.AvatarStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AvatarStorageServiceImplTest {
    @TempDir Path directory;

    @Test
    void previousAvatarRemainsUntilProfileUpdateSucceeds() throws Exception {
        AvatarStorageService storage = new AvatarStorageService(directory.toString());
        var image = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[] {1, 2, 3});

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
}
