package dev.aurora.client.module.impl.performance;

import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;

/**
 * Toggles for expensive cosmetic animations. Read by render mixins; when the
 * active profile is POTATO/PERFORMANCE these default to on for max FPS.
 */
public final class AnimationOptimizer extends Module {

    private static AnimationOptimizer active;

    private final BooleanSetting reduceItemSwing =
            new BooleanSetting("Lean Item Swing", "Simplify held-item animation", false);
    private final BooleanSetting staticEffects =
            new BooleanSetting("Static Effects", "Disable animated GUI effects", false);

    public AnimationOptimizer() {
        super("Animation Optimizer", "Reduces cosmetic animation cost", Category.PERFORMANCE);
        addSettings(reduceItemSwing, staticEffects);
    }

    @Override protected void onEnable() { active = this; }
    @Override protected void onDisable() { if (active == this) active = null; }

    public static AnimationOptimizer getActive() { return active; }
    public boolean isReduceItemSwing() { return reduceItemSwing.get(); }

    /** Combines profile + setting to decide if animations should be minimised. */
    public boolean shouldMinimise() {
        if (FpsBooster.getActive() != null
                && FpsBooster.getActive().getProfile().disableAnimations) return true;
        return reduceItemSwing.get();
    }
}
