package com.habittracker.test;

import com.habittracker.exception.HabitNotFoundException;
import com.habittracker.exception.InvalidInputException;
import com.habittracker.exception.StorageException;
import com.habittracker.model.CompletionLog;
import com.habittracker.model.Frequency;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCategory;
import com.habittracker.repository.FileHabitRepository;
import com.habittracker.repository.FileLogRepository;
import com.habittracker.repository.FileProfileRepository;
import com.habittracker.service.GamificationService;
import com.habittracker.service.HabitService;
import com.habittracker.service.TrackingService;

import java.io.File;
import java.time.LocalDate;

public class TrackingServiceTest {

    private static class Fixture {
        HabitService habitService;
        TrackingService trackingService;
        GamificationService gamificationService;
        Habit habit;
    }

    private Fixture createFixture(String testId) throws Exception {
        new File("data/test").mkdirs();
        String hFile = "data/test/track_habits_" + testId + ".csv";
        String lFile = "data/test/track_logs_" + testId + ".csv";
        String pFile = "data/test/track_prof_" + testId + ".csv";
        new File(hFile).delete();
        new File(lFile).delete();
        new File(pFile).delete();

        FileHabitRepository hRepo = new FileHabitRepository(hFile);
        FileLogRepository lRepo = new FileLogRepository(lFile);
        FileProfileRepository pRepo = new FileProfileRepository(pFile);

        Fixture f = new Fixture();
        f.habitService = new HabitService(hRepo, lRepo);
        f.trackingService = new TrackingService(hRepo, lRepo);
        f.gamificationService = new GamificationService(pRepo, lRepo);
        f.trackingService.setGamificationService(f.gamificationService);

        f.habit = f.habitService.createHabit("Daily Reading", HabitCategory.READING, Frequency.DAILY);
        return f;
    }

    public void testStreakBuildingAcrossConsecutiveDays() throws Exception {
        Fixture f = createFixture("consecutive");
        LocalDate base = LocalDate.of(2026, 1, 1);

        f.trackingService.markCompleted(f.habit.getId(), base);
        f.trackingService.markCompleted(f.habit.getId(), base.plusDays(1));
        f.trackingService.markCompleted(f.habit.getId(), base.plusDays(2));

        int streak = f.trackingService.getCurrentStreak(f.habit.getId());
        TestRunner.assertEquals("Streak after 3 consecutive days should be 3", 3, streak);

        int longest = f.trackingService.getLongestStreak(f.habit.getId());
        TestRunner.assertEquals("Longest streak should be 3", 3, longest);
    }

    public void testStreakResetAfterGap() throws Exception {
        Fixture f = createFixture("gap");
        LocalDate base = LocalDate.of(2026, 1, 1);

        // Day 1 & Day 2 completed
        f.trackingService.markCompleted(f.habit.getId(), base);
        f.trackingService.markCompleted(f.habit.getId(), base.plusDays(1));

        // Day 3 was missed/skipped
        f.trackingService.markSkipped(f.habit.getId(), base.plusDays(2));

        // Day 4 completed (streak should reset to 1)
        f.trackingService.markCompleted(f.habit.getId(), base.plusDays(3));

        int currentStreak = f.trackingService.getCurrentStreak(f.habit.getId());
        TestRunner.assertEquals("Streak should reset to 1 after a gap", 1, currentStreak);

        int longestStreak = f.trackingService.getLongestStreak(f.habit.getId());
        TestRunner.assertEquals("Longest streak should retain peak value of 2", 2, longestStreak);
    }

    public void testCompletionRateCalculation() throws Exception {
        Fixture f = createFixture("rate");
        LocalDate base = LocalDate.of(2026, 2, 1);

        // 3 completed, 1 skipped -> 75%
        f.trackingService.markCompleted(f.habit.getId(), base);
        f.trackingService.markCompleted(f.habit.getId(), base.plusDays(1));
        f.trackingService.markCompleted(f.habit.getId(), base.plusDays(2));
        f.trackingService.markSkipped(f.habit.getId(), base.plusDays(3));

        double rate = f.trackingService.getCompletionRate(f.habit.getId());
        TestRunner.assertEquals("Completion rate should be 75.0%", 75.0, rate, 0.01);
    }

    public void testXpGrowthWithStreakLength() throws Exception {
        Fixture f = createFixture("xp");

        // Formula: base (10) + min(40, (streak - 1) * 2)
        int xpDay1 = f.gamificationService.calculateXp(1);
        int xpDay2 = f.gamificationService.calculateXp(2);
        int xpDay5 = f.gamificationService.calculateXp(5);
        int xpDay30 = f.gamificationService.calculateXp(30);

        TestRunner.assertEquals("XP for day 1 streak", 10, xpDay1);
        TestRunner.assertEquals("XP for day 2 streak", 12, xpDay2);
        TestRunner.assertEquals("XP for day 5 streak", 18, xpDay5);
        TestRunner.assertEquals("XP for day 30 streak should be capped at 50", 50, xpDay30);
    }

    public void testDuplicateLogRejection() throws Exception {
        Fixture f = createFixture("dup_log");
        LocalDate date = LocalDate.of(2026, 3, 1);

        f.trackingService.markCompleted(f.habit.getId(), date);

        boolean threw = false;
        try {
            f.trackingService.markCompleted(f.habit.getId(), date);
        } catch (InvalidInputException e) {
            threw = true;
        }
        TestRunner.assertTrue("Logging the same habit twice on the same date must throw InvalidInputException", threw);
    }
}
