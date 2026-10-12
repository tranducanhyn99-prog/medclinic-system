package com.medclinic.service;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.entity.Role;
import com.medclinic.enums.RoleType;
import com.medclinic.entity.Specialty;
import com.medclinic.entity.User;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.repository.SpecialtyRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<DoctorProfile> findAll() {
        return doctorProfileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DoctorProfile findById(Long id) {
        return doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ"));
    }

    @Transactional(readOnly = true)
    public DoctorProfile findByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bác sĩ cho người dùng này"));
    }

    @Transactional(readOnly = true)
    public List<DoctorProfile> filterDoctors(Long specialtyId, BigDecimal maxFee, String keyword) {
        return findAll().stream()
                .filter(com.medclinic.util.DoctorAvailability::acceptsBookings)
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

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorProfile update(Long id, Long specialtyId, String title, int expYears, BigDecimal fee, String bio) {
        validateProfile(title, expYears, fee);
        DoctorProfile profile = findById(id);
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));

        profile.setSpecialty(specialty);
        profile.setTitle(title.trim());
        profile.setExperienceYears(expYears);
        profile.setConsultationFee(fee);
        profile.setBio(bio);
        return doctorProfileRepository.save(profile);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorProfile registerDoctor(AdminDoctorCreateDto doctorDto) {
        com.medclinic.util.PasswordPolicy.requireValid(doctorDto.getPassword());
        validateProfile(doctorDto.getTitle(), doctorDto.getExperienceYears(), doctorDto.getConsultationFee());
        String username = doctorDto.getUsername().trim();
        String email = doctorDto.getEmail().trim().toLowerCase();
        String phone = com.medclinic.util.PhoneNumbers.normalize(doctorDto.getPhone(), true);
        String license = doctorDto.getLicenseNumber().trim();
        if (userRepository.existsByUsername(username)) throw new BusinessLogicException("Tên đăng nhập đã tồn tại");
        if (userRepository.existsByEmail(email)) throw new BusinessLogicException("Email đã được đăng ký");
        if (userRepository.existsByPhone(phone)) throw new BusinessLogicException("Số điện thoại đã được đăng ký");
        if (doctorProfileRepository.findByLicenseNumber(license).isPresent()) throw new BusinessLogicException("Số giấy phép hành nghề đã tồn tại");
        Specialty specialty = specialtyRepository.findById(doctorDto.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
        Role role = roleRepository.findByName(RoleType.ROLE_DOCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ROLE_DOCTOR"));
        User user = User.builder().username(username).password(passwordEncoder.encode(doctorDto.getPassword()))
                .fullName(doctorDto.getFullName().trim()).email(email).phone(phone).role(role).isActive(true).build();
        try {
            userRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new BusinessLogicException("Thông tin tài khoản bác sĩ đã được đăng ký. Vui lòng kiểm tra số điện thoại, email và tên đăng nhập.");
        }
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user); profile.setSpecialty(specialty); profile.setLicenseNumber(license);
        profile.setTitle(doctorDto.getTitle().trim()); profile.setExperienceYears(doctorDto.getExperienceYears());
        profile.setConsultationFee(doctorDto.getConsultationFee()); profile.setBio(doctorDto.getBio());
        return doctorProfileRepository.save(profile);
    }

    private void validateProfile(String title, int expYears, BigDecimal fee) {
        if (title == null || title.trim().isEmpty()) throw new BusinessLogicException("Học vị bác sĩ không được để trống");
        if (expYears < 0) throw new BusinessLogicException("Số năm kinh nghiệm không hợp lệ");
        com.medclinic.util.VndMoney.requireValid(fee, "Giá khám");
    }
}
