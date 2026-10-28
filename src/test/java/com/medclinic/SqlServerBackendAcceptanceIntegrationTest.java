package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
@org.junit.jupiter.api.condition.EnabledIf("disposableLocalDatabaseConfigured")
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false", "spring.datasource.url=${DB_URL}"})
class SqlServerBackendAcceptanceIntegrationTest extends BackendAcceptanceIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_(?:e2e|[a-z0-9_]+_it)(?:;.*)?");
    }
}
