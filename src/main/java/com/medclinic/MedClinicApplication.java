package com.medclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MedClinicApplication {

    public static void main(String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        loadDotEnvIfPresent(Path.of(".env"));
        if (System.getenv("DEMO_SEED_APPOINTMENTS") == null && System.getProperty("app.demo.seed-appointments") == null) {
            System.setProperty("app.demo.seed-appointments", "true");
        }
        SpringApplication.run(MedClinicApplication.class, args);
    }

    static void loadDotEnvIfPresent(Path dotEnvPath) {
        if (!Files.isRegularFile(dotEnvPath)) {
            return;
        }
        try {
            Map<String, String> parsed = new LinkedHashMap<>();
            for (String rawLine : Files.readAllLines(dotEnvPath, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (!value.isEmpty() || !parsed.containsKey(key)) {
                    parsed.put(key, value);
                }
            }
            for (Map.Entry<String, String> entry : parsed.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!value.isEmpty() && System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
