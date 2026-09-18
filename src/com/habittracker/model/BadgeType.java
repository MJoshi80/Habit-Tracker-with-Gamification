package com.habittracker.model;

public enum BadgeType {
    FIRST_STEP("First Step", "Logged your very first habit completion."),
    SEVEN_DAY_WARRIOR("7-Day Warrior", "Maintained an active streak of at least 7 periods."),
    THIRTY_DAY_CHAMPION("30-Day Champion", "Achieved an unbroken streak of 30 periods."),
    HUNDRED_DAY_LEGEND("100-Day Legend", "Reached an epic 100-period consistency streak."),
    COMEBACK_KID("Comeback Kid", "Resumed logging and rebuilt momentum after a broken streak."),
    CONSISTENCY_MASTER("Consistency Master", "Achieved an overall completion rate of 80% or higher (min 10 logs)."),
    RISING_STAR("Rising Star", "Reached Profile Level 5 (500+ XP)."),
    HABIT_HERO("Habit Hero", "Reached Profile Level 10 (1000+ XP).");

    private final String title;
    private final String description;

    BadgeType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public static BadgeType fromString(String name) {
        if (name == null) return null;
        for (BadgeType b : BadgeType.values()) {
            if (b.name().equalsIgnoreCase(name.trim()) || b.title.equalsIgnoreCase(name.trim())) {
                return b;
            }
        }
        return null;
    }
}
