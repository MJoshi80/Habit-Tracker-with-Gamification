package com.habittracker.exception;

public class HabitTrackerException extends Exception {
    public HabitTrackerException(String message) {
        super(message);
    }

    public HabitTrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
