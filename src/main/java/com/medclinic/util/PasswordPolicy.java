package com.medclinic.util;

import com.medclinic.exception.BusinessLogicException;
import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {
    public static final String MESSAGE = "Mật khẩu cần ít nhất 8 ký tự và tối đa 72 byte UTF-8; ký tự có dấu hoặc emoji có thể chiếm nhiều byte.";
    private PasswordPolicy() { }

    public static boolean fitsBcrypt(CharSequence password) {
        return password != null && password.toString().getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static boolean valid(String password) {
        return password != null && !password.isBlank() && password.length() >= 8 && fitsBcrypt(password);
    }

    public static void requireValid(String password) {
        if (!valid(password)) throw new BusinessLogicException(MESSAGE);
    }
}
