package com.medclinic.service;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.Role;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.Gender;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerPatient(UserRegisterDto registerDto) {
        com.medclinic.util.PasswordPolicy.requireValid(registerDto.getPassword());
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new BusinessLogicException("Mật khẩu xác nhận không khớp!");
        }
        String username = registerDto.getUsername().trim();
        String email = registerDto.getEmail().trim().toLowerCase();
        String phone = com.medclinic.util.PhoneNumbers.normalize(registerDto.getPhone(), true);

        User claimable = findClaimablePatient(username, email, phone);
        if (claimable != null) {
            User byUsername = userRepository.findByUsername(username).orElse(null);
            if (byUsername != null && !byUsername.getId().equals(claimable.getId())) {
                throw new BusinessLogicException("Tên đăng nhập '" + registerDto.getUsername() + "' đã tồn tại!");
            }
            User byEmail = userRepository.findByEmail(email).orElse(null);
            if (byEmail != null && !byEmail.getId().equals(claimable.getId())) {
                throw new BusinessLogicException("Email '" + registerDto.getEmail() + "' đã được đăng ký!");
            }
            User byPhone = userRepository.findByPhone(phone).orElse(null);
            if (byPhone != null && !byPhone.getId().equals(claimable.getId())) {
                throw new BusinessLogicException("Số điện thoại đã được đăng ký");
            }
            claimable.setUsername(username);
            claimable.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            claimable.setFullName(registerDto.getFullName().trim());
            claimable.setEmail(email);
            claimable.setPhone(phone);
            claimable.setGender(registerDto.getGender());
            claimable.setDateOfBirth(registerDto.getDateOfBirth());
            claimable.setAddress(normalizeOptional(registerDto.getAddress()));
            claimable.setEmailVerified(false);
            claimable.setOtpBypass(false);
            claimable.setIsActive(false);
            return saveWithUniquePhone(claimable);
        }

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new BusinessLogicException("Tên đăng nhập '" + registerDto.getUsername() + "' đã tồn tại!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new BusinessLogicException("Email '" + registerDto.getEmail() + "' đã được đăng ký!");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessLogicException("Số điện thoại đã được đăng ký");
        }

        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ROLE_PATIENT"));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .fullName(registerDto.getFullName().trim())
                .email(email)
                .phone(phone)
                .gender(registerDto.getGender())
                .dateOfBirth(registerDto.getDateOfBirth())
                .address(registerDto.getAddress())
                .role(patientRole)
                .emailVerified(false)
                .otpBypass(false)
                .isActive(false)
                .build();

        return saveWithUniquePhone(user);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<User> findUnverifiedRegistrationCandidate(String identifier, String rawPassword) {
        if (identifier == null || identifier.isBlank() || rawPassword == null) {
            return java.util.Optional.empty();
        }
        String normalized = identifier.trim();
        return userRepository.findByUsername(normalized)
                .or(() -> userRepository.findByEmail(normalized.toLowerCase()))
                .filter(this::isClaimableUnverifiedPatient)
                .filter(u -> u.getEmail() != null && !u.getEmail().endsWith("@invalid.medclinic.local"))
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()));
    }

    private User findClaimablePatient(String username, String email, String phone) {
        User byPhone = userRepository.findByPhone(phone).orElse(null);
        if (byPhone != null && isClaimableUnverifiedPatient(byPhone)) {
            return byPhone;
        }
        User byUsername = userRepository.findByUsername(username).orElse(null);
        if (byUsername != null && isClaimableUnverifiedPatient(byUsername)) {
            return byUsername;
        }
        User byEmail = userRepository.findByEmail(email).orElse(null);
        if (byEmail != null && isClaimableUnverifiedPatient(byEmail)) {
            return byEmail;
        }
        return null;
    }

    private boolean isClaimableUnverifiedPatient(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getName() == RoleType.ROLE_PATIENT
                && !Boolean.TRUE.equals(user.getIsActive())
                && (!Boolean.TRUE.equals(user.getEmailVerified())
                    || (user.getEmail() != null && user.getEmail().endsWith("@invalid.medclinic.local")));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(keyword.trim());
    }

    @Transactional
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public User updateUserProfile(Long userId, String fullName, String phone, Gender gender, LocalDate dateOfBirth, String address) {
        requireText(fullName, "Họ tên");
        User user = findById(userId);
        String normalizedPhone = com.medclinic.util.PhoneNumbers.normalize(phone, false);
        if (normalizedPhone != null) {
            User owner = userRepository.findByPhone(normalizedPhone).orElse(null);
            if (owner != null && !owner.getId().equals(userId)) {
                throw new BusinessLogicException("Số điện thoại đã được đăng ký");
            }
        }
        user.setFullName(fullName.trim());
        user.setPhone(normalizedPhone);
        user.setGender(gender);
        user.setDateOfBirth(dateOfBirth);
        user.setAddress(normalizeOptional(address));
        return saveWithUniquePhone(user);
    }

    @Transactional
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = findById(userId);
        user.setAvatarUrl(normalizeOptional(avatarUrl));
        return userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        com.medclinic.util.PasswordPolicy.requireValid(newPassword);
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessLogicException("Mật khẩu hiện tại không chính xác!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setSecurityVersion(user.getSecurityVersion() + 1);
        userRepository.save(user);
        userRepository.invalidateAuthChallenges(userId, java.time.LocalDateTime.now());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setIsActive(!user.getIsActive());
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            user.setSecurityVersion(user.getSecurityVersion() + 1);
        }
        userRepository.save(user);
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            userRepository.invalidateAuthChallenges(userId, java.time.LocalDateTime.now());
        }
    }

    private void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessLogicException(label + " không được để trống");
        }
    }

    private User saveWithUniquePhone(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new BusinessLogicException("Thông tin tài khoản đã được đăng ký. Vui lòng kiểm tra số điện thoại, email và tên đăng nhập.");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}

