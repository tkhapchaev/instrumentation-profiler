package ru.tkhapchaev.profiler;

public class AgentConfigParser {
    private static final int DEFAULT_TOP_N = 10;

    public AgentConfig parse(String rawArgs) {
        int topN = DEFAULT_TOP_N;
        SortMode sortMode = SortMode.TOTAL;

        if (rawArgs != null && !rawArgs.isBlank()) {
            String[] pairs = rawArgs.split(",");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);

                if (keyValue.length != 2) {
                    continue;
                }

                String key = keyValue[0].trim().toLowerCase();
                String value = keyValue[1].trim();

                if ("top".equals(key)) {
                    topN = parseTop(value);
                } else if ("sort".equals(key)) {
                    sortMode = SortMode.fromString(value);
                }
            }
        }

        return new AgentConfig(topN, sortMode);
    }

    private int parseTop(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return DEFAULT_TOP_N;
        }
    }
}