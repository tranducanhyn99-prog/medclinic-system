package com.medclinic;

import com.medclinic.security.JdbcSqlServerRateLimitStore;
import com.medclinic.security.RateLimitStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@org.junit.jupiter.api.condition.EnabledIf("disposableLocalDatabaseConfigured")
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false", "spring.datasource.url=${DB_URL}"})
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
class SqlServerSharedRateLimitIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_(?:e2e|[a-z0-9_]+_it)(?:;.*)?");
    }
    @Autowired
    private RateLimitStore store;

    @Test
    void persistsAndAtomicallyIncrementsSharedBucket() {
        assertInstanceOf(JdbcSqlServerRateLimitStore.class, store);
        String key = "it-" + UUID.randomUUID();

        RateLimitStore.Decision first = store.increment(key, 1_000L, 60_000L, 1);
        RateLimitStore.Decision second = store.increment(key, 1_001L, 60_000L, 1);

        assertTrue(first.allowed());
        assertFalse(second.allowed());
        assertTrue(second.retryAfterSeconds() > 0);
    }
}
