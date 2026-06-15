package dev.aurora.client.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Samples runtime performance metrics on a fixed cadence to avoid per-frame
 * JMX overhead. Values are smoothed so HUD readouts don't flicker.
 */
public final class PerformanceMonitor {

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final Runtime runtime = Runtime.getRuntime();

    private long lastFrame = System.nanoTime();
    private long lastSample = 0L;
    private static final long SAMPLE_INTERVAL_MS = 500L;

    private double frameTimeMs = 0;
    private double smoothedFrameTime = 0;
    private int fps = 0;
    private long usedMemoryMb = 0;
    private long maxMemoryMb = 0;
    private double cpuLoad = 0;

    /** Call once per rendered frame. */
    public void onFrame() {
        long now = System.nanoTime();
        frameTimeMs = (now - lastFrame) / 1_000_000.0;
        lastFrame = now;
        // Exponential smoothing for a stable readout.
        smoothedFrameTime = smoothedFrameTime == 0 ? frameTimeMs
                : smoothedFrameTime * 0.9 + frameTimeMs * 0.1;
        fps = smoothedFrameTime > 0 ? (int) Math.round(1000.0 / smoothedFrameTime) : 0;

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastSample >= SAMPLE_INTERVAL_MS) {
            lastSample = nowMs;
            usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / 1_048_576L;
            maxMemoryMb = runtime.maxMemory() / 1_048_576L;
            cpuLoad = readCpuLoad();
        }
    }

    @SuppressWarnings("restriction")
    private double readCpuLoad() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                double load = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad();
                return load < 0 ? cpuLoad : load * 100.0;
            }
        } catch (Throwable ignored) { /* not available on this JVM */ }
        return cpuLoad;
    }

    public int getFps() { return fps; }
    public double getFrameTimeMs() { return smoothedFrameTime; }
    public long getUsedMemoryMb() { return usedMemoryMb; }
    public long getMaxMemoryMb() { return maxMemoryMb; }
    public int getMemoryPercent() {
        return maxMemoryMb == 0 ? 0 : (int) (usedMemoryMb * 100 / maxMemoryMb);
    }
    public double getCpuLoad() { return cpuLoad; }
}
