package com.medclinic.service;

import com.medclinic.enums.RoleType;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public long countTotalPatients() {
        return userRepository.countByRole_Name(RoleType.ROLE_PATIENT);
    }

    @Transactional(readOnly = true)
    public long countTotalDoctors() {
        return doctorProfileRepository.count();
    }

    @Transactional(readOnly = true)
    public long countTodayAppointments() {
        return appointmentRepository.countByAppointmentDate(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal total = invoiceRepository.calculateTotalPaidRevenue();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getAppointmentsByStatusGroup() {
        List<Object[]> results = appointmentRepository.countAppointmentsByStatusGroup();
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            String status = result[0].toString();
            Long count = ((Number) result[1]).longValue();
            map.put(status, count);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getAppointmentsBySpecialtyGroup() {
        List<Object[]> results = appointmentRepository.countAppointmentsBySpecialtyGroup();
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            String specialtyName = result[0].toString();
            Long count = ((Number) result[1]).longValue();
            map.put(specialtyName, count);
        }
        return map;
    }
}
