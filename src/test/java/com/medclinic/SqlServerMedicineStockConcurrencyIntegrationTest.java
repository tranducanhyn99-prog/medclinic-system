package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@org.junit.jupiter.api.condition.EnabledIf("disposableLocalDatabaseConfigured")
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false", "spring.datasource.url=${DB_URL}"})
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
class SqlServerMedicineStockConcurrencyIntegrationTest extends MedicineStockConcurrencyIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_(?:e2e|[a-z0-9_]+_it)(?:;.*)?");
    }
}
