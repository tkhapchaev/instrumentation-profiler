package ru.tkhapchaev.profiler;

public class ProfilerRuntime {
    private static final MethodProfiler PROFILER = new MethodProfiler();

    private ProfilerRuntime() { }

    public static void enter(String methodId) {
        PROFILER.enter(methodId);
    }

    public static void exit(String methodId) {
        PROFILER.exit(methodId);
    }

    public static void printReport(int topN, SortMode sortMode) {
        PROFILER.printReport(topN, sortMode);
    }

    public static MethodProfiler profiler() {
        return PROFILER;
    }
}