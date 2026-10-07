package com.medclinic;

import com.medclinic.exception.BusinessLogicException;
import com.medclinic.util.ClinicalDatePolicy;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class ClinicalDemoPolicyTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T17:00:01Z"),
            ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    @ParameterizedTest @ValueSource(ints = {0, 1, 14})
    void demoAcceptsOnlyTheBookingWindow(int offset) {
        assertDoesNotThrow(() -> ClinicalDatePolicy.requireProcessableDate(TODAY.plusDays(offset), CLOCK, true));
    }

    @ParameterizedTest @ValueSource(ints = {-1, 15})
    void demoRejectsPastAndBeyondFourteenDays(int offset) {
        assertThrows(BusinessLogicException.class,
                () -> ClinicalDatePolicy.requireProcessableDate(TODAY.plusDays(offset), CLOCK, true));
    }

    @Test
    void nullDateIsRejectedAndStrictModeStillRequiresToday() {
        assertThrows(BusinessLogicException.class, () -> ClinicalDatePolicy.requireProcessableDate(null, CLOCK, true));
        assertDoesNotThrow(() -> ClinicalDatePolicy.requireProcessableDate(TODAY, CLOCK, false));
        assertThrows(BusinessLogicException.class,
                () -> ClinicalDatePolicy.requireProcessableDate(TODAY.plusDays(1), CLOCK, false));
    }

    @ParameterizedTest @ValueSource(strings = {"dev", "demo"})
    void earlyProcessingRequiresAnExplicitSettingInADemoProfile(String profile) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        assertFalse(ClinicalDatePolicy.earlyProcessingEnabled(environment));
        environment.setProperty("app.demo.early-clinical-processing", "true");
        assertTrue(ClinicalDatePolicy.earlyProcessingEnabled(environment));
        environment.setProperty("app.demo.early-clinical-processing", "false");
        assertFalse(ClinicalDatePolicy.earlyProcessingEnabled(environment));
    }

    @ParameterizedTest @ValueSource(strings = {"prod", "sqlserver"})
    void settingCannotEnableEarlyProcessingOutsideDemoProfiles(String profile) {
        var environment = new MockEnvironment().withProperty("app.demo.early-clinical-processing", "true");
        environment.setActiveProfiles(profile);
        assertFalse(ClinicalDatePolicy.earlyProcessingEnabled(environment));
        environment.setActiveProfiles("demo", "prod");
        assertFalse(ClinicalDatePolicy.earlyProcessingEnabled(environment));
    }
}
