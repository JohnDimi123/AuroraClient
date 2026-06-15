package dev.aurora.client.render.animation;

/**
 * A time-based value interpolator independent of frame rate.
 * <p>
 * Call {@link #update()} each frame to advance the animation toward its target
 * using the configured {@link Easing}. Because it is driven by wall-clock time,
 * animation speed is consistent at any FPS.
 */
public final class Animation {

    /** Easing curves applied to the normalised 0..1 progress. */
    public enum Easing {
        LINEAR { public double apply(double t) { return t; } },
        EASE_OUT_QUAD { public double apply(double t) { return 1 - (1 - t) * (1 - t); } },
        EASE_IN_OUT { public double apply(double t) {
            return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2; } },
        EASE_OUT_EXPO { public double apply(double t) {
            return t >= 1 ? 1 : 1 - Math.pow(2, -10 * t); } };
        public abstract double apply(double t);
    }

    private double value;
    private double target;
    private double start;
    private long startTime;
    private final long durationMs;
    private final Easing easing;

    public Animation(double initial, long durationMs, Easing easing) {
        this.value = initial;
        this.target = initial;
        this.start = initial;
        this.durationMs = durationMs;
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
    }

    /** Sets a new target, restarting the interpolation from the current value. */
    public void setTarget(double target) {
        if (this.target == target) return;
        this.start = value;
        this.target = target;
        this.startTime = System.currentTimeMillis();
    }

    /** Advances and returns the current interpolated value. */
    public double update() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= durationMs) {
            value = target;
            return value;
        }
        double t = easing.apply((double) elapsed / durationMs);
        value = start + (target - start) * t;
        return value;
    }

    public double getValue() { return value; }
    public boolean isFinished() { return value == target; }
}
