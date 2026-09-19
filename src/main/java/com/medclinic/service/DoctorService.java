package com.medclinic.service;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.dto.AdminDoctorCreateDto;
import java.math.BigDecimal;
import java.util.List;

public interface DoctorService {
    List<DoctorProfile> findAll();
    List<DoctorProfile> findBySpecialty(Long specialtyId);
    DoctorProfile findById(Long id);
    DoctorProfile findByUserId(Long userId);
    List<DoctorProfile> filterDoctors(Long specialtyId, BigDecimal maxFee, String keyword);
    DoctorProfile save(Long userId, Long specialtyId, String title, int expYears, BigDecimal fee, String bio);
    DoctorProfile registerDoctor(AdminDoctorCreateDto doctorDto);
    DoctorProfile update(Long id, Long specialtyId, String title, int expYears, BigDecimal fee, String bio);
}
