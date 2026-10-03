package com.medclinic;

import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Optional rendered evidence from isolated test data, never from the running clinic database. */
final class UxReviewFixture {
    private UxReviewFixture() {}

    static void write(String name, MockHttpServletResponse response) throws Exception {
        String directory = System.getProperty("medclinic.uxReviewDir");
        if (directory == null || directory.isBlank()) return;
        Path root = Path.of(directory).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Files.writeString(root.resolve(name + ".html"), response.getContentAsString(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
