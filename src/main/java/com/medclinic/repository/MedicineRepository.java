package com.medclinic.repository;

import com.medclinic.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByName(String name);
    List<Medicine> findByIsActiveTrue();

    @Query("SELECT m FROM Medicine m WHERE m.isActive = true AND LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medicine> searchMedicines(@Param("keyword") String keyword);

    Page<Medicine> findByNameContainingIgnoreCaseOrUsageGuideContainingIgnoreCase(
            String name, String usageGuide, Pageable pageable);
}

