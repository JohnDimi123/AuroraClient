package dev.aurora.client.anticheat.check;

import dev.aurora.client.anticheat.DetectionResult;
import dev.aurora.client.anticheat.PlayerObservation;

/**
 * A single heuristic that inspects a {@link PlayerObservation} and optionally
 * returns a {@link DetectionResult}.
 * <p>
 * Implementations must be conservative: they apply statistical thresholds with
 * built-in false-positive protection and never report certainty. Returning
 * {@code null} means "nothing suspicious observed".
 */
public interface Check {

    /** @return a short human-readable check name (e.g. "KillAura"). */
    String name();

    /**
     * Analyses the observation.
     *
     * @return a result if the configured confidence threshold is met, else null
     */
    DetectionResult analyse(PlayerObservation observation);
}
