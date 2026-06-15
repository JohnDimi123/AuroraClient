package dev.aurora.client.module.impl.performance;

import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.NumberSetting;

/**
 * Limits particle spawn volume. {@code MixinParticleManager} calls
 * {@link #shouldSpawn()} before adding a particle; a configurable fraction are
 * dropped. The active {@link PerformanceProfile} provides a baseline fraction.
 */
public final class ParticleOptimizer extends Module {

    private static ParticleOptimizer active;
    private final java.util.Random random = new java.util.Random();

    private final BooleanSetting useProfile =
            new BooleanSetting("Use Profile", "Follow FPS Booster profile", true);
    private final NumberSetting keepFraction =
            new NumberSetting("Keep %", "Fraction of particles kept", 50, 0, 100, 5)
                    .visibleWhen(() -> !useProfile.get());
    private final BooleanSetting hideExplosions =
            new BooleanSetting("Lean Explosions", "Reduce explosion particles", false);

    public ParticleOptimizer() {
        super("Particle Optimizer", "Reduces particle rendering volume", Category.PERFORMANCE);
        addSettings(useProfile, keepFraction, hideExplosions);
    }

    @Override protected void onEnable() { active = this; }
    @Override protected void onDisable() { if (active == this) active = null; }

    public static ParticleOptimizer getActive() { return active; }

    /** @return true if a new particle should be spawned this call. */
    public boolean shouldSpawn() {
        float fraction;
        if (useProfile.get() && FpsBooster.getActive() != null) {
            fraction = FpsBooster.getActive().getProfile().particleFraction;
        } else {
            fraction = (float) (keepFraction.get() / 100.0);
        }
        return random.nextFloat() < fraction;
    }
}
