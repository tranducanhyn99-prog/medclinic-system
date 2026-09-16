package com.medclinic.service.impl;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.Role;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerPatient(UserRegisterDto registerDto) {
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new BusinessLogicException("Mật khẩu xác nhận không khớp!");
        }
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new BusinessLogicException("Tên đăng nhập '" + registerDto.getUsername() + "' đã tồn tại!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new BusinessLogicException("Email '" + registerDto.getEmail() + "' đã được đăng ký!");
        }

        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ROLE_PATIENT"));

        User user = User.builder()
                .username(registerDto.getUsername().trim())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .fullName(registerDto.getFullName().trim())
                .email(registerDto.getEmail().trim().toLowerCase())
                .phone(registerDto.getPhone() != null ? registerDto.getPhone().trim() : null)
                .gender(registerDto.getGender())
                .dateOfBirth(registerDto.getDateOfBirth())
                .address(registerDto.getAddress())
                .role(patientRole)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(RoleType roleType) {
        return userRepository.findByRole_Name(roleType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchUsers(keyword.trim(), pageable);
    }

    @Override
    @Transactional
    public User updateUserProfile(Long userId, String fullName, String phone, String address) {
        User user = findById(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessLogicException("Mật khẩu hiện tại không chính xác!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }
}

