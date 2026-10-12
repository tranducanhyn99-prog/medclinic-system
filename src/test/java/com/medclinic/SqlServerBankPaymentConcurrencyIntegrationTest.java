package com.medclinic;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@EnabledIf("disposableLocalDatabaseConfigured")
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
        "app.payment.bank.enabled=true", "app.payment.bank.account-no=0123456789",
        "app.payment.bank.account-name=CONCURRENCY TEST", "app.payment.bank.callback-secret=0123456789abcdef0123456789abcdef"})
@ActiveProfiles(profiles = {"dev", "sqlserver"}, inheritProfiles = false)
class SqlServerBankPaymentConcurrencyIntegrationTest extends BankPaymentConcurrencyIntegrationTest {
    static boolean disposableLocalDatabaseConfigured() {
        String url = System.getenv("DB_URL");
        return url != null && url.toLowerCase(java.util.Locale.ROOT).matches(
                "jdbc:sqlserver://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?;.*databasename=medclinic_payments_it(?:;.*)?");
    }
}
