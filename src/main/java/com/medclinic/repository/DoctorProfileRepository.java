package com.medclinic.repository;

import com.medclinic.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUserId(Long userId);
    Optional<DoctorProfile> findByLicenseNumber(String licenseNumber);
}

