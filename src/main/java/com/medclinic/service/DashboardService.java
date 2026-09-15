package com.medclinic.service;

import java.math.BigDecimal;
import java.util.Map;

public interface DashboardService {
    long countTotalPatients();
    long countTotalDoctors();
    long countTodayAppointments();
    BigDecimal getTotalRevenue();
    Map<String, Long> getAppointmentsByStatusGroup();
    Map<String, Long> getAppointmentsBySpecialtyGroup();
}
