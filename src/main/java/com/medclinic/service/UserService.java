package com.medclinic.service;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User registerPatient(UserRegisterDto registerDto);
    User findByUsername(String username);
    User findById(Long id);
    User findByPhone(String phone);
    List<User> findByRole(RoleType roleType);
    List<User> searchUsers(String keyword);
    Page<User> searchUsers(String keyword, Pageable pageable);
    User updateUserProfile(Long userId, String fullName, String phone, String address);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void toggleUserStatus(Long userId);
}

