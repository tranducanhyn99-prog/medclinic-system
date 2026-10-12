package com.medclinic;

import com.medclinic.entity.InventoryTransaction;
import com.medclinic.entity.Medicine;
import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.InventoryTransactionRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {
    @Mock MedicineRepository medicineRepository;
    @Mock InventoryTransactionRepository inventoryTransactionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks MedicineService service;

    private Medicine medicine(long id, String name, Boolean active) {
        Medicine medicine = new Medicine();
        medicine.setId(id); medicine.setName(name); medicine.setIsActive(active);
        medicine.setUnit("viên"); medicine.setUnitPrice(new BigDecimal("1000"));        medicine.setStockQuantity(10);
        return medicine;
    }

    @Test
    void readsCatalogAndHandlesMissingMedicine() {
        Medicine m = medicine(1L, "Paracetamol", true);
        when(medicineRepository.findAll()).thenReturn(List.of(m));
        when(medicineRepository.findByIsActiveTrue()).thenReturn(List.of(m));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));
        when(medicineRepository.findById(9L)).thenReturn(Optional.empty());

        assertEquals(List.of(m), service.findAll());
        assertEquals(List.of(m), service.findAllActive());
        assertSame(m, service.findById(1L));
        assertThrows(ResourceNotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void savesNormalizedMedicine() {
        when(medicineRepository.findByName("  Paracetamol  ")).thenReturn(Optional.empty());
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(i -> i.getArgument(0));

        Medicine saved = service.save("  Paracetamol  ", " viên ", new BigDecimal("1500"), "Sau ăn", 20, 50);

        assertEquals("Paracetamol", saved.getName());
        assertEquals("viên", saved.getUnit());
        assertEquals(20, saved.getStockQuantity());
        assertTrue(saved.getIsActive());
    }
    @Test
    void rejectsDuplicateAndInvalidMedicineData() {
        when(medicineRepository.findByName("Paracetamol")).thenReturn(Optional.of(medicine(2L, "Paracetamol", true)));
        assertThrows(BusinessLogicException.class,
                () -> service.save("Paracetamol", "viên", BigDecimal.ONE, null, 1, 50));

        assertThrows(BusinessLogicException.class,
                () -> service.save(" ", "viên", BigDecimal.ONE, null, 1, 50));
        assertThrows(BusinessLogicException.class,
                () -> service.save("A", " ", BigDecimal.ONE, null, 1, 50));
        assertThrows(BusinessLogicException.class,
                () -> service.save("A", "viên", new BigDecimal("-1"), null, 1, 50));
        assertThrows(BusinessLogicException.class,
                () -> service.save("A", "viên", BigDecimal.ONE, null, -1, 50));
    }

    @Test
    void updatesWhenNameBelongsToSameMedicineButRejectsOtherOwner() {
        Medicine current = medicine(1L, "Old", true);
        when(medicineRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(current));
        when(medicineRepository.findByName("New")).thenReturn(Optional.of(medicine(1L, "New", true)));
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(i -> i.getArgument(0));

        Medicine updated = service.updateCatalog(1L, "New", " hộp ", new BigDecimal("2500"), "Guide", 4);
        assertEquals("New", updated.getName());
        assertEquals("hộp", updated.getUnit());
        assertEquals(10, updated.getStockQuantity());
        assertEquals(4, updated.getReorderLevel());

        when(medicineRepository.findByName("Taken")).thenReturn(Optional.of(medicine(2L, "Taken", true)));
        assertThrows(BusinessLogicException.class,
                () -> service.updateCatalog(1L, "Taken", "viên", BigDecimal.ONE, null, 1));
    }
    @Test
    void togglesMedicineStatusBothDirections() {
        Medicine active = medicine(1L, "A", true);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(active));
        service.toggleStatus(1L);
        assertFalse(active.getIsActive());

        Medicine inactive = medicine(2L, "B", false);
        when(medicineRepository.findById(2L)).thenReturn(Optional.of(inactive));
        service.toggleStatus(2L);
        assertTrue(inactive.getIsActive());
        verify(medicineRepository, times(2)).save(any(Medicine.class));
    }
    @Test
    void restockIncreasesStockAndCreatesAuditTransaction() {
        Medicine current = medicine(1L, "Paracetamol", true);
        current.setStockQuantity(10);
        User admin = new User();
        admin.setId(99L);
        when(medicineRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(current));
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));

        Medicine updated = service.restock(1L, 25, new BigDecimal("800"), "Phiếu nhập PN-01", 99L);

        assertEquals(35, updated.getStockQuantity());
        verify(inventoryTransactionRepository).save(argThat(tx ->
                tx.getQuantityChange() == 25 && tx.getStockBefore() == 10 && tx.getStockAfter() == 35));
    }

    @Test
    void restockRejectsNonPositiveQuantity() {
        assertThrows(BusinessLogicException.class, () -> service.restock(1L, 0, null, null, 99L));
        verifyNoInteractions(inventoryTransactionRepository, userRepository);
    }

    @Test
    void rejectsFractionalAndOverflowPricesAtEveryCatalogEntryPoint() {
        Medicine current = medicine(1L, "Existing", true);
        when(medicineRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(current));
        for (String value : new String[]{"1000.50", "10000000000"}) {
            BigDecimal invalid = new BigDecimal(value);
            assertThrows(BusinessLogicException.class, () -> service.save("New", "viên", invalid, null, 0, 0));
            assertThrows(BusinessLogicException.class, () -> service.updateCatalog(1L, "Existing", "viên", invalid, null, 0));
            assertThrows(BusinessLogicException.class, () -> service.restock(1L, 1, invalid, null, 1L));
        }
        verify(medicineRepository, never()).save(any());
        assertEquals(new BigDecimal("1000"), current.getUnitPrice());
    }

}
