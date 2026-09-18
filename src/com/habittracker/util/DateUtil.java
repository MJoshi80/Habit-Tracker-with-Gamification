package com.habittracker.util;

import com.habittracker.model.Frequency;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final WeekFields ISO_WEEKS = WeekFields.ISO;

    private DateUtil() {
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    public static LocalDate parseDate(String text) throws DateTimeParseException {
        if (text == null || text.trim().isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(text.trim(), DATE_FORMATTER);
    }

    public static boolean isFutureDate(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }

    public static String toUnitKey(LocalDate date, Frequency frequency) {
        if (date == null) return "";
        if (frequency == Frequency.DAILY) {
            return date.toString();
        } else {
            int year = date.get(ISO_WEEKS.weekBasedYear());
            int week = date.get(ISO_WEEKS.weekOfWeekBasedYear());
            return String.format(Locale.ROOT, "%04d-W%02d", year, week);
        }
    }

    public static String previousUnitKey(String currentUnitKey, Frequency frequency) {
        if (currentUnitKey == null || currentUnitKey.isEmpty()) {
            return "";
        }
        if (frequency == Frequency.DAILY) {
            LocalDate date = LocalDate.parse(currentUnitKey, DATE_FORMATTER);
            return date.minusDays(1).toString();
        } else {
            // Parses e.g. 2026-W38
            String[] parts = currentUnitKey.split("-W");
            int year = Integer.parseInt(parts[0]);
            int week = Integer.parseInt(parts[1]);
            // Anchor to Thursday of this ISO week
            LocalDate thursday = LocalDate.of(year, 1, 4)
                    .with(ISO_WEEKS.weekOfWeekBasedYear(), week)
                    .with(DayOfWeek.THURSDAY);
            LocalDate previousWeekThursday = thursday.minusWeeks(1);
            int prevYear = previousWeekThursday.get(ISO_WEEKS.weekBasedYear());
            int prevWeek = previousWeekThursday.get(ISO_WEEKS.weekOfWeekBasedYear());
            return String.format(Locale.ROOT, "%04d-W%02d", prevYear, prevWeek);
        }
    }
}
