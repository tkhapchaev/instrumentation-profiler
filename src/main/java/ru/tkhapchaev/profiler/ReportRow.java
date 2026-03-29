package ru.tkhapchaev.profiler;

public record ReportRow(String methodId, long calls, long totalNanos, double avgNanos, long selfNanos) { }