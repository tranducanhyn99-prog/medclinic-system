package com.medclinic.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Presentation only: never rounds or changes the stored amount. */
public final class InvoiceMoneyFormat {
    private static final String[] DIGITS = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] GROUPS = {"", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"};

    private InvoiceMoneyFormat() { }

    public static String number(BigDecimal amount) {
        if (amount == null) return "-";
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        int decimals = Math.max(0, amount.stripTrailingZeros().scale());
        format.setMinimumFractionDigits(decimals);
        format.setMaximumFractionDigits(decimals);
        return format.format(amount);
    }

    public static String words(BigDecimal amount) {
        if (amount == null || amount.stripTrailingZeros().scale() > 0) return null;
        BigInteger remaining = amount.abs().toBigIntegerExact();
        if (remaining.signum() == 0) return "Không đồng";
        List<Integer> groups = new ArrayList<>();
        while (remaining.signum() > 0) {
            BigInteger[] part = remaining.divideAndRemainder(BigInteger.valueOf(1000));
            groups.add(part[1].intValueExact());
            remaining = part[0];
        }
        if (groups.size() > GROUPS.length) return null;
        List<String> result = new ArrayList<>();
        if (amount.signum() < 0) result.add("âm");
        for (int i = groups.size() - 1; i >= 0; i--) {
            int value = groups.get(i);
            if (value == 0) continue;
            result.add(triple(value, i < groups.size() - 1));
            if (!GROUPS[i].isEmpty()) result.add(GROUPS[i]);
        }
        String text = String.join(" ", result) + " đồng";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static String triple(int value, boolean padHundreds) {
        List<String> result = new ArrayList<>();
        int hundred = value / 100, ten = (value % 100) / 10, one = value % 10;
        if (hundred > 0 || padHundreds) {
            result.add(DIGITS[hundred]);
            result.add("trăm");
        }
        if (ten > 1) {
            result.add(DIGITS[ten]);
            result.add("mươi");
        } else if (ten == 1) {
            result.add("mười");
        } else if (one > 0 && !result.isEmpty()) {
            result.add("lẻ");
        }
        if (one > 0) result.add(one == 1 && ten > 1 ? "mốt" : one == 5 && ten > 0 ? "lăm" : DIGITS[one]);
        return String.join(" ", result);
    }
}
