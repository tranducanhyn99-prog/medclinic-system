package com.medclinic.util;

import com.medclinic.exception.BusinessLogicException;

public final class PhoneNumbers {
    public static final String PATTERN = "^0[35789][0-9]{8}$";
    public static final String MESSAGE = "Số điện thoại không hợp lệ (10 số, bắt đầu bằng 03, 05, 07, 08, 09)";

    private PhoneNumbers() {}

    public static String normalize(String phone, boolean required) {
        String normalized = phone == null || phone.isBlank() ? null : phone.trim();
        if ((required && normalized == null) || (normalized != null && !normalized.matches(PATTERN))) {
            throw new BusinessLogicException(MESSAGE);
        }
        return normalized;
    }
}
