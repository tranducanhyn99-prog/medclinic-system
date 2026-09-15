package com.medclinic.enums;

public enum SlotStatus {
    AVAILABLE("Còn trống"),
    BOOKED("Đã đặt"),
    BLOCKED("Tạm khóa");

    private final String displayName;

    SlotStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

