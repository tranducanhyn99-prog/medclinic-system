package com.medclinic.util;

import com.medclinic.exception.BusinessLogicException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/** Allows the defense demo to process booked dates without changing the business clock. */
public final class ClinicalDatePolicy {
    private ClinicalDatePolicy() {}

    public static boolean earlyProcessingEnabled(Environment environment) {
        return environment.acceptsProfiles(Profiles.of("dev", "demo"))
                && !environment.acceptsProfiles(Profiles.of("prod"))
                && environment.getProperty("app.demo.early-clinical-processing", Boolean.class, false);
    }

    public static LocalDate lastProcessableDate(Clock clock, boolean earlyProcessing) {
        LocalDate today = LocalDate.now(clock);
        return earlyProcessing ? today.plusDays(14) : today;
    }

    public static void requireProcessableDate(LocalDate date, Clock clock, boolean earlyProcessing) {
        LocalDate today = LocalDate.now(clock);
        if (date == null || date.isBefore(today) || date.isAfter(lastProcessableDate(clock, earlyProcessing))) {
            throw new BusinessLogicException(earlyProcessing
                    ? "Chế độ demo chỉ tiếp nhận và khám lịch từ hôm nay đến 14 ngày tới."
                    : "Chỉ tiếp nhận và khám lịch khám hôm nay.");
        }
    }
}
