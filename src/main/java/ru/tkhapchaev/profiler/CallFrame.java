package ru.tkhapchaev.profiler;

public class CallFrame {
    private final String methodId;
    private final long startNanos;

    private long childNanos;

    public CallFrame(String methodId, long startNanos) {
        this.methodId = methodId;
        this.startNanos = startNanos;
        this.childNanos = 0L;
    }

    public String methodId() {
        return methodId;
    }

    public long startNanos() {
        return startNanos;
    }

    public long childNanos() {
        return childNanos;
    }

    public void addChildNanos(long nanos) {
        childNanos += nanos;
    }
}