package com.medclinic;

import com.medclinic.entity.Medicine;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.service.impl.MedicineServiceImpl;
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
    @InjectMocks MedicineServiceImpl service;

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

        Medicine saved = service.save("  Paracetamol  ", " viên ", new BigDecimal("1500"), "Sau ăn", 20);

        assertEquals("Paracetamol", saved.getName());
        assertEquals("viên", saved.getUnit());
        assertEquals(20, saved.getStockQuantity());
        assertTrue(saved.getIsActive());
    }
    @Test
    void rejectsDuplicateAndInvalidMedicineData() {
        when(medicineRepository.findByName("Paracetamol")).thenReturn(Optional.of(medicine(2L, "Paracetamol", true)));
        assertThrows(BusinessLogicException.class,
                () -> service.save("Paracetamol", "viên", BigDecimal.ONE, null, 1));

        assertThrows(BusinessLogicException.class,
                () -> service.save(" ", "viên", BigDecimal.ONE, null, 1));
        assertThrows(BusinessLogicException.class,
                () -> service.save("A", " ", BigDecimal.ONE, null, 1));
        assertThrows(BusinessLogicException.class,
                () -> service.save("A", "viên", new BigDecimal("-1"), null, 1));
        assertThrows(BusinessLogicException.class,
                () -> service.save("A", "viên", BigDecimal.ONE, null, -1));
    }

    @Test
    void updatesWhenNameBelongsToSameMedicineButRejectsOtherOwner() {
        Medicine current = medicine(1L, "Old", true);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(current));
        when(medicineRepository.findByName("New")).thenReturn(Optional.of(medicine(1L, "New", true)));
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(i -> i.getArgument(0));

        Medicine updated = service.update(1L, "New", " hộp ", new BigDecimal("2500"), "Guide", 4);
        assertEquals("New", updated.getName());
        assertEquals("hộp", updated.getUnit());

        when(medicineRepository.findByName("Taken")).thenReturn(Optional.of(medicine(2L, "Taken", true)));
        assertThrows(BusinessLogicException.class,
                () -> service.update(1L, "Taken", "viên", BigDecimal.ONE, null, 1));
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
}
