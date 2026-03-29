package ru.tkhapchaev.profiler;

public enum SortMode {
    TOTAL,
    AVG;

    public static SortMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return TOTAL;
        }

        String normalized = value.trim().toLowerCase();

        if ("avg".equals(normalized) || "average".equals(normalized)) {
            return AVG;
        }

        return TOTAL;
    }
}