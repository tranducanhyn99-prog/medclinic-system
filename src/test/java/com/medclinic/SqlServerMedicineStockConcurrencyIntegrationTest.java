package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
class SqlServerMedicineStockConcurrencyIntegrationTest extends MedicineStockConcurrencyIntegrationTest {
}
