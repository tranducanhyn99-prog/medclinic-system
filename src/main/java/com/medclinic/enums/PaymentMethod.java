package com.medclinic.enums;

public enum PaymentMethod {
    CASH("Tiền mặt tại quầy"),
    VNPAY("Cổng VNPAY Sandbox");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

