package com.habittracker.service;

import com.habittracker.exception.HabitNotFoundException;
import com.habittracker.exception.InvalidInputException;
import com.habittracker.exception.StorageException;
import com.habittracker.model.Frequency;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCategory;
import com.habittracker.repository.FileHabitRepository;
import com.habittracker.repository.FileLogRepository;
import com.habittracker.util.AppLogger;

import java.util.ArrayList;
import java.util.List;

public class HabitService {
    private final FileHabitRepository habitRepository;
    private final FileLogRepository logRepository;

    public HabitService(FileHabitRepository habitRepository, FileLogRepository logRepository) {
        this.habitRepository = habitRepository;
        this.logRepository = logRepository;
    }

    public Habit createHabit(String name, HabitCategory category, Frequency frequency)
            throws InvalidInputException, StorageException {
        validateName(name, 0);

        HabitCategory cat = category != null ? category : HabitCategory.OTHER;
        Frequency freq = frequency != null ? frequency : Frequency.DAILY;

        Habit habit = new Habit(0, name.trim(), cat, freq);
        Habit saved = habitRepository.save(habit);
        AppLogger.info("Created new habit: " + saved);
        return saved;
    }

    public Habit getHabitById(int id) throws HabitNotFoundException, StorageException {
        return habitRepository.findById(id)
                .orElseThrow(() -> new HabitNotFoundException(id));
    }

    public List<Habit> getActiveHabits() throws StorageException {
        List<Habit> all = habitRepository.findAll();
        List<Habit> active = new ArrayList<Habit>();
        for (Habit h : all) {
            if (h.isActive()) {
                active.add(h);
            }
        }
        return active;
    }

    public List<Habit> getAllHabits() throws StorageException {
        return habitRepository.findAll();
    }

    public Habit renameHabit(int id, String newName)
            throws HabitNotFoundException, InvalidInputException, StorageException {
        Habit habit = getHabitById(id);
        validateName(newName, id);

        habit.setName(newName.trim());
        habitRepository.save(habit);
        AppLogger.info("Renamed habit [" + id + "] to '" + newName.trim() + "'");
        return habit;
    }

    public Habit changeCategory(int id, HabitCategory category)
            throws HabitNotFoundException, StorageException {
        Habit habit = getHabitById(id);
        habit.setCategory(category != null ? category : HabitCategory.OTHER);
        habitRepository.save(habit);
        AppLogger.info("Updated category for habit [" + id + "] to " + habit.getCategory());
        return habit;
    }

    public Habit changeFrequency(int id, Frequency frequency)
            throws HabitNotFoundException, StorageException {
        Habit habit = getHabitById(id);
        habit.setFrequency(frequency != null ? frequency : Frequency.DAILY);
        habitRepository.save(habit);
        AppLogger.info("Updated frequency for habit [" + id + "] to " + habit.getFrequency());
        return habit;
    }

    public Habit archiveHabit(int id) throws HabitNotFoundException, StorageException {
        Habit habit = getHabitById(id);
        habit.setActive(false);
        habitRepository.save(habit);
        AppLogger.info("Archived habit [" + id + "]: " + habit.getName());
        return habit;
    }

    public boolean deleteHabitPermanently(int id) throws HabitNotFoundException, StorageException {
        getHabitById(id); // verify existence
        boolean deleted = habitRepository.deleteById(id);
        if (deleted && logRepository != null) {
            logRepository.deleteByHabitId(id);
        }
        AppLogger.info("Permanently deleted habit [" + id + "] and its logs");
        return deleted;
    }

    private void validateName(String name, int excludeId) throws InvalidInputException, StorageException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Habit name cannot be blank.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 60) {
            throw new InvalidInputException("Habit name exceeds maximum allowed length of 60 characters (was " + trimmed.length() + ").");
        }

        List<Habit> activeHabits = getActiveHabits();
        for (Habit h : activeHabits) {
            if (h.getId() != excludeId && h.getName().equalsIgnoreCase(trimmed)) {
                throw new InvalidInputException("An active habit named '" + trimmed + "' already exists.");
            }
        }
    }
}
