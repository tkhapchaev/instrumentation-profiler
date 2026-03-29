package ru.tkhapchaev.profiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MethodProfilerTest {
    private MethodProfiler profiler;

    @BeforeEach
    void setup() {
        profiler = new MethodProfiler();
    }

    @AfterEach
    void cleanup() {
        profiler.clear();
    }

    @Test
    void collectsCallsAndTotalTime() {
        for (int i = 0; i < 3; i++) {
            profiler.enter("demo#hot()");
            busyWork(100_000);
            profiler.exit("demo#hot()");
        }

        Map<String, ReportRow> rowsByMethod = profiler
            .snapshot(10, SortMode.TOTAL)
            .stream()
            .collect(Collectors.toMap(ReportRow::methodId, Function.identity()));

        ReportRow row = rowsByMethod.get("demo#hot()");

        assertEquals(3L, row.calls());
        assertTrue(row.totalNanos() > 0);
        assertTrue(row.avgNanos() > 0);
    }

    @Test
    void keepsNestedCalls() {
        profiler.enter("demo#outer()");
        busyWork(40_000);

        profiler.enter("demo#inner()");
        busyWork(80_000);

        profiler.exit("demo#inner()");
        profiler.exit("demo#outer()");

        Map<String, ReportRow> rowsByMethod = profiler
            .snapshot(10, SortMode.TOTAL)
            .stream()
            .collect(Collectors.toMap(ReportRow::methodId, Function.identity()));

        assertFalse(rowsByMethod.isEmpty());
        assertEquals(1L, rowsByMethod.get("demo#outer()").calls());
        assertEquals(1L, rowsByMethod.get("demo#inner()").calls());
        assertTrue(rowsByMethod.get("demo#outer()").totalNanos() >= rowsByMethod.get("demo#inner()").totalNanos());
    }

    private static long busyWork(int iterations) {
        long value = 0;

        for (int i = 0; i < iterations; i++) {
            value += (i * 13L) ^ (i >>> 1);
        }

        return value;
    }
}