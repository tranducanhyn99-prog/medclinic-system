package com.medclinic.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProductionProfileGuard implements ApplicationRunner {
    private final Environment environment;

    public ProductionProfileGuard(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> profiles = Set.of(environment.getActiveProfiles());
        if (profiles.contains("prod") && (profiles.contains("dev") || profiles.contains("demo"))) {
            throw new IllegalStateException("Profile prod không được chạy cùng dev hoặc demo");
        }
    }
}
