package com.medclinic.util;

import com.medclinic.exception.BusinessLogicException;
import java.math.BigDecimal;

public final class VndMoney {
    public static final BigDecimal MAX = new BigDecimal("9999999999");
    public static final String MESSAGE = "Số tiền phải là VND nguyên, từ 0 đến 9.999.999.999";

    private VndMoney() {}

    public static boolean isValid(BigDecimal amount) {
        return amount != null && amount.signum() >= 0 && amount.compareTo(MAX) <= 0
                && amount.stripTrailingZeros().scale() <= 0;
    }

    public static void requireValid(BigDecimal amount, String label) {
        if (!isValid(amount)) throw new BusinessLogicException(label + ": " + MESSAGE);
    }
}
