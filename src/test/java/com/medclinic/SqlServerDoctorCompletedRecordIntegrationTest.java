package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@EnabledIf("disposableLocalDatabaseConfigured")
@SpringBootTest(properties = {"spring.datasource.url=${DB_URL}",
        "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true"})
@ActiveProfiles(profiles = {"sqlserver", "demo"}, inheritProfiles = false)
class SqlServerDoctorCompletedRecordIntegrationTest extends DoctorCompletedRecordIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_records_it(?:;.*)?");
    }
}
