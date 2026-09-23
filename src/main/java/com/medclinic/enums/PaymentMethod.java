package com.medclinic.enums;

public enum PaymentMethod {
    CASH("Tiền mặt tại quầy"),
    BANK_TRANSFER("Chuyển khoản ngân hàng qua VietQR"),
    VNPAY("VNPAY (legacy/không sử dụng)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
