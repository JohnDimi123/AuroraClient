package dev.aurora.client.anticheat.check;

import dev.aurora.client.anticheat.DetectionResult;
import dev.aurora.client.anticheat.PlayerObservation;

/**
 * Detects suspiciously regular attack intervals (KillAura / AutoClicker).
 * <p>
 * Human clicking has natural jitter; a very low coefficient of variation in
 * inter-attack intervals, combined with a high effective click rate, is a weak
 * indicator of automation. A minimum sample size guards against false positives.
 */
public final class AttackTimingCheck implements Check {

    @Override public String name() { return "AutoClicker/KillAura"; }

    @Override
    public DetectionResult analyse(PlayerObservation o) {
        if (o.attackTimes.size() < 10) return null;

        Long prev = null;
        java.util.List<Long> intervals = new java.util.ArrayList<>();
        for (Long t : o.attackTimes) {
            if (prev != null) intervals.add(t - prev);
            prev = t;
        }
        if (intervals.size() < 8) return null;

        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
        if (mean <= 0) return null;
        double variance = intervals.stream()
                .mapToDouble(i -> (i - mean) * (i - mean)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = stdDev / mean; // coefficient of variation

        double cps = 1000.0 / mean;

        // Extremely low variation at a high rate is the signal.
        if (cv < 0.08 && cps > 8) {
            double confidence = Math.min(0.88, 0.5 + (0.08 - cv) * 4);
            return new DetectionResult(name(), confidence,
                    "Highly regular attack timing (~" + Math.round(cps)
                            + " CPS, CV " + Math.round(cv * 100) + "%)");
        }
        return null;
    }
}
