package com.habittracker;

import com.habittracker.repository.FileHabitRepository;
import com.habittracker.repository.FileLogRepository;
import com.habittracker.repository.FileProfileRepository;
import com.habittracker.service.GamificationService;
import com.habittracker.service.HabitService;
import com.habittracker.service.ReportService;
import com.habittracker.service.TrackingService;
import com.habittracker.ui.ConsoleUI;
import com.habittracker.util.AppLogger;

public class Main {
    public static void main(String[] args) {
        AppLogger.info("Starting Habit Tracker application...");

        // Initialize Persistence Repositories
        FileHabitRepository habitRepository = new FileHabitRepository("data/habits.csv");
        FileLogRepository logRepository = new FileLogRepository("data/logs.csv");
        FileProfileRepository profileRepository = new FileProfileRepository("data/profile.csv");

        // Initialize Services
        HabitService habitService = new HabitService(habitRepository, logRepository);
        TrackingService trackingService = new TrackingService(habitRepository, logRepository);
        GamificationService gamificationService = new GamificationService(profileRepository, logRepository);
        ReportService reportService = new ReportService(habitService, trackingService, gamificationService);

        // Cross-wire Circular/Collaborative dependencies
        trackingService.setGamificationService(gamificationService);

        // Initialize UI
        ConsoleUI consoleUI = new ConsoleUI(habitService, trackingService, gamificationService, reportService);

        // Run interactive CLI
        consoleUI.start();
        AppLogger.info("Exiting Habit Tracker application.");
    }
}
