package dev.aurora.client.anticheat.check;

import dev.aurora.client.anticheat.DetectionResult;
import dev.aurora.client.anticheat.PlayerObservation;

/**
 * Placeholder hook for reach analysis. Reach must be computed from attacker and
 * victim positions at attack time; that contextual data is supplied by the
 * engine via {@link #setLastReach}. Reported only when clearly above the vanilla
 * 3.0-block interaction range, with margin for latency.
 */
public final class ReachCheck implements Check {

    private double lastReach = -1;

    public void setLastReach(double reach) { this.lastReach = reach; }

    @Override public String name() { return "Reach"; }

    @Override
    public DetectionResult analyse(PlayerObservation o) {
        if (lastReach < 0) return null;
        double r = lastReach;
        lastReach = -1;
        if (r > 3.6) {
            double confidence = Math.min(0.8, 0.3 + (r - 3.6) * 0.4);
            return new DetectionResult(name(), confidence,
                    String.format("Attack distance %.2f blocks exceeds vanilla range", r));
        }
        return null;
    }
}
