package dev.aurora.client.module.impl.performance;

/**
 * Pre-tuned bundles of optimisation aggressiveness.
 * <ul>
 *   <li>{@code MAX_QUALITY} – minimal culling, best visuals.</li>
 *   <li>{@code BALANCED} – sensible defaults for mid-range PCs.</li>
 *   <li>{@code PERFORMANCE} – aggressive culling, reduced effects.</li>
 *   <li>{@code POTATO} – maximum FPS for low-end hardware.</li>
 * </ul>
 */
public enum PerformanceProfile {
    MAX_QUALITY(1.0f, false, 64, false),
    BALANCED(0.75f, true, 48, false),
    PERFORMANCE(0.5f, true, 32, true),
    POTATO(0.25f, true, 16, true);

    public final float particleFraction;
    public final boolean cullEntities;
    public final int entityCullDistance;
    public final boolean disableAnimations;

    PerformanceProfile(float particleFraction, boolean cullEntities,
                       int entityCullDistance, boolean disableAnimations) {
        this.particleFraction = particleFraction;
        this.cullEntities = cullEntities;
        this.entityCullDistance = entityCullDistance;
        this.disableAnimations = disableAnimations;
    }
}
