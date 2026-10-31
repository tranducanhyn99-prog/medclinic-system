package com.medclinic.config;

import com.medclinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class OverdueAppointmentScheduler {

    private final AppointmentService appointmentService;

    @Scheduled(
            fixedDelayString = "${app.scheduling.overdue-check-ms:60000}",
            initialDelayString = "${app.scheduling.overdue-initial-delay-ms:10000}"
    )
    public void autoExpireOverdueAppointments() {
        int expiredCount = appointmentService.expireOverdueTodayAppointments();
        if (expiredCount > 0) {
            log.info("Đã tự động chuyển {} lịch hẹn quá giờ sang trạng thái NO_SHOW (Quá giờ / Vắng mặt).", expiredCount);
        }
    }
}
