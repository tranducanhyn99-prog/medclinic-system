package com.medclinic.repository;

import com.medclinic.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUserId(Long userId);
    List<DoctorProfile> findBySpecialtyId(Long specialtyId);

    @Query("SELECT d FROM DoctorProfile d JOIN d.user u WHERE u.isActive = true")
    List<DoctorProfile> findAllActiveDoctors();

    @Query("SELECT d FROM DoctorProfile d " +
           "JOIN d.user u " +
           "JOIN d.specialty s " +
           "WHERE u.isActive = true " +
           "AND (:specialtyId IS NULL OR s.id = :specialtyId) " +
           "AND (:maxFee IS NULL OR d.consultationFee <= :maxFee) " +
           "AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DoctorProfile> filterDoctors(@Param("specialtyId") Long specialtyId,
                                      @Param("maxFee") BigDecimal maxFee,
                                      @Param("keyword") String keyword);
}

