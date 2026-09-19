package com.medclinic.service.impl;

import com.medclinic.entity.Specialty;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.SpecialtyRepository;
import com.medclinic.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Specialty> findAll() {
        return specialtyRepository.findAllWithDoctors();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Specialty> findAllActive() {
        return specialtyRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Specialty findById(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Specialty save(String name, String description, String icon) {
        validateName(name);
        if (specialtyRepository.findByName(name).isPresent()) {
            throw new BusinessLogicException("Tên chuyên khoa đã tồn tại");
        }
        Specialty specialty = new Specialty();
        specialty.setName(name.trim());
        specialty.setDescription(description);
        specialty.setIcon(icon);
        specialty.setIsActive(true);
        return specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Specialty update(Long id, String name, String description, String icon) {
        validateName(name);
        Specialty specialty = findById(id);
        specialtyRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessLogicException("Tên chuyên khoa đã tồn tại");
            }
        });
        specialty.setName(name.trim());
        specialty.setDescription(description);
        specialty.setIcon(icon);
        return specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleStatus(Long id) {
        Specialty specialty = findById(id);
        specialty.setIsActive(!Boolean.TRUE.equals(specialty.getIsActive()));
        specialtyRepository.save(specialty);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessLogicException("Tên chuyên khoa không được để trống");
        }
    }
}
