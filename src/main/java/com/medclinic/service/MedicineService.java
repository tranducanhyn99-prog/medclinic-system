package com.medclinic.service;

import com.medclinic.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface MedicineService {
    List<Medicine> findAll();
    List<Medicine> findAllActive();
    Page<Medicine> searchMedicines(String keyword, Pageable pageable);
    Medicine findById(Long id);
    Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity);
    Medicine update(Long id, String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity);
    void toggleStatus(Long id);
}
