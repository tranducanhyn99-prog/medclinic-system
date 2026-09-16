package com.medclinic.service.impl;

import com.medclinic.entity.Medicine;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    public Page<Medicine> searchMedicines(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return medicineRepository.findByNameContainingIgnoreCaseOrUsageGuideContainingIgnoreCase(
                normalizedKeyword, normalizedKeyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Medicine findById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
    }

    @Override
    @Transactional
    public Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity) {
        if (medicineRepository.findByName(name).isPresent()) {
            throw new BusinessLogicException("Tên thuốc đã tồn tại");
        }
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setUnit(unit);
        medicine.setUnitPrice(unitPrice);
        medicine.setUsageGuide(usageGuide);
        medicine.setStockQuantity(stockQuantity);
        medicine.setIsActive(true);
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    public Medicine update(Long id, String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity) {
        Medicine medicine = findById(id);
        medicineRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessLogicException("Tên thuốc đã tồn tại");
            }
        });
        medicine.setName(name);
        medicine.setUnit(unit);
        medicine.setUnitPrice(unitPrice);
        medicine.setUsageGuide(usageGuide);
        medicine.setStockQuantity(stockQuantity);
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Medicine medicine = findById(id);
        medicine.setIsActive(!Boolean.TRUE.equals(medicine.getIsActive()));
        medicineRepository.save(medicine);
    }
}
