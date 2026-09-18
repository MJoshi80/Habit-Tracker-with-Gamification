package com.habittracker.util;

import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    private CsvUtil() {
        // utility class
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (containsSpecial) {
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return value;
    }

    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    public static String join(List<String> fields) {
        return join(fields.toArray(new String[0]));
    }

    public static List<String> parseLine(String line) {
        List<String> tokens = new ArrayList<String>();
        if (line == null || line.trim().isEmpty()) {
            return tokens;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote
                    sb.append('\"');
                    i++; // skip second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());

        return tokens;
    }
}
