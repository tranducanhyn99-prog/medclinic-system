package com.medclinic.enums;

public enum AppointmentStatus {
    CONFIRMED("Đã xác nhận", "badge bg-primary"),
    CHECKED_IN("Đã tiếp nhận (Chờ khám)", "badge bg-info text-dark"),
    IN_PROGRESS("Đang khám", "badge bg-secondary"),
    COMPLETED("Đã hoàn tất", "badge bg-success"),
    CANCELLED("Đã hủy", "badge bg-danger"),
    NO_SHOW("Vắng mặt", "badge bg-dark");

    private final String displayName;
    private final String badgeClass;

    AppointmentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}

