package com.medclinic.service;

import com.medclinic.entity.InventoryTransaction;
import com.medclinic.entity.Medicine;

import java.math.BigDecimal;
import java.util.List;

public interface MedicineService {
    List<Medicine> findAll();
    List<Medicine> findAllActive();
    Medicine findById(Long id);
    List<InventoryTransaction> findRecentTransactions();
    long countLowStock();

    Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide, int stockQuantity);
    Medicine save(String name, String unit, BigDecimal unitPrice, String usageGuide,
                  int stockQuantity, int reorderLevel);

    Medicine update(Long id, String name, String unit, BigDecimal unitPrice,
                    String usageGuide, int stockQuantity);
    Medicine updateCatalog(Long id, String name, String unit, BigDecimal unitPrice,
                           String usageGuide, int reorderLevel);

    Medicine restock(Long id, int quantity, BigDecimal unitCost, String note, Long adminUserId);
    void toggleStatus(Long id);
}
