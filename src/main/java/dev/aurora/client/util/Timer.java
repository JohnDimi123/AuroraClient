package dev.aurora.client.util;

/** Simple monotonic stopwatch with millisecond resolution. */
public final class Timer {
    private long last = System.currentTimeMillis();

    /** @return true if at least {@code ms} have elapsed since the last reset. */
    public boolean hasElapsed(long ms) {
        return System.currentTimeMillis() - last >= ms;
    }

    /** @return true and resets if elapsed; otherwise false. */
    public boolean passed(long ms) {
        if (hasElapsed(ms)) { reset(); return true; }
        return false;
    }

    public long elapsed() { return System.currentTimeMillis() - last; }
    public void reset() { last = System.currentTimeMillis(); }
}
