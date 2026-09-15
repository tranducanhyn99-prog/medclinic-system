package com.medclinic.service;

import com.medclinic.entity.Specialty;
import java.util.List;

public interface SpecialtyService {
    List<Specialty> findAll();
    List<Specialty> findAllActive();
    Specialty findById(Long id);
    Specialty save(String name, String description, String icon);
    Specialty update(Long id, String name, String description, String icon);
    void toggleStatus(Long id);
}
