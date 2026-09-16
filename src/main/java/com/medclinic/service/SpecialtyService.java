package com.medclinic.service;

import com.medclinic.entity.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SpecialtyService {
    List<Specialty> findAll();
    List<Specialty> findAllActive();
    Page<Specialty> searchSpecialties(String keyword, Pageable pageable);
    Specialty findById(Long id);
    Specialty save(String name, String description, String icon);
    Specialty update(Long id, String name, String description, String icon);
    void toggleStatus(Long id);
}
