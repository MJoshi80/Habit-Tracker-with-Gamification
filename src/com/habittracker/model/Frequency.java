package com.habittracker.model;

public enum Frequency {
    DAILY("Daily"),
    WEEKLY("Weekly");

    private final String displayName;

    Frequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Frequency fromString(String text) {
        if (text == null) return DAILY;
        for (Frequency f : Frequency.values()) {
            if (f.name().equalsIgnoreCase(text.trim()) || f.displayName.equalsIgnoreCase(text.trim())) {
                return f;
            }
        }
        return DAILY;
    }
}
