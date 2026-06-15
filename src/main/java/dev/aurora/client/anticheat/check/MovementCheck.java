package dev.aurora.client.anticheat.check;

import dev.aurora.client.anticheat.DetectionResult;
import dev.aurora.client.anticheat.PlayerObservation;

/**
 * Detects movement inconsistent with vanilla physics (Flight / Speed).
 * <p>
 * Examines horizontal speed and vertical behaviour across position samples.
 * Sustained horizontal speed well above the sprint-jump ceiling, or prolonged
 * hovering with zero vertical change while airborne, are weak indicators. These
 * are heuristics only and can be affected by lag, ice, or knockback, hence the
 * conservative thresholds and capped confidence.
 */
public final class MovementCheck implements Check {

    private static final double SPRINT_JUMP_SPEED = 0.40; // blocks/tick upper-ish bound

    @Override public String name() { return "Flight/Speed"; }

    @Override
    public DetectionResult analyse(PlayerObservation o) {
        if (o.positions.size() < 12) return null;

        double[] prev = null;
        int fastTicks = 0, total = 0, hoverTicks = 0;
        for (double[] p : o.positions) {
            if (prev != null) {
                double dx = p[0] - prev[0];
                double dz = p[2] - prev[2];
                double dy = p[1] - prev[1];
                double horiz = Math.sqrt(dx * dx + dz * dz);
                if (horiz > SPRINT_JUMP_SPEED) fastTicks++;
                if (Math.abs(dy) < 0.001 && horiz > 0.05) hoverTicks++;
                total++;
            }
            prev = p;
        }
        if (total < 8) return null;

        double fastRatio = (double) fastTicks / total;
        double hoverRatio = (double) hoverTicks / total;

        if (fastRatio > 0.6) {
            double confidence = Math.min(0.85, 0.4 + fastRatio * 0.5);
            return new DetectionResult("Speed", confidence,
                    "Sustained horizontal speed above vanilla limits ("
                            + Math.round(fastRatio * 100) + "% of samples)");
        }
        if (hoverRatio > 0.7) {
            double confidence = Math.min(0.80, 0.4 + hoverRatio * 0.4);
            return new DetectionResult("Flight", confidence,
                    "Prolonged airborne hovering (" + Math.round(hoverRatio * 100) + "% of samples)");
        }
        return null;
    }
}
