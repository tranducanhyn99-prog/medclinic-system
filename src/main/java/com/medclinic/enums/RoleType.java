package com.medclinic.enums;

public enum RoleType {
    ROLE_PATIENT("Bệnh nhân"),
    ROLE_DOCTOR("Bác sĩ"),
    ROLE_STAFF("Lễ tân / Tiếp nhận"),
    ROLE_ADMIN("Quản trị viên");

    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

