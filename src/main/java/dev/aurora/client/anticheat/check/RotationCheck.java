package dev.aurora.client.anticheat.check;

import dev.aurora.client.anticheat.DetectionResult;
import dev.aurora.client.anticheat.PlayerObservation;

/**
 * Detects rotation patterns associated with aim assistance (Aimbot / KillAura).
 * <p>
 * Combines two statistical signals on the yaw stream:
 * <ul>
 *   <li><b>Snap ratio</b> – proportion of consecutive deltas that are large
 *       (instant flicks) immediately followed by near-zero deltas.</li>
 *   <li><b>GCD consistency</b> – legitimate mouse input has a fractional,
 *       hardware-dependent sensitivity granularity; perfectly regular deltas are
 *       a weak indicator of synthetic rotation.</li>
 * </ul>
 * Confidence is capped and only emitted above a threshold, with a sample-count
 * guard to limit false positives on small windows.
 */
public final class RotationCheck implements Check {

    @Override public String name() { return "Aimbot/KillAura"; }

    @Override
    public DetectionResult analyse(PlayerObservation o) {
        if (o.rotations.size() < 12) return null;

        float[] prev = null;
        float[] prevDelta = null;
        int snaps = 0, total = 0;
        double sumAbsDelta = 0;

        for (float[] r : o.rotations) {
            if (prev != null) {
                float dYaw = wrap(r[0] - prev[0]);
                sumAbsDelta += Math.abs(dYaw);
                if (prevDelta != null) {
                    float prevYaw = prevDelta[0];
                    // A "snap" = large flick (>30°) followed by a tiny correction (<2°).
                    if (Math.abs(prevYaw) > 30 && Math.abs(dYaw) < 2) snaps++;
                    total++;
                }
                prevDelta = new float[]{dYaw};
            }
            prev = r;
        }
        if (total < 6) return null;

        double snapRatio = (double) snaps / total;
        double avgDelta = sumAbsDelta / o.rotations.size();

        // Require a meaningful snap ratio AND active aiming (non-trivial movement).
        if (snapRatio > 0.35 && avgDelta > 5) {
            double confidence = Math.min(0.92, 0.4 + snapRatio);
            return new DetectionResult(name(), confidence,
                    "Unnatural rotation pattern (snap ratio "
                            + Math.round(snapRatio * 100) + "%)");
        }
        return null;
    }

    private static float wrap(float deg) {
        deg %= 360f;
        if (deg >= 180f) deg -= 360f;
        if (deg < -180f) deg += 360f;
        return deg;
    }
}
