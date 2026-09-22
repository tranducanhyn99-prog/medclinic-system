package com.medclinic.service;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.Gender;

import java.time.LocalDate;

import java.util.List;

public interface UserService {
    User registerPatient(UserRegisterDto registerDto);
    User findByUsername(String username);
    User findById(Long id);
    User findByPhone(String phone);
    List<User> findByRole(RoleType roleType);
    List<User> searchUsers(String keyword);
    User updateUserProfile(Long userId, String fullName, String phone, Gender gender, LocalDate dateOfBirth, String address);
    User updateAvatar(Long userId, String avatarUrl);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void toggleUserStatus(Long userId);
}

