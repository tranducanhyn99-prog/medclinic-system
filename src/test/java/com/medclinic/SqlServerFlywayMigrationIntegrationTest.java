package com.medclinic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@org.junit.jupiter.api.condition.EnabledIf("disposableLocalDatabaseConfigured")
class SqlServerFlywayMigrationIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_e2e(?:;.*)?");
    }
    private static final String MIGRATION_DATABASE = "medclinic_flyway_it";

    @Test
    void migratesFreshSchemaAndAdoptsLegacyBaseline() throws Exception {
        String baseUrl = requiredEnv("DB_URL");
        String username = optionalEnv("DB_USERNAME");
        String password = optionalEnv("DB_PASSWORD");
        assertSafeLocalAcceptanceUrl(baseUrl);

        String migrationUrl = withDatabase(baseUrl, MIGRATION_DATABASE);
        String masterUrl = withDatabase(baseUrl, "master");
        JdbcTemplate masterJdbc = jdbc(masterUrl, username, password);

        try {
            recreateMigrationDatabase(masterJdbc);
            try (ConfigurableApplicationContext context =
                         startSqlServerApplication(migrationUrl, username, password, false, true)) {
                JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
                assertSchemaAndRoles(jdbc);
                assertEquals("SQL", historyType(jdbc, "1"));
                assertEquals("SQL", historyType(jdbc, "2"));
                assertEquals("SQL", historyType(jdbc, "3"));
                assertEquals("SQL", historyType(jdbc, "4"));
                assertEquals("SQL", historyType(jdbc, "5"));
                assertEquals("SQL", historyType(jdbc, "6"));
                assertEquals("SQL", historyType(jdbc, "7"));
                assertEquals("SQL", historyType(jdbc, "8"));
                assertEquals("SQL", historyType(jdbc, "9"));
                assertEquals("SQL", historyType(jdbc, "10"));
                assertEquals(9, jdbc.queryForObject("select count(*) from users", Integer.class));
                assertEquals(6, jdbc.queryForObject("select count(*) from doctor_profiles", Integer.class));
            }

            recreateMigrationDatabase(masterJdbc);
            applyLegacyBaseline(migrationUrl, username, password);
            JdbcTemplate legacyJdbc = jdbc(migrationUrl, username, password);
            assertEquals(16, applicationTableCount(legacyJdbc));
            assertFalse(tableExists(legacyJdbc, "flyway_schema_history"));

            try (ConfigurableApplicationContext context =
                         startSqlServerApplication(migrationUrl, username, password, true, false)) {
                JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
                assertSchemaAndRoles(jdbc);
                assertEquals("BASELINE", historyType(jdbc, "1"));
                assertEquals("SQL", historyType(jdbc, "2"));
                assertEquals("SQL", historyType(jdbc, "3"));
                assertEquals("SQL", historyType(jdbc, "4"));
                assertEquals("SQL", historyType(jdbc, "5"));
                assertEquals("SQL", historyType(jdbc, "6"));
                assertEquals("SQL", historyType(jdbc, "7"));
                assertEquals("SQL", historyType(jdbc, "8"));
                assertEquals("SQL", historyType(jdbc, "9"));
                assertEquals("SQL", historyType(jdbc, "10"));
            }
        } finally {
            dropMigrationDatabase(masterJdbc);
        }
    }

    @Test
    void phoneMigrationRejectsLegacyDuplicatesAndBlanksWithoutChangingProfiles() {
        String baseUrl = requiredEnv("DB_URL");
        assertSafeLocalAcceptanceUrl(baseUrl);
        String username = optionalEnv("DB_USERNAME"), password = optionalEnv("DB_PASSWORD");
        String url = withDatabase(baseUrl, MIGRATION_DATABASE);
        JdbcTemplate master = jdbc(withDatabase(baseUrl, "master"), username, password);
        try {
            for (boolean duplicate : new boolean[]{true, false}) {
                recreateMigrationDatabase(master);
                flyway(url, username, password, "6").migrate();
                JdbcTemplate legacy = jdbc(url, username, password);
                String phone = duplicate ? "0912345678" : "";
                int count = duplicate ? 2 : 1;
                for (int i = 0; i < count; i++) {
                    legacy.update("INSERT INTO users(username,email,password,full_name,phone,is_active,created_at,role_id) "
                            + "VALUES(?,?,?,?,?,1,GETDATE(),(SELECT id FROM roles WHERE name='ROLE_PATIENT'))",
                            "legacy-phone-" + i, "legacy-phone-" + i + "@example.test", "encoded", "Legacy patient", phone);
                }
                assertThrows(org.flywaydb.core.api.FlywayException.class,
                        () -> flyway(url, username, password, "7").migrate());
                assertEquals(count, legacy.queryForObject("SELECT COUNT(*) FROM users WHERE phone = ?", Integer.class, phone));
                assertEquals(0, legacy.queryForObject("SELECT COUNT(*) FROM sys.indexes WHERE name='ux_users_phone'", Integer.class));
            }
        } finally { dropMigrationDatabase(master); }
    }

    @Test
    void moneyMigrationRejectsLegacyFractionsWithoutRoundingCatalog() {
        String baseUrl = requiredEnv("DB_URL");
        assertSafeLocalAcceptanceUrl(baseUrl);
        String username = optionalEnv("DB_USERNAME"), password = optionalEnv("DB_PASSWORD");
        String url = withDatabase(baseUrl, MIGRATION_DATABASE);
        JdbcTemplate master = jdbc(withDatabase(baseUrl, "master"), username, password);
        try {
            recreateMigrationDatabase(master);
            flyway(url, username, password, "8").migrate();
            JdbcTemplate legacy = jdbc(url, username, password);
            legacy.update("INSERT INTO medicines(name,unit,unit_price,stock_quantity,is_active) VALUES('Fraction test','vien',1000.50,0,1)");
            assertThrows(org.flywaydb.core.api.FlywayException.class,
                    () -> flyway(url, username, password, "9").migrate());
            assertEquals(0, new java.math.BigDecimal("1000.50").compareTo(
                    legacy.queryForObject("SELECT unit_price FROM medicines WHERE name='Fraction test'", java.math.BigDecimal.class)));
        } finally { dropMigrationDatabase(master); }
    }

    private org.flywaydb.core.Flyway flyway(String url, String username, String password, String target) {
        return org.flywaydb.core.Flyway.configure().dataSource(url, username, password)
                .locations("classpath:db/migration/sqlserver").target(target).load();
    }

    private ConfigurableApplicationContext startSqlServerApplication(
            String url, String username, String password,
            boolean baselineOnMigrate, boolean demoProfile) {
        return new SpringApplicationBuilder(MedClinicApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(
                        "--spring.profiles.active=" + (demoProfile ? "sqlserver,demo" : "sqlserver"),
                        "--server.port=0",
                        "--spring.datasource.url=" + url,
                        "--spring.datasource.username=" + username,
                        "--spring.datasource.password=" + password,
                        "--spring.flyway.baseline-on-migrate=" + baselineOnMigrate,
                        "--app.security.remember-me-key=flyway-test-only-key");
    }

    private void recreateMigrationDatabase(JdbcTemplate masterJdbc) {
        dropMigrationDatabase(masterJdbc);
        masterJdbc.execute("CREATE DATABASE [" + MIGRATION_DATABASE + "]");
    }

    private void dropMigrationDatabase(JdbcTemplate masterJdbc) {
        Integer count = masterJdbc.queryForObject(
                "SELECT COUNT(*) FROM sys.databases WHERE name = ?",
                Integer.class,
                MIGRATION_DATABASE);
        if (count != null && count > 0) {
            masterJdbc.execute("ALTER DATABASE [" + MIGRATION_DATABASE
                    + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
            masterJdbc.execute("DROP DATABASE [" + MIGRATION_DATABASE + "]");
        }
    }

    private void applyLegacyBaseline(
            String url, String username, String password) throws Exception {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(url, username, password);
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "db/migration/sqlserver/V1__baseline_schema.sql"));
        }
    }

    private JdbcTemplate jdbc(String url, String username, String password) {
        return new JdbcTemplate(
                new DriverManagerDataSource(url, username, password));
    }

    private void assertSchemaAndRoles(JdbcTemplate jdbc) {
        assertEquals(21, applicationTableCount(jdbc));
        assertTrue(tableExists(jdbc, "flyway_schema_history"));
        assertTrue(tableExists(jdbc, "inventory_transactions"));
        assertTrue(tableExists(jdbc, "auth_verification_challenges"));
        assertTrue(tableExists(jdbc, "bank_transfer_payments"));
        assertTrue(tableExists(jdbc, "business_audit_events"));
        assertTrue(tableExists(jdbc, "security_rate_limit_buckets"));
        assertEquals(10, jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = 1",
                Integer.class));
        assertEquals(4, jdbc.queryForObject(
                "select count(*) from roles where name in ('ROLE_PATIENT','ROLE_DOCTOR','ROLE_STAFF','ROLE_ADMIN')",
                Integer.class));
    }

    private int applicationTableCount(JdbcTemplate jdbc) {
        return jdbc.queryForObject(
                "select count(*) from sys.tables where name <> 'flyway_schema_history'",
                Integer.class);
    }

    private boolean tableExists(JdbcTemplate jdbc, String tableName) {
        Integer count = jdbc.queryForObject(
                "select count(*) from sys.tables where name = ?",
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    private String historyType(JdbcTemplate jdbc, String version) {
        return jdbc.queryForObject(
                "select type from flyway_schema_history where version = ? and success = 1",
                String.class,
                version);
    }

    private String withDatabase(String url, String databaseName) {
        if (!url.toLowerCase().contains("databasename=")) {
            throw new IllegalStateException("DB_URL must contain databaseName");
        }
        return url.replaceFirst("(?i)databaseName=[^;]*", "databaseName=" + databaseName);
    }

    private String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + " must be configured for SQL Server integration tests");
        }
        return value;
    }

    private String optionalEnv(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }

    private void assertSafeLocalAcceptanceUrl(String url) {
        String normalized = url.toLowerCase();
        boolean localServer = normalized.startsWith("jdbc:sqlserver://localhost")
                || normalized.startsWith("jdbc:sqlserver://127.0.0.1");
        if (!localServer || !normalized.contains("databasename=medclinic_e2e")) {
            throw new IllegalStateException(
                    "Flyway migration test requires local medclinic_e2e as its base URL");
        }
    }
}
