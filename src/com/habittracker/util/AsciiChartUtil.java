package com.habittracker.util;

import java.util.List;

public class AsciiChartUtil {

    private AsciiChartUtil() {
    }

    public static String renderProgressBar(int current, int max, int width) {
        if (max <= 0) {
            max = 1;
        }
        int clampedCurrent = Math.max(0, Math.min(current, max));
        double ratio = (double) clampedCurrent / max;
        int filled = (int) Math.round(ratio * width);
        int empty = width - filled;

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < filled; i++) {
            sb.append("#");
        }
        for (int i = 0; i < empty; i++) {
            sb.append("-");
        }
        sb.append(String.format("] %3d%%", (int) (ratio * 100)));
        return sb.toString();
    }

    public static String renderStreakStars(int streak) {
        if (streak <= 0) {
            return "0 days";
        }
        int stars = 1;
        if (streak >= 30) stars = 4;
        else if (streak >= 14) stars = 3;
        else if (streak >= 7) stars = 2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append("*");
        }
        sb.append(String.format(" %d %s", streak, streak == 1 ? "period" : "periods"));
        return sb.toString();
    }

    public static String renderWeekStrip(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "[No Data]";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < statuses.size(); i++) {
            String status = statuses.get(i);
            if (i > 0) {
                sb.append(" ");
            }
            if ("DONE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                sb.append("[V]");
            } else if ("MISSED".equalsIgnoreCase(status) || "SKIPPED".equalsIgnoreCase(status)) {
                sb.append("[X]");
            } else {
                sb.append("[-]");
            }
        }
        return sb.toString();
    }
}
