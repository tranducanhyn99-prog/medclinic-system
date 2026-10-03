package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false"})
class SqlServerBackendAcceptanceIntegrationTest extends BackendAcceptanceIntegrationTest { }
