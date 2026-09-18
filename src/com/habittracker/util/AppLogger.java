package com.habittracker.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {
    private static final String LOG_FILE_PATH = "data/app.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {
    }

    private static synchronized void log(String level, String message) {
        try {
            File logFile = new File(LOG_FILE_PATH);
            File parent = logFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                pw.println(String.format("[%s] [%s] %s", timestamp, level, message));
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not write to app.log: " + e.getMessage());
        }
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void error(String message, Throwable t) {
        log("ERROR", message + (t != null ? " (" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")" : ""));
    }
}
