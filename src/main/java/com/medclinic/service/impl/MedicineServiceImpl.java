package com.medclinic.service.impl;

import com.medclinic.entity.InventoryTransaction;
import com.medclinic.entity.Medicine;
import com.medclinic.entity.User;
import com.medclinic.enums.InventoryTransactionType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.InventoryTransactionRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {
    private static final int DEFAULT_REORDER_LEVEL = 50;

    private final MedicineRepository medicineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final UserRepository userRepository;

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
    @Transactional(readOnly = true)
    public List<InventoryTransaction> findRecentTransactions() {
        return inventoryTransactionRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStock() {
        return medicineRepository.countLowStock();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity) {
        return save(name, unit, unitPrice, usageGuide, stockQuantity, DEFAULT_REORDER_LEVEL);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide,
                         int stockQuantity, int reorderLevel) {
        validateMedicine(name, unit, unitPrice, stockQuantity, reorderLevel);
        if (medicineRepository.findByName(name).isPresent()) {
            throw new BusinessLogicException("Tên thuốc đã tồn tại");
        }
        Medicine medicine = new Medicine();
        medicine.setName(name.trim());
        medicine.setUnit(unit.trim());
        medicine.setUnitPrice(unitPrice);
        medicine.setUsageGuide(usageGuide);
        medicine.setStockQuantity(stockQuantity);
        medicine.setReorderLevel(reorderLevel);
        medicine.setIsActive(true);
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine update(Long id, String name, String unit, BigDecimal unitPrice,
                           String usageGuide, int stockQuantity) {
        Medicine medicine = findById(id);
        int reorderLevel = medicine.getReorderLevel() != null ? medicine.getReorderLevel() : DEFAULT_REORDER_LEVEL;
        validateMedicine(name, unit, unitPrice, stockQuantity, reorderLevel);
        validateUniqueName(id, name);
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
    public Medicine updateCatalog(Long id, String name, String unit, BigDecimal unitPrice,
                                  String usageGuide, int reorderLevel) {
        Medicine medicine = medicineRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
        validateMedicine(name, unit, unitPrice, medicine.getStockQuantity(), reorderLevel);
        validateUniqueName(id, name);
        medicine.setName(name.trim());
        medicine.setUnit(unit.trim());
        medicine.setUnitPrice(unitPrice);
        medicine.setUsageGuide(usageGuide);
        medicine.setReorderLevel(reorderLevel);
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine restock(Long id, int quantity, BigDecimal unitCost, String note, Long adminUserId) {
        if (quantity <= 0) {
            throw new BusinessLogicException("Số lượng nhập thêm phải lớn hơn 0");
        }
        if (unitCost != null && unitCost.signum() < 0) {
            throw new BusinessLogicException("Giá nhập không được âm");
        }
        Medicine medicine = medicineRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản quản trị"));
        int before = medicine.getStockQuantity();
        final int after;
        try {
            after = Math.addExact(before, quantity);
        } catch (ArithmeticException ex) {
            throw new BusinessLogicException("Số lượng tồn kho vượt giới hạn cho phép");
        }
        medicine.setStockQuantity(after);
        medicineRepository.save(medicine);
        inventoryTransactionRepository.save(InventoryTransaction.builder()
                .medicine(medicine).type(InventoryTransactionType.RESTOCK)
                .quantityChange(quantity).stockBefore(before).stockAfter(after)
                .unitCost(unitCost).note(normalizeNote(note))
                .performedBy(admin).build());
        return medicine;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleStatus(Long id) {
        Medicine medicine = findById(id);
        medicine.setIsActive(!Boolean.TRUE.equals(medicine.getIsActive()));
        medicineRepository.save(medicine);
    }

    private void validateUniqueName(Long id, String name) {
        medicineRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessLogicException("Tên thuốc đã tồn tại");
            }
        });
    }

    private void validateMedicine(String name, String unit, BigDecimal unitPrice,
                                  int stockQuantity, int reorderLevel) {
        if (name == null || name.trim().isEmpty() || unit == null || unit.trim().isEmpty()) {
            throw new BusinessLogicException("Tên thuốc và đơn vị tính không được để trống");
        }
        if (unitPrice == null || unitPrice.signum() < 0 || stockQuantity < 0 || reorderLevel < 0) {
            throw new BusinessLogicException("Đơn giá, tồn kho và ngưỡng cảnh báo phải lớn hơn hoặc bằng 0");
        }
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) return null;
        String normalized = note.trim();
        return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
    }
}
