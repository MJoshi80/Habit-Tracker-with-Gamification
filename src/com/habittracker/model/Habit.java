package com.habittracker.model;

import java.time.LocalDate;
import java.util.Objects;

public class Habit {
    private int id;
    private String name;
    private HabitCategory category;
    private Frequency frequency;
    private LocalDate createdOn;
    private boolean active;

    public Habit(int id, String name, HabitCategory category, Frequency frequency, LocalDate createdOn, boolean active) {
        this.id = id;
        this.name = name;
        this.category = category != null ? category : HabitCategory.OTHER;
        this.frequency = frequency != null ? frequency : Frequency.DAILY;
        this.createdOn = createdOn != null ? createdOn : LocalDate.now();
        this.active = active;
    }

    public Habit(int id, String name, HabitCategory category, Frequency frequency) {
        this(id, name, category, frequency, LocalDate.now(), true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HabitCategory getCategory() {
        return category;
    }

    public void setCategory(HabitCategory category) {
        this.category = category;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Habit habit = (Habit) o;
        return id == habit.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s, %s)%s",
                id, name, category.getDisplayName(), frequency.getDisplayName(),
                active ? "" : " [ARCHIVED]");
    }
}
