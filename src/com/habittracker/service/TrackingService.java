package com.habittracker.service;

import com.habittracker.exception.HabitNotFoundException;
import com.habittracker.exception.InvalidInputException;
import com.habittracker.exception.StorageException;
import com.habittracker.model.CompletionLog;
import com.habittracker.model.Frequency;
import com.habittracker.model.Habit;
import com.habittracker.repository.FileHabitRepository;
import com.habittracker.repository.FileLogRepository;
import com.habittracker.util.AppLogger;
import com.habittracker.util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class TrackingService {
    private final FileHabitRepository habitRepository;
    private final FileLogRepository logRepository;
    private GamificationService gamificationService;

    public TrackingService(FileHabitRepository habitRepository, FileLogRepository logRepository) {
        this.habitRepository = habitRepository;
        this.logRepository = logRepository;
    }

    public void setGamificationService(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    public CompletionLog markCompleted(int habitId, LocalDate date)
            throws HabitNotFoundException, InvalidInputException, StorageException {
        return logHabit(habitId, date, true);
    }

    public CompletionLog markSkipped(int habitId, LocalDate date)
            throws HabitNotFoundException, InvalidInputException, StorageException {
        return logHabit(habitId, date, false);
    }

    private synchronized CompletionLog logHabit(int habitId, LocalDate inputDate, boolean completed)
            throws HabitNotFoundException, InvalidInputException, StorageException {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        LocalDate date = inputDate != null ? inputDate : LocalDate.now();

        if (DateUtil.isFutureDate(date)) {
            throw new InvalidInputException("Cannot log habits for a future date (" + DateUtil.formatDate(date) + ").");
        }

        Optional<CompletionLog> existing = logRepository.findByHabitAndDate(habitId, date);
        if (existing.isPresent()) {
            throw new InvalidInputException("Habit [" + habit.getName() + "] has already been logged for " + DateUtil.formatDate(date) + ".");
        }

        int xp = 0;
        if (completed && gamificationService != null) {
            // Temporary streak with this completion
            int streak = calculateStreakEndingAt(habit, date, true);
            xp = gamificationService.calculateXp(streak);
        }

        CompletionLog log = new CompletionLog(habitId, date, completed, xp);
        logRepository.appendLog(log);

        if (completed && gamificationService != null) {
            int currentStreak = getCurrentStreak(habitId);
            gamificationService.registerCompletion(habit, log, currentStreak);
        }

        AppLogger.info(String.format("Logged habit [%d] '%s' on %s (completed=%b, xp=%d)",
                habitId, habit.getName(), DateUtil.formatDate(date), completed, xp));

        return log;
    }

    public int getCurrentStreak(int habitId) throws HabitNotFoundException, StorageException {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));
        return calculateStreakEndingAt(habit, null, false);
    }

    private int calculateStreakEndingAt(Habit habit, LocalDate additionalDate, boolean additionalCompleted)
            throws StorageException {
        List<CompletionLog> logs = logRepository.findByHabitId(habit.getId());
        Frequency frequency = habit.getFrequency();

        TreeSet<String> completedUnits = new TreeSet<String>();
        for (CompletionLog l : logs) {
            if (l.isCompleted()) {
                completedUnits.add(DateUtil.toUnitKey(l.getDate(), frequency));
            }
        }
        if (additionalDate != null && additionalCompleted) {
            completedUnits.add(DateUtil.toUnitKey(additionalDate, frequency));
        }

        if (completedUnits.isEmpty()) {
            return 0;
        }

        // The most recent completed unit
        String currentUnit = completedUnits.last();
        int streak = 0;

        while (currentUnit != null && !currentUnit.isEmpty() && completedUnits.contains(currentUnit)) {
            streak++;
            currentUnit = DateUtil.previousUnitKey(currentUnit, frequency);
        }

        return streak;
    }

    public int getLongestStreak(int habitId) throws HabitNotFoundException, StorageException {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));
        List<CompletionLog> logs = logRepository.findByHabitId(habit.getId());
        Frequency frequency = habit.getFrequency();

        TreeSet<String> completedUnits = new TreeSet<String>();
        for (CompletionLog l : logs) {
            if (l.isCompleted()) {
                completedUnits.add(DateUtil.toUnitKey(l.getDate(), frequency));
            }
        }

        if (completedUnits.isEmpty()) {
            return 0;
        }

        int maxStreak = 0;
        for (String unit : completedUnits) {
            int streak = 0;
            String cur = unit;
            while (cur != null && !cur.isEmpty() && completedUnits.contains(cur)) {
                streak++;
                cur = DateUtil.previousUnitKey(cur, frequency);
            }
            if (streak > maxStreak) {
                maxStreak = streak;
            }
        }

        return maxStreak;
    }

    public double getCompletionRate(int habitId) throws HabitNotFoundException, StorageException {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));
        List<CompletionLog> logs = logRepository.findByHabitId(habit.getId());
        if (logs.isEmpty()) {
            return 0.0;
        }

        int completedCount = 0;
        for (CompletionLog log : logs) {
            if (log.isCompleted()) {
                completedCount++;
            }
        }

        return ((double) completedCount / logs.size()) * 100.0;
    }

    public List<String> getRecentDaysStatus(int habitId, int days) throws StorageException {
        List<String> statuses = new ArrayList<String>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            Optional<CompletionLog> log = logRepository.findByHabitAndDate(habitId, targetDate);
            if (log.isPresent()) {
                statuses.add(log.get().isCompleted() ? "DONE" : "MISSED");
            } else {
                statuses.add("NONE");
            }
        }

        return statuses;
    }

    public List<CompletionLog> getLogsForHabit(int habitId) throws StorageException {
        return logRepository.findByHabitId(habitId);
    }
}
