package com.medclinic.enums;

public enum ShiftType {
    MORNING("Ca Sáng (08:00 - 12:00)"),
    AFTERNOON("Ca Chiều (13:30 - 17:30)");

    private final String displayName;

    ShiftType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

