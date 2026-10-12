package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@EnabledIf("disposableLocalDatabaseConfigured")
@SpringBootTest(properties = {"app.auth.google.enabled=true", "app.auth.google.client-id=test-client",
        "app.auth.google.client-secret=test-secret-not-live", "spring.datasource.url=${DB_URL}",
        "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true"})
@ActiveProfiles(profiles = {"sqlserver", "demo"}, inheritProfiles = false)
class SqlServerGoogleLoginIntegrationTest extends GoogleLoginIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_google_it(?:;.*)?");
    }
}
