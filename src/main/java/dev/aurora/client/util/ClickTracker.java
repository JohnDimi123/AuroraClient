package dev.aurora.client.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks click timestamps for left and right mouse buttons and computes
 * clicks-per-second over a rolling one-second window.
 */
public final class ClickTracker {

    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    public void onLeftClick() { leftClicks.addLast(System.currentTimeMillis()); }
    public void onRightClick() { rightClicks.addLast(System.currentTimeMillis()); }

    public int getLeftCps() { return count(leftClicks); }
    public int getRightCps() { return count(rightClicks); }

    private int count(Deque<Long> clicks) {
        long cutoff = System.currentTimeMillis() - 1000L;
        while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) clicks.pollFirst();
        return clicks.size();
    }
}
