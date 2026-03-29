package ru.tkhapchaev.profiler;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MethodProfiler {
    private final ConcurrentMap<String, MethodStats> statsByMethod;
    private final ThreadLocal<Deque<CallFrame>> callStacks;
    private final ProfilerReportPrinter reportPrinter;

    public MethodProfiler() {
        this(new ConcurrentHashMap<>(), ThreadLocal.withInitial(ArrayDeque::new), new ProfilerReportPrinter());
    }

    public MethodProfiler(
        ConcurrentMap<String, MethodStats> statsByMethod,
        ThreadLocal<Deque<CallFrame>> callStacks,
        ProfilerReportPrinter reportPrinter
    ) {
        this.statsByMethod = statsByMethod;
        this.callStacks = callStacks;
        this.reportPrinter = reportPrinter;
    }

    public void enter(String methodId) {
        callStacks.get().push(new CallFrame(methodId, System.nanoTime()));
    }

    public void exit(String methodId) {
        Deque<CallFrame> stack = callStacks.get();

        if (stack.isEmpty()) {
            return;
        }

        CallFrame frame = stack.pop();

        long elapsedNanos = System.nanoTime() - frame.startNanos();
        long selfNanos = elapsedNanos - frame.childNanos();

        MethodStats stats = statsByMethod.computeIfAbsent(frame.methodId(), key -> new MethodStats());
        stats.record(elapsedNanos, selfNanos);

        if (!stack.isEmpty()) {
            stack.peek().addChildNanos(elapsedNanos);
        }
    }

    public void printReport(int topN, SortMode sortMode) {
        List<ReportRow> rows = snapshot(topN, sortMode);
        reportPrinter.print(rows, topN, sortMode);
    }

    public List<ReportRow> snapshot(int topN, SortMode sortMode) {
        Comparator<ReportRow> comparator = sortMode == SortMode.AVG
            ? Comparator.comparingDouble(ReportRow::avgNanos).reversed()
            : Comparator.comparingLong(ReportRow::totalNanos).reversed();

        return statsByMethod.entrySet()
            .stream()
            .map(this::toRow)
            .sorted(comparator.thenComparing(ReportRow::methodId))
            .limit(Math.max(1, topN))
            .toList();
    }

    public void clear() {
        statsByMethod.clear();
        callStacks.remove();
    }

    private ReportRow toRow(Map.Entry<String, MethodStats> entry) {
        MethodStats stats = entry.getValue();
        return new ReportRow(entry.getKey(), stats.calls(), stats.totalNanos(), stats.avgNanos(), stats.selfNanos());
    }
}