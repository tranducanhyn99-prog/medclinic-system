package com.medclinic.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(String rawToken, String newPassword, String confirmPassword);
}
