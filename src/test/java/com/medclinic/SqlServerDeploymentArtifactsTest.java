package com.medclinic;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlServerDeploymentArtifactsTest {
    private static final Path DATABASE_DOCS = Path.of("docs", "database");

    @Test
    void referenceDataScriptSeedsEveryMandatoryRoleIdempotently() throws IOException {
        String sql = Files.readString(DATABASE_DOCS.resolve("SQLSERVER_REFERENCE_DATA_2026-09-19.sql"));

        for (String role : new String[]{"ROLE_PATIENT", "ROLE_DOCTOR", "ROLE_STAFF", "ROLE_ADMIN"}) {
            assertTrue(sql.contains("IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'" + role + "')"));
            assertTrue(sql.contains("N'" + role + "'"));
        }
        assertTrue(sql.contains("SET XACT_ABORT ON"));
        assertTrue(sql.contains("BEGIN TRANSACTION"));
        assertTrue(sql.contains("COMMIT TRANSACTION"));
    }

    @Test
    void adminProvisioningTemplateRequiresRoleAndBcryptWithoutDefaultSecrets() throws IOException {
        String sql = Files.readString(DATABASE_DOCS.resolve("SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql"));
        String guide = Files.readString(DATABASE_DOCS.resolve("PRODUCTION_ADMIN_PROVISIONING.md"));

        assertTrue(sql.contains("WHERE name = N'ROLE_ADMIN'"));
        assertTrue(sql.contains("Admin username already exists"));
        assertTrue(sql.contains("Admin email already exists"));
        assertTrue(sql.contains("AdminPasswordHash"));
        assertTrue(sql.contains("BCrypt"));
        assertFalse(sql.contains("123456"));

        assertTrue(guide.contains("SQLSERVER_REFERENCE_DATA_2026-09-19.sql"));
        assertTrue(guide.contains("SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql"));
        assertTrue(guide.contains("không có"));
        assertTrue(guide.contains("must_change_password"));
    }
}
