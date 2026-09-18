package com.habittracker.model;

public enum HabitCategory {
    STUDY("Study"),
    FITNESS("Fitness"),
    HEALTH("Health"),
    CODING("Coding"),
    READING("Reading"),
    MEDITATION("Meditation"),
    HYDRATION("Hydration"),
    OTHER("Other");

    private final String displayName;

    HabitCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static HabitCategory fromString(String text) {
        if (text == null) return OTHER;
        for (HabitCategory c : HabitCategory.values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }
}
