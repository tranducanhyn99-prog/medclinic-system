package com.medclinic;

import com.medclinic.entity.Specialty;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.SpecialtyRepository;
import com.medclinic.service.SpecialtyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {
    @Mock SpecialtyRepository specialtyRepository;
    @InjectMocks SpecialtyService service;

    private Specialty specialty(long id, String name, Boolean active) {
        Specialty specialty = new Specialty();
        specialty.setId(id); specialty.setName(name); specialty.setIsActive(active);
        return specialty;
    }
    @Test
    void readsCatalogAndHandlesMissingSpecialty() {
        Specialty s = specialty(1L, "Tim mạch", true);
        when(specialtyRepository.findAllWithDoctors()).thenReturn(List.of(s));
        when(specialtyRepository.findByIsActiveTrue()).thenReturn(List.of(s));
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(s));
        when(specialtyRepository.findById(9L)).thenReturn(Optional.empty());

        assertEquals(List.of(s), service.findAll());
        assertEquals(List.of(s), service.findAllActive());
        assertSame(s, service.findById(1L));
        assertThrows(ResourceNotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void savesNormalizedSpecialtyAndRejectsDuplicateOrBlankName() {
        when(specialtyRepository.findByName("  Nhi khoa  ")).thenReturn(Optional.empty());
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(i -> i.getArgument(0));

        Specialty saved = service.save("  Nhi khoa  ", "Mô tả", "bi-heart");
        assertEquals("Nhi khoa", saved.getName());
        assertTrue(saved.getIsActive());

        when(specialtyRepository.findByName("Tim mạch"))
                .thenReturn(Optional.of(specialty(2L, "Tim mạch", true)));
        assertThrows(BusinessLogicException.class,
                () -> service.save("Tim mạch", null, null));
        assertThrows(BusinessLogicException.class, () -> service.save(null, null, null));
        assertThrows(BusinessLogicException.class, () -> service.save("  ", null, null));
    }
    @Test
    void updatesWhenNameBelongsToSameSpecialtyButRejectsOtherOwner() {
        Specialty current = specialty(1L, "Old", true);
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(current));
        when(specialtyRepository.findByName("New"))
                .thenReturn(Optional.of(specialty(1L, "New", true)));
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(i -> i.getArgument(0));

        Specialty updated = service.update(1L, "New", "Desc", "icon");
        assertEquals("New", updated.getName());
        assertEquals("Desc", updated.getDescription());

        when(specialtyRepository.findByName("Taken"))
                .thenReturn(Optional.of(specialty(2L, "Taken", true)));
        assertThrows(BusinessLogicException.class,
                () -> service.update(1L, "Taken", null, null));
    }

    @Test
    void togglesSpecialtyStatusBothDirections() {
        Specialty active = specialty(1L, "A", true);
        Specialty inactive = specialty(2L, "B", false);
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(active));
        when(specialtyRepository.findById(2L)).thenReturn(Optional.of(inactive));
        service.toggleStatus(1L); service.toggleStatus(2L);
        assertFalse(active.getIsActive());
        assertTrue(inactive.getIsActive());
        verify(specialtyRepository, times(2)).save(any(Specialty.class));
    }
}
