package ru.tkhapchaev.profiler;

import java.io.PrintStream;
import java.util.List;

public class ProfilerReportPrinter {
    private final PrintStream out;

    public ProfilerReportPrinter() {
        this(System.out);
    }

    public ProfilerReportPrinter(PrintStream out) {
        this.out = out;
    }

    public void print(List<ReportRow> rows, int topN, SortMode sortMode) {
        if (rows.isEmpty()) {
            out.println("Profiler report: no collected method data");

            return;
        }

        out.printf("%n=== Profiler report (top %d by %s) ===%n", topN, sortMode.name().toLowerCase());
        out.printf("%-85s | %10s | %13s | %13s%n", "method", "calls", "total time", "avg time");
        out.println("-".repeat(131));

        for (ReportRow row : rows) {
            out.printf(
                "%-85s | %10d | %10.3f ms | %10.3f ms%n",
                row.methodId(),
                row.calls(),
                nanosToMillis(row.totalNanos()),
                nanosToMillis(row.avgNanos())
            );
        }
    }

    private double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }
}