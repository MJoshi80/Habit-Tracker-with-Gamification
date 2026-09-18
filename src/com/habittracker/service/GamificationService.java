package com.habittracker.service;

import com.habittracker.exception.StorageException;
import com.habittracker.model.BadgeType;
import com.habittracker.model.CompletionLog;
import com.habittracker.model.Habit;
import com.habittracker.model.UserProfile;
import com.habittracker.repository.FileLogRepository;
import com.habittracker.repository.FileProfileRepository;
import com.habittracker.util.AppLogger;

import java.util.ArrayList;
import java.util.List;

public class GamificationService {
    public static final int BASE_XP = 10;
    public static final int MAX_BONUS = 40;
    public static final int BONUS_PER_DAY = 2;

    private final FileProfileRepository profileRepository;
    private final FileLogRepository logRepository;

    public GamificationService(FileProfileRepository profileRepository, FileLogRepository logRepository) {
        this.profileRepository = profileRepository;
        this.logRepository = logRepository;
    }

    public int calculateXp(int streak) {
        if (streak <= 0) {
            streak = 1;
        }
        int bonus = Math.min(MAX_BONUS, (streak - 1) * BONUS_PER_DAY);
        return BASE_XP + bonus;
    }

    public synchronized List<BadgeType> registerCompletion(Habit habit, CompletionLog log, int streak) throws StorageException {
        UserProfile profile = profileRepository.getProfile();
        profile.addXp(log.getXpEarned());

        List<BadgeType> newlyUnlocked = evaluateBadges(profile, habit, streak);
        for (BadgeType b : newlyUnlocked) {
            profile.unlockBadge(b);
            AppLogger.info("Unlocked badge: " + b.getTitle() + " for user " + profile.getUserName());
        }

        profileRepository.save(profile);
        return newlyUnlocked;
    }

    public synchronized List<BadgeType> evaluateBadges(UserProfile profile, Habit habit, int streak) throws StorageException {
        List<BadgeType> newlyUnlocked = new ArrayList<BadgeType>();
        List<CompletionLog> allLogs = logRepository.findAll();

        // 1. FIRST_STEP: first completion
        if (!profile.hasBadge(BadgeType.FIRST_STEP)) {
            boolean hasAny = false;
            for (CompletionLog l : allLogs) {
                if (l.isCompleted()) {
                    hasAny = true;
                    break;
                }
            }
            if (hasAny) {
                newlyUnlocked.add(BadgeType.FIRST_STEP);
            }
        }

        // 2. 7-Day Warrior
        if (!profile.hasBadge(BadgeType.SEVEN_DAY_WARRIOR) && streak >= 7) {
            newlyUnlocked.add(BadgeType.SEVEN_DAY_WARRIOR);
        }

        // 3. 30-Day Champion
        if (!profile.hasBadge(BadgeType.THIRTY_DAY_CHAMPION) && streak >= 30) {
            newlyUnlocked.add(BadgeType.THIRTY_DAY_CHAMPION);
        }

        // 4. 100-Day Legend
        if (!profile.hasBadge(BadgeType.HUNDRED_DAY_LEGEND) && streak >= 100) {
            newlyUnlocked.add(BadgeType.HUNDRED_DAY_LEGEND);
        }

        // 5. Comeback Kid: resumed logging after a broken streak / skipped day
        if (!profile.hasBadge(BadgeType.COMEBACK_KID) && habit != null) {
            List<CompletionLog> habitLogs = logRepository.findByHabitId(habit.getId());
            boolean hadSkippedOrBreak = false;
            for (CompletionLog l : habitLogs) {
                if (!l.isCompleted()) {
                    hadSkippedOrBreak = true;
                    break;
                }
            }
            if (hadSkippedOrBreak && streak >= 1) {
                newlyUnlocked.add(BadgeType.COMEBACK_KID);
            }
        }

        // 6. Consistency Master: >= 80% completion rate with at least 10 logs
        if (!profile.hasBadge(BadgeType.CONSISTENCY_MASTER)) {
            if (allLogs.size() >= 10) {
                int completed = 0;
                for (CompletionLog l : allLogs) {
                    if (l.isCompleted()) completed++;
                }
                double rate = ((double) completed / allLogs.size()) * 100.0;
                if (rate >= 80.0) {
                    newlyUnlocked.add(BadgeType.CONSISTENCY_MASTER);
                }
            }
        }

        // 7. Rising Star (Level 5)
        if (!profile.hasBadge(BadgeType.RISING_STAR) && profile.getLevel() >= 5) {
            newlyUnlocked.add(BadgeType.RISING_STAR);
        }

        // 8. Habit Hero (Level 10)
        if (!profile.hasBadge(BadgeType.HABIT_HERO) && profile.getLevel() >= 10) {
            newlyUnlocked.add(BadgeType.HABIT_HERO);
        }

        return newlyUnlocked;
    }

    public UserProfile getProfile() throws StorageException {
        return profileRepository.getProfile();
    }
}
