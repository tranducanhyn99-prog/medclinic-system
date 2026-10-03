package com.medclinic.util;

public final class PrescriptionValidation {
    public static final int MAX_ITEMS = 100;
    public static final int DOSAGE_LENGTH = 100;
    public static final int FREQUENCY_LENGTH = 100;
    public static final int INSTRUCTIONS_LENGTH = 255;

    private PrescriptionValidation() { }

    public static boolean validRow(Long medicineId, Integer quantity, String dosage,
                                   String frequency, String instructions) {
        return medicineId != null && medicineId > 0 && quantity != null && quantity > 0
                && requiredText(dosage, DOSAGE_LENGTH) && requiredText(frequency, FREQUENCY_LENGTH)
                && (instructions == null || instructions.length() <= INSTRUCTIONS_LENGTH);
    }

    private static boolean requiredText(String value, int maxLength) {
        return value != null && !value.isBlank() && value.length() <= maxLength;
    }
}
