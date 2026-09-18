package com.habittracker.exception;

public class HabitNotFoundException extends HabitTrackerException {
    private final int habitId;

    public HabitNotFoundException(int habitId) {
        super("Habit with ID " + habitId + " was not found.");
        this.habitId = habitId;
    }

    public HabitNotFoundException(String message) {
        super(message);
        this.habitId = -1;
    }

    public int getHabitId() {
        return habitId;
    }
}
