package com.medclinic.enums;

public enum AuthChallengeType {
    REGISTRATION("đăng ký tài khoản"),
    LOGIN("đăng nhập");

    private final String displayName;

    AuthChallengeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
