package com.medclinic.service.impl;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.entity.Specialty;
import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.SpecialtyRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.DoctorService;
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
public class DoctorServiceImpl implements DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfile> findAll() {
        return doctorProfileRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfile> findBySpecialty(Long specialtyId) {
        return doctorProfileRepository.findBySpecialtyId(specialtyId);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfile findById(Long id) {
        return doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ"));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfile findByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ cho người dùng này"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfile> filterDoctors(Long specialtyId, BigDecimal maxFee, String keyword) {
        return findAll().stream()
                .filter(doc -> specialtyId == null || (doc.getSpecialty() != null && doc.getSpecialty().getId().equals(specialtyId)))
                .filter(doc -> maxFee == null || doc.getConsultationFee().compareTo(maxFee) <= 0)
                .filter(doc -> {
                    if (keyword == null || keyword.trim().isEmpty()) {
                        return true;
                    }
                    String lowerKeyword = keyword.toLowerCase();
                    boolean matchName = doc.getUser() != null && doc.getUser().getFullName() != null && doc.getUser().getFullName().toLowerCase().contains(lowerKeyword);
                    boolean matchTitle = doc.getTitle() != null && doc.getTitle().toLowerCase().contains(lowerKeyword);
                    return matchName || matchTitle;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorProfile> searchForAdmin(Long specialtyId, String keyword, Pageable pageable) {
        return doctorProfileRepository.searchForAdmin(
                specialtyId, keyword == null ? "" : keyword.trim(), pageable);
    }

    @Override
    @Transactional
    public DoctorProfile save(Long userId, Long specialtyId, String title, int expYears, BigDecimal fee, String bio) {
        if (doctorProfileRepository.findByUserId(userId).isPresent()) {
            throw new BusinessLogicException("Người dùng này đã có hồ sơ bác sĩ");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setSpecialty(specialty);
        profile.setTitle(title);
        profile.setExperienceYears(expYears);
        profile.setConsultationFee(fee);
        profile.setBio(bio);
        return doctorProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public DoctorProfile update(Long id, Long specialtyId, String title, int expYears, BigDecimal fee, String bio) {
        DoctorProfile profile = findById(id);
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        profile.setSpecialty(specialty);
        profile.setTitle(title);
        profile.setExperienceYears(expYears);
        profile.setConsultationFee(fee);
        profile.setBio(bio);
        return doctorProfileRepository.save(profile);
    }
}
