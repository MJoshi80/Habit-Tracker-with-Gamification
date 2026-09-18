package com.habittracker.model;

import java.time.LocalDate;
import java.util.Objects;

public class CompletionLog {
    private int habitId;
    private LocalDate date;
    private boolean completed;
    private int xpEarned;

    public CompletionLog(int habitId, LocalDate date, boolean completed, int xpEarned) {
        this.habitId = habitId;
        this.date = date != null ? date : LocalDate.now();
        this.completed = completed;
        this.xpEarned = xpEarned;
    }

    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletionLog that = (CompletionLog) o;
        return habitId == that.habitId && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(habitId, date);
    }

    @Override
    public String toString() {
        return String.format("Log(habit=%d, date=%s, completed=%b, xp=%d)",
                habitId, date, completed, xpEarned);
    }
}
