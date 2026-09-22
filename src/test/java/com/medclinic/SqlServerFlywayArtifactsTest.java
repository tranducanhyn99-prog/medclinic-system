package com.medclinic;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlServerFlywayArtifactsTest {
    private static final Path DATABASE_DOCS = Path.of("docs", "database");
    private static final Path MIGRATIONS = Path.of(
            "src", "main", "resources", "db", "migration", "sqlserver");

    @Test
    void versionedMigrationsStayAlignedWithReviewedSqlServerScripts() throws IOException {
        assertEquals(
                normalized(DATABASE_DOCS.resolve("SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql")),
                normalized(MIGRATIONS.resolve("V1__baseline_schema.sql")));
        assertEquals(
                normalized(DATABASE_DOCS.resolve("SQLSERVER_REFERENCE_DATA_2026-09-19.sql")),
                normalized(MIGRATIONS.resolve("V2__reference_roles.sql")));
        String sessionStateMigration = normalized(
                MIGRATIONS.resolve("V3__restore_jdbc_session_state.sql"));
        assertTrue(sessionStateMigration.contains("SET NOCOUNT OFF;"));
        String inventoryMigration = normalized(
                MIGRATIONS.resolve("V4__inventory_restock_and_low_stock.sql"));
        assertTrue(inventoryMigration.contains("reorder_level"));
        assertTrue(inventoryMigration.contains("inventory_transactions"));
        assertTrue(inventoryMigration.contains("RESTOCK"));
        assertTrue(inventoryMigration.contains("DISPENSE"));
    }

    @Test
    void sqlServerProfileUsesFlywayBeforeHibernateValidation() throws IOException {
        String config = Files.readString(Path.of("src", "main", "resources", "application.yml"));

        assertTrue(config.contains("locations: classpath:db/migration/sqlserver"));
        assertTrue(config.contains("baseline-on-migrate: false"));
        assertTrue(config.contains("baseline-version: 1"));
        assertTrue(config.contains("ddl-auto: validate"));
        assertTrue(config.contains("defer-datasource-initialization: false"));
        assertFalse(Files.exists(Path.of(
                "src", "main", "resources", "application-sqlserver.properties")));
    }

    @Test
    void dockerInitializerOnlyCreatesAndGuardsDatabaseForFlyway() throws IOException {
        String script = Files.readString(Path.of("docker", "sqlserver", "init-db.sh"));
        String compose = Files.readString(Path.of("compose.yml"));

        assertTrue(script.contains("CREATE DATABASE"));
        assertTrue(script.contains("flyway_schema_history"));
        assertTrue(script.contains("APP_TABLE_COUNT"));
        assertFalse(script.contains("SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql"));
        assertFalse(script.contains("SQLSERVER_REFERENCE_DATA_2026-09-19.sql"));
        assertTrue(compose.contains("SPRING_FLYWAY_BASELINE_ON_MIGRATE: \"true\""));
        assertTrue(compose.contains("${SQLSERVER_HOST_PORT:-14333}:1433"));
        assertTrue(compose.contains("/actuator/health"));
    }

    private String normalized(Path path) throws IOException {
        return Files.readString(path).replace("\r\n", "\n").trim();
    }
}
