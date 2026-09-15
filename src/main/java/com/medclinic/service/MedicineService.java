package com.medclinic.service;

import com.medclinic.entity.Medicine;
import java.math.BigDecimal;
import java.util.List;

public interface MedicineService {
    List<Medicine> findAll();
    List<Medicine> findAllActive();
    Medicine findById(Long id);
    Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity);
    Medicine update(Long id, String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity);
    void toggleStatus(Long id);
}
