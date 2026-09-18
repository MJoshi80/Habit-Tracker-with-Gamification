package com.habittracker.service;

import com.habittracker.exception.HabitNotFoundException;
import com.habittracker.exception.StorageException;
import com.habittracker.model.BadgeType;
import com.habittracker.model.CompletionLog;
import com.habittracker.model.Habit;
import com.habittracker.model.UserProfile;
import com.habittracker.util.AsciiChartUtil;
import com.habittracker.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ReportService {
    private final HabitService habitService;
    private final TrackingService trackingService;
    private final GamificationService gamificationService;

    public ReportService(HabitService habitService, TrackingService trackingService, GamificationService gamificationService) {
        this.habitService = habitService;
        this.trackingService = trackingService;
        this.gamificationService = gamificationService;
    }

    public String generateDashboard() throws StorageException {
        UserProfile profile = gamificationService.getProfile();
        List<Habit> activeHabits = habitService.getActiveHabits();

        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================================================\n");
        sb.append("                                   HABIT TRACKER DASHBOARD                              \n");
        sb.append("========================================================================================\n");
        sb.append(String.format(" User: %-15s | Level %-2d | Total XP: %-5d XP\n",
                profile.getUserName(), profile.getLevel(), profile.getTotalXp()));
        sb.append(String.format(" Level Progress: %s (%d/100 XP to Level %d)\n",
                AsciiChartUtil.renderProgressBar(profile.getXpIntoCurrentLevel(), 100, 25),
                profile.getXpIntoCurrentLevel(), profile.getLevel() + 1));
        sb.append(String.format(" Badges Unlocked: %d of %d\n",
                profile.getBadges().size(), BadgeType.values().length));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (activeHabits.isEmpty()) {
            sb.append(" No active habits yet! Choose 'Add Habit' from the menu to get started.\n");
        } else {
            sb.append(String.format(" %-4s %-20s %-12s %-14s %-18s %-28s\n",
                    "ID", "Habit Name", "Category", "Streak", "Consistency", "Last 7 Days"));
            sb.append("----------------------------------------------------------------------------------------\n");

            for (Habit h : activeHabits) {
                try {
                    int currentStreak = trackingService.getCurrentStreak(h.getId());
                    int longestStreak = trackingService.getLongestStreak(h.getId());
                    double rate = trackingService.getCompletionRate(h.getId());
                    List<String> last7 = trackingService.getRecentDaysStatus(h.getId(), 7);

                    String streakDisplay = AsciiChartUtil.renderStreakStars(currentStreak);
                    String rateBar = AsciiChartUtil.renderProgressBar((int) Math.round(rate), 100, 8);
                    String strip = AsciiChartUtil.renderWeekStrip(last7);

                    sb.append(String.format(" %-4d %-20s %-12s %-14s %-18s %-28s\n",
                            h.getId(),
                            truncate(h.getName(), 20),
                            truncate(h.getCategory().getDisplayName(), 12),
                            truncate(streakDisplay, 14),
                            rateBar,
                            strip));
                } catch (HabitNotFoundException ignored) {
                }
            }
        }
        sb.append("========================================================================================\n");
        sb.append(" Legend: [V] Completed   [X] Missed/Skipped   [-] No Entry\n");
        return sb.toString();
    }

    public String generateBadgeCabinet() throws StorageException {
        UserProfile profile = gamificationService.getProfile();
        Set<BadgeType> unlocked = profile.getBadges();

        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================================================\n");
        sb.append(String.format("                        BADGE CABINET (%d / %d Unlocked)                        \n",
                unlocked.size(), BadgeType.values().length));
        sb.append("========================================================================================\n");

        for (BadgeType badge : BadgeType.values()) {
            boolean isUnlocked = unlocked.contains(badge);
            String status = isUnlocked ? "[* UNLOCKED]" : "[  LOCKED  ]";
            sb.append(String.format(" %-15s %-22s - %s\n", status, badge.getTitle(), badge.getDescription()));
        }
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public String generateHabitDetails(int habitId) throws HabitNotFoundException, StorageException {
        Habit habit = habitService.getHabitById(habitId);
        int currentStreak = trackingService.getCurrentStreak(habitId);
        int longestStreak = trackingService.getLongestStreak(habitId);
        double rate = trackingService.getCompletionRate(habitId);
        List<CompletionLog> logs = trackingService.getLogsForHabit(habitId);

        StringBuilder sb = new StringBuilder();
        sb.append("\n----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Habit Details: [%d] %s\n", habit.getId(), habit.getName()));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Category      : %s\n", habit.getCategory().getDisplayName()));
        sb.append(String.format(" Frequency     : %s\n", habit.getFrequency().getDisplayName()));
        sb.append(String.format(" Status        : %s\n", habit.isActive() ? "Active" : "Archived"));
        sb.append(String.format(" Created On    : %s\n", DateUtil.formatDate(habit.getCreatedOn())));
        sb.append(String.format(" Current Streak: %d periods (%s)\n", currentStreak, AsciiChartUtil.renderStreakStars(currentStreak)));
        sb.append(String.format(" Longest Streak: %d periods\n", longestStreak));
        sb.append(String.format(" Completion    : %.1f%% (%d logs total)\n", rate, logs.size()));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(" Log History (most recent last):\n");
        if (logs.isEmpty()) {
            sb.append("   (No logs recorded yet)\n");
        } else {
            for (CompletionLog l : logs) {
                sb.append(String.format("   %s : %s | XP Earned: +%d\n",
                        DateUtil.formatDate(l.getDate()),
                        l.isCompleted() ? "[V] Completed" : "[X] Skipped",
                        l.getXpEarned()));
            }
        }
        sb.append("----------------------------------------------------------------------------------------\n");
        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 1) + "...";
    }
}
