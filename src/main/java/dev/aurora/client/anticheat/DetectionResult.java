package dev.aurora.client.anticheat;

/**
 * The outcome of a single {@link dev.aurora.client.anticheat.check.Check}
 * analysing a tracked player. Confidence is a 0..1 likelihood, never a
 * certainty — Aurora's anti-cheat is informational only and deliberately
 * avoids definitive accusations.
 */
public final class DetectionResult {

    public final String checkName;
    public final double confidence;
    public final String reason;

    public DetectionResult(String checkName, double confidence, String reason) {
        this.checkName = checkName;
        this.confidence = Math.max(0, Math.min(1, confidence));
        this.reason = reason;
    }

    /** @return confidence expressed as a whole-number percentage. */
    public int getPercent() { return (int) Math.round(confidence * 100); }
}
