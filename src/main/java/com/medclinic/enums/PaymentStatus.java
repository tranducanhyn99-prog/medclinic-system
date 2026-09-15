package com.medclinic.enums;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán", "badge bg-warning text-dark"),
    PAID("Đã thanh toán", "badge bg-success"),
    REFUNDED("Đã hoàn tiền", "badge bg-secondary");

    private final String displayName;
    private final String badgeClass;

    PaymentStatus(String displayName, String badgeClass) {
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

