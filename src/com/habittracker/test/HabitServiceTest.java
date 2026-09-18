package com.habittracker.test;

import com.habittracker.exception.HabitNotFoundException;
import com.habittracker.exception.InvalidInputException;
import com.habittracker.exception.StorageException;
import com.habittracker.model.Frequency;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCategory;
import com.habittracker.repository.FileHabitRepository;
import com.habittracker.repository.FileLogRepository;
import com.habittracker.service.HabitService;

import java.io.File;
import java.util.List;

public class HabitServiceTest {

    private HabitService createTestService(String testId) {
        new File("data/test").mkdirs();
        String habitFile = "data/test/habits_" + testId + ".csv";
        String logFile = "data/test/logs_" + testId + ".csv";
        new File(habitFile).delete();
        new File(logFile).delete();

        FileHabitRepository hRepo = new FileHabitRepository(habitFile);
        FileLogRepository lRepo = new FileLogRepository(logFile);
        return new HabitService(hRepo, lRepo);
    }

    public void testSuccessfulCreation() throws InvalidInputException, StorageException, HabitNotFoundException {
        HabitService service = createTestService("create");
        Habit habit = service.createHabit("Daily Meditation", HabitCategory.MEDITATION, Frequency.DAILY);

        TestRunner.assertNotNull("Created habit should not be null", habit);
        TestRunner.assertTrue("Habit should have positive ID", habit.getId() > 0);
        TestRunner.assertEquals("Habit name should match", "Daily Meditation", habit.getName());
        TestRunner.assertTrue("Habit should be active by default", habit.isActive());

        Habit fetched = service.getHabitById(habit.getId());
        TestRunner.assertEquals("Fetched habit should match created", habit.getId(), fetched.getId());
    }

    public void testBlankNameRejection() throws StorageException {
        HabitService service = createTestService("blank");
        boolean threw = false;
        try {
            service.createHabit("   ", HabitCategory.HEALTH, Frequency.DAILY);
        } catch (InvalidInputException e) {
            threw = true;
        }
        TestRunner.assertTrue("Creating habit with blank name should throw InvalidInputException", threw);
    }

    public void testDuplicateNameRejection() throws InvalidInputException, StorageException {
        HabitService service = createTestService("dup");
        service.createHabit("Drink Water", HabitCategory.HYDRATION, Frequency.DAILY);

        boolean threw = false;
        try {
            // Case-insensitive duplicate check
            service.createHabit("drink water", HabitCategory.HYDRATION, Frequency.DAILY);
        } catch (InvalidInputException e) {
            threw = true;
        }
        TestRunner.assertTrue("Creating duplicate habit should throw InvalidInputException", threw);
    }

    public void testMissingIdLookup() throws StorageException {
        HabitService service = createTestService("missing");
        boolean threw = false;
        try {
            service.getHabitById(9999);
        } catch (HabitNotFoundException e) {
            threw = true;
            TestRunner.assertEquals("Exception should contain missing ID", 9999, e.getHabitId());
        }
        TestRunner.assertTrue("Looking up non-existent ID should throw HabitNotFoundException", threw);
    }

    public void testArchiveBehavior() throws InvalidInputException, StorageException, HabitNotFoundException {
        HabitService service = createTestService("archive");
        Habit h1 = service.createHabit("Habit To Archive", HabitCategory.OTHER, Frequency.DAILY);
        TestRunner.assertTrue("Should be active initially", h1.isActive());

        service.archiveHabit(h1.getId());
        Habit fetched = service.getHabitById(h1.getId());
        TestRunner.assertFalse("Should be archived (active = false)", fetched.isActive());

        List<Habit> activeHabits = service.getActiveHabits();
        TestRunner.assertEquals("Active list should not contain archived habit", 0, activeHabits.size());

        List<Habit> allHabits = service.getAllHabits();
        TestRunner.assertEquals("All habits list should contain archived habit", 1, allHabits.size());
    }
}
