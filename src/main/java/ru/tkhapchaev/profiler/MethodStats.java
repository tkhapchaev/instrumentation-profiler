package ru.tkhapchaev.profiler;

import java.util.concurrent.atomic.LongAdder;

public class MethodStats {
    private final LongAdder calls = new LongAdder();
    private final LongAdder totalNanos = new LongAdder();
    private final LongAdder selfNanos = new LongAdder();

    public void record(long elapsedNanos, long selfTimeNanos) {
        calls.increment();
        totalNanos.add(elapsedNanos);
        selfNanos.add(Math.max(0L, selfTimeNanos));
    }

    public long calls() {
        return calls.sum();
    }

    public long totalNanos() {
        return totalNanos.sum();
    }

    public long selfNanos() {
        return selfNanos.sum();
    }

    public double avgNanos() {
        long callCount = calls();

        if (callCount == 0L) {
            return 0.0;
        }

        return (double) totalNanos() / callCount;
    }
}