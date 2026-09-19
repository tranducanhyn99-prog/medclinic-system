package com.medclinic.service.impl;

import com.medclinic.entity.Medicine;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Medicine> findAll() {
        return medicineRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medicine> findAllActive() {
        return medicineRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Medicine findById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity) {
        validateMedicine(name, unit, unitPrice, stockQuantity);
        if (medicineRepository.findByName(name).isPresent()) {
            throw new BusinessLogicException("Tên thuốc đã tồn tại");
        }
        Medicine medicine = new Medicine();
        medicine.setName(name.trim());
        medicine.setUnit(unit.trim());
        medicine.setUnitPrice(unitPrice);
        medicine.setUsageGuide(usageGuide);
        medicine.setStockQuantity(stockQuantity);
        medicine.setIsActive(true);
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine update(Long id, String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity) {
        validateMedicine(name, unit, unitPrice, stockQuantity);
        Medicine medicine = findById(id);
        medicineRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessLogicException("Tên thuốc đã tồn tại");
            }
        });
        medicine.setName(name.trim());
        medicine.setUnit(unit.trim());
        medicine.setUnitPrice(unitPrice);
        medicine.setUsageGuide(usageGuide);
        medicine.setStockQuantity(stockQuantity);
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleStatus(Long id) {
        Medicine medicine = findById(id);
        medicine.setIsActive(!Boolean.TRUE.equals(medicine.getIsActive()));
        medicineRepository.save(medicine);
    }

    private void validateMedicine(String name, String unit, BigDecimal unitPrice, int stockQuantity) {
        if (name == null || name.trim().isEmpty() || unit == null || unit.trim().isEmpty()) {
            throw new BusinessLogicException("Tên thuốc và đơn vị tính không được để trống");
        }
        if (unitPrice == null || unitPrice.signum() < 0 || stockQuantity < 0) {
            throw new BusinessLogicException("Đơn giá và tồn kho phải lớn hơn hoặc bằng 0");
        }
    }
}
