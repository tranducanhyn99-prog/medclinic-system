package com.medclinic;

import com.medclinic.entity.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlServerSchemaGenerationTest {

    @Test
    void generatesSqlServerBaselineFromCurrentEntities() throws Exception {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
                .applySetting("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            for (Class<?> entity : List.of(
                    Role.class, User.class, Specialty.class, DoctorProfile.class,
                    DoctorSchedule.class, TimeSlot.class, Appointment.class,
                    MedicalRecord.class, Prescription.class, PrescriptionItem.class,
                    Medicine.class, Invoice.class, DailyQueueCounter.class,
                    InvoiceCodeReservation.class, PasswordResetToken.class,
                    Notification.class, BankTransferPayment.class,
                    BusinessAuditEvent.class, SecurityRateLimitBucket.class)) {
                sources.addAnnotatedClass(entity);
            }

            Metadata metadata = sources.buildMetadata();
            List<String> commands = new SchemaCreatorImpl(registry)
                    .generateCreationCommands(metadata, true);
            String ddl = String.join(System.lineSeparator() + "GO" + System.lineSeparator(), commands)
                    + System.lineSeparator() + "GO" + System.lineSeparator();

            Path out = Path.of("target", "sqlserver-baseline-generated.sql");
            Files.createDirectories(out.getParent());
            Files.writeString(out, ddl, StandardCharsets.UTF_8);

            String lower = ddl.toLowerCase();
            assertTrue(lower.contains("create table appointments"));
            assertTrue(lower.contains("create table password_reset_tokens"));
            assertTrue(lower.contains("create table notifications"));

            assertTrue(lower.contains("invoice_code_reservations"));
            assertTrue(lower.contains("bank_transfer_payments"));
            assertTrue(lower.contains("business_audit_events"));
            assertTrue(lower.contains("security_rate_limit_buckets"));
            assertTrue(lower.contains("time_slot_id"));
            assertTrue(lower.contains("queue_sequence"));
            assertTrue(lower.contains("unit_price_at_time"));
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
