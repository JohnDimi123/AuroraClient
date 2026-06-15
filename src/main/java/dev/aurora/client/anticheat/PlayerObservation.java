package dev.aurora.client.anticheat;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A rolling window of observed data for one player, fed each tick from publicly
 * visible state (position, rotation, attack events). Checks read these buffers
 * to compute statistical features. No private/hidden data is ever used.
 */
public final class PlayerObservation {

    public final String name;

    /** Recent yaw/pitch samples for rotation analysis. */
    public final Deque<float[]> rotations = new ArrayDeque<>();
    /** Recent (x, y, z) positions for movement analysis. */
    public final Deque<double[]> positions = new ArrayDeque<>();
    /** Recent attack timestamps (ms) for combat-timing analysis. */
    public final Deque<Long> attackTimes = new ArrayDeque<>();

    private static final int MAX_SAMPLES = 40;

    public PlayerObservation(String name) { this.name = name; }

    public void addRotation(float yaw, float pitch) {
        rotations.addLast(new float[]{yaw, pitch});
        trim(rotations);
    }

    public void addPosition(double x, double y, double z) {
        positions.addLast(new double[]{x, y, z});
        trim(positions);
    }

    public void addAttack(long time) {
        attackTimes.addLast(time);
        trim(attackTimes);
    }

    private void trim(Deque<?> deque) {
        while (deque.size() > MAX_SAMPLES) deque.pollFirst();
    }

    public boolean hasEnoughData() {
        return rotations.size() >= 8 || positions.size() >= 8;
    }
}
