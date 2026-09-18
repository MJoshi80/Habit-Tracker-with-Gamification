package com.habittracker.ui;

import com.habittracker.exception.HabitNotFoundException;
import com.habittracker.exception.HabitTrackerException;
import com.habittracker.exception.InvalidInputException;
import com.habittracker.exception.StorageException;
import com.habittracker.model.BadgeType;
import com.habittracker.model.CompletionLog;
import com.habittracker.model.Frequency;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCategory;
import com.habittracker.model.UserProfile;
import com.habittracker.service.GamificationService;
import com.habittracker.service.HabitService;
import com.habittracker.service.ReportService;
import com.habittracker.service.TrackingService;
import com.habittracker.util.AsciiChartUtil;
import com.habittracker.util.DateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final HabitService habitService;
    private final TrackingService trackingService;
    private final GamificationService gamificationService;
    private final ReportService reportService;
    private final Scanner scanner;

    public ConsoleUI(HabitService habitService, TrackingService trackingService,
                     GamificationService gamificationService, ReportService reportService) {
        this.habitService = habitService;
        this.trackingService = trackingService;
        this.gamificationService = gamificationService;
        this.reportService = reportService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice (0-9): ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        showDashboard();
                        break;
                    case "2":
                        logHabit(true);
                        break;
                    case "3":
                        logHabit(false);
                        break;
                    case "4":
                        addHabit();
                        break;
                    case "5":
                        editHabit();
                        break;
                    case "6":
                        archiveOrDeleteHabit();
                        break;
                    case "7":
                        viewHabitDetails();
                        break;
                    case "8":
                        showBadgeCabinet();
                        break;
                    case "0":
                        System.out.println("\nThank you for using Habit Tracker! Stay consistent!");
                        running = false;
                        break;
                    default:
                        System.out.println("\n[!] Invalid selection. Please choose an option between 0 and 8.");
                }
            } catch (HabitTrackerException e) {
                System.out.println("\n[ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[UNEXPECTED ERROR] An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void printBanner() {
        System.out.println("========================================================================================");
        System.out.println("                            HABIT TRACKER WITH GAMIFICATION                             ");
        System.out.println("            Offline Command-Line Application for Building Consistent Habits             ");
        System.out.println("========================================================================================");
    }

    private void printMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println(" 1. View Dashboard & Progress");
        System.out.println(" 2. Mark Habit Complete");
        System.out.println(" 3. Mark Habit Skipped");
        System.out.println(" 4. Add New Habit");
        System.out.println(" 5. Edit Habit (Rename / Category / Frequency)");
        System.out.println(" 6. Archive or Delete Habit");
        System.out.println(" 7. View Habit History & Deep Dive");
        System.out.println(" 8. View Badge Cabinet");
        System.out.println(" 0. Exit");
    }

    private void showDashboard() throws StorageException {
        System.out.println(reportService.generateDashboard());
    }

    private void showBadgeCabinet() throws StorageException {
        System.out.println(reportService.generateBadgeCabinet());
    }

    private void addHabit() throws InvalidInputException, StorageException {
        System.out.println("\n--- Add New Habit ---");
        System.out.print("Enter habit name: ");
        String name = scanner.nextLine().trim();

        System.out.println("\nSelect Category:");
        HabitCategory[] categories = HabitCategory.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("  %d. %s\n", (i + 1), categories[i].getDisplayName());
        }
        System.out.print("Enter choice (1-8, press Enter for Other): ");
        String catChoice = scanner.nextLine().trim();
        HabitCategory category = HabitCategory.OTHER;
        if (!catChoice.isEmpty()) {
            try {
                int idx = Integer.parseInt(catChoice) - 1;
                if (idx >= 0 && idx < categories.length) {
                    category = categories[idx];
                }
            } catch (NumberFormatException ignored) {
            }
        }

        System.out.println("\nSelect Frequency:");
        System.out.println("  1. Daily (Default)");
        System.out.println("  2. Weekly");
        System.out.print("Enter choice (1-2, press Enter for Daily): ");
        String freqChoice = scanner.nextLine().trim();
        Frequency frequency = "2".equals(freqChoice) ? Frequency.WEEKLY : Frequency.DAILY;

        Habit created = habitService.createHabit(name, category, frequency);
        System.out.printf("\n[SUCCESS] Created Habit [%d]: '%s' (%s, %s)\n",
                created.getId(), created.getName(), created.getCategory().getDisplayName(), created.getFrequency().getDisplayName());
    }

    private void logHabit(boolean completed) throws HabitNotFoundException, InvalidInputException, StorageException {
        List<Habit> active = habitService.getActiveHabits();
        if (active.isEmpty()) {
            System.out.println("\n[!] No active habits found. Please create a habit first.");
            return;
        }

        System.out.printf("\n--- Mark Habit %s ---\n", completed ? "Complete" : "Skipped");
        for (Habit h : active) {
            int streak = trackingService.getCurrentStreak(h.getId());
            System.out.printf("  [%d] %-25s (Current Streak: %d)\n", h.getId(), h.getName(), streak);
        }

        System.out.print("Enter Habit ID to log: ");
        String idStr = scanner.nextLine().trim();
        int habitId;
        try {
            habitId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid Habit ID entered.");
        }

        System.out.print("Enter date (YYYY-MM-DD, press Enter for Today): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate date;
        try {
            date = dateStr.isEmpty() ? LocalDate.now() : DateUtil.parseDate(dateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date format. Expected YYYY-MM-DD.");
        }

        CompletionLog log;
        if (completed) {
            log = trackingService.markCompleted(habitId, date);
            int newStreak = trackingService.getCurrentStreak(habitId);
            UserProfile profile = gamificationService.getProfile();

            System.out.printf("\n[AWESOME!] Completed on %s! +%d XP earned!\n",
                    DateUtil.formatDate(date), log.getXpEarned());
            System.out.printf("Current Streak: %s\n", AsciiChartUtil.renderStreakStars(newStreak));
            System.out.printf("Total XP: %d | Level %d\n", profile.getTotalXp(), profile.getLevel());
        } else {
            log = trackingService.markSkipped(habitId, date);
            System.out.printf("\n[NOTED] Marked skipped for %s. Consistency is a journey -- come back tomorrow!\n",
                    DateUtil.formatDate(date));
        }
    }

    private void editHabit() throws HabitNotFoundException, InvalidInputException, StorageException {
        List<Habit> active = habitService.getActiveHabits();
        if (active.isEmpty()) {
            System.out.println("\n[!] No active habits found.");
            return;
        }

        System.out.println("\n--- Edit Habit ---");
        for (Habit h : active) {
            System.out.println("  " + h);
        }
        System.out.print("Enter Habit ID to edit: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Habit habit = habitService.getHabitById(id);

        System.out.println("\nWhat would you like to edit?");
        System.out.println(" 1. Rename Habit");
        System.out.println(" 2. Change Category");
        System.out.println(" 3. Change Frequency");
        System.out.print("Enter choice (1-3): ");
        String editChoice = scanner.nextLine().trim();

        switch (editChoice) {
            case "1":
                System.out.print("Enter new name: ");
                String newName = scanner.nextLine().trim();
                habitService.renameHabit(id, newName);
                System.out.println("[SUCCESS] Habit renamed successfully.");
                break;
            case "2":
                System.out.println("Select Category:");
                HabitCategory[] categories = HabitCategory.values();
                for (int i = 0; i < categories.length; i++) {
                    System.out.printf("  %d. %s\n", (i + 1), categories[i].getDisplayName());
                }
                System.out.print("Enter choice (1-8): ");
                int catIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (catIdx >= 0 && catIdx < categories.length) {
                    habitService.changeCategory(id, categories[catIdx]);
                    System.out.println("[SUCCESS] Category updated.");
                }
                break;
            case "3":
                System.out.println("Select Frequency: 1. Daily  2. Weekly");
                String freqChoice = scanner.nextLine().trim();
                Frequency freq = "2".equals(freqChoice) ? Frequency.WEEKLY : Frequency.DAILY;
                habitService.changeFrequency(id, freq);
                System.out.println("[SUCCESS] Frequency updated.");
                break;
            default:
                System.out.println("[!] Invalid choice.");
        }
    }

    private void archiveOrDeleteHabit() throws HabitNotFoundException, StorageException {
        List<Habit> habits = habitService.getAllHabits();
        if (habits.isEmpty()) {
            System.out.println("\n[!] No habits exist.");
            return;
        }

        System.out.println("\n--- Archive or Delete Habit ---");
        for (Habit h : habits) {
            System.out.println("  " + h);
        }
        System.out.print("Enter Habit ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Habit habit = habitService.getHabitById(id);

        System.out.printf("Selected: [%d] %s\n", habit.getId(), habit.getName());
        System.out.println(" 1. Archive (Soft delete - preserves streak history)");
        System.out.println(" 2. Permanently Delete (Removes habit and all history logs)");
        System.out.print("Enter choice (1-2): ");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            habitService.archiveHabit(id);
            System.out.println("[SUCCESS] Habit archived. It will no longer appear on active tracking.");
        } else if ("2".equals(choice)) {
            System.out.print("Are you sure you want to permanently delete this habit and all logs? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
                habitService.deleteHabitPermanently(id);
                System.out.println("[SUCCESS] Habit and associated logs permanently deleted.");
            } else {
                System.out.println("[INFO] Deletion cancelled.");
            }
        }
    }

    private void viewHabitDetails() throws HabitNotFoundException, StorageException {
        List<Habit> habits = habitService.getAllHabits();
        if (habits.isEmpty()) {
            System.out.println("\n[!] No habits found.");
            return;
        }

        System.out.println("\n--- Habit Deep Dive ---");
        for (Habit h : habits) {
            System.out.println("  " + h);
        }
        System.out.print("Enter Habit ID to view: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.println(reportService.generateHabitDetails(id));
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid Habit ID format.");
        }
    }

    public void runDemoWalkthrough() {
        System.out.println("\n>>> STARTING AUTOMATED DEMO WALKTHROUGH <<<");
        try {
            System.out.println("\n1. Initializing Demo Habits...");
            Habit h1 = findOrCreateHabit("Daily Java Coding", HabitCategory.CODING, Frequency.DAILY);
            Habit h2 = findOrCreateHabit("Morning Workout", HabitCategory.FITNESS, Frequency.DAILY);
            Habit h3 = findOrCreateHabit("Read Technical Book", HabitCategory.READING, Frequency.WEEKLY);
            System.out.printf("   Ready: [%d] %s, [%d] %s, [%d] %s\n",
                    h1.getId(), h1.getName(), h2.getId(), h2.getName(), h3.getId(), h3.getName());

            System.out.println("\n2. Simulating Consecutive Completions (Building Streaks & XP)...");
            LocalDate today = LocalDate.now();
            for (int i = 7; i >= 0; i--) {
                LocalDate logDate = today.minusDays(i);
                try {
                    CompletionLog l1 = trackingService.markCompleted(h1.getId(), logDate);
                    System.out.printf("   Logged %s on %s (+%d XP)\n", h1.getName(), DateUtil.formatDate(logDate), l1.getXpEarned());
                } catch (InvalidInputException ignored) {
                    // Already logged
                }
            }

            System.out.println("\n3. Simulating Skipped Day for Workout (Testing Broken Streak & Comeback)...");
            try {
                trackingService.markCompleted(h2.getId(), today.minusDays(2));
            } catch (InvalidInputException ignored) {}
            try {
                trackingService.markSkipped(h2.getId(), today.minusDays(1));
            } catch (InvalidInputException ignored) {}
            try {
                trackingService.markCompleted(h2.getId(), today);
            } catch (InvalidInputException ignored) {}
            System.out.println("   Workout completed, then skipped, then resumed.");

            System.out.println("\n4. Displaying Rendered ASCII Dashboard:");
            System.out.println(reportService.generateDashboard());

            System.out.println("\n5. Displaying Unlocked Badges in Cabinet:");
            System.out.println(reportService.generateBadgeCabinet());

            System.out.println(">>> AUTOMATED DEMO WALKTHROUGH COMPLETED SUCCESSFULLY <<<\n");
        } catch (Exception e) {
            System.out.println("[DEMO ERROR] " + e.getMessage());
        }
    }

    private Habit findOrCreateHabit(String name, HabitCategory category, Frequency frequency)
            throws StorageException, InvalidInputException {
        List<Habit> active = habitService.getActiveHabits();
        for (Habit h : active) {
            if (h.getName().equalsIgnoreCase(name.trim())) {
                return h;
            }
        }
        return habitService.createHabit(name, category, frequency);
    }
}
